package fr.themsou.panel.LeftBar;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import fr.themsou.main.Main;
import fr.themsou.utils.Builders;
import fr.themsou.utils.SortEvent;
import fr.themsou.utils.SortManager;
import fr.themsou.utils.Sorter;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class LBFilesTab extends Tab {

	private SortManager sortManager;
	private VBox separator = new VBox();
	private GridPane options = new GridPane();
	public ListView<File> files = new ListView<>();

	public LBFilesTab(){

		setClosable(false);
		setContent(separator);

		setGraphic(Builders.buildImage(getClass().getResource("/img/PDF-Document.png")+"", 0, 25));
		Main.leftBar.getTabs().add(0, this);

		setup();
	}

	public void setup(){
		files.setStyle("-fx-border-width: 0px;");
		files.setPrefWidth(270);
		files.setMaxHeight(Double.MAX_VALUE);
		VBox.setVgrow(files, Priority.ALWAYS);
		new LBFilesListView(files);

		files.setOnDragOver(new EventHandler<DragEvent>(){
			@Override
			public void handle(DragEvent e) {
				Dragboard db = e.getDragboard();
				if(db.hasFiles()){
					for(File file : db.getFiles()){
						if(isFilePdf(file) || file.isDirectory()){
							e.acceptTransferModes(TransferMode.ANY);
							e.consume();
							return;
						}
					}
				}
				e.consume();
			}
		});
		files.setOnDragDropped(new EventHandler<DragEvent>(){
			@Override
			public void handle(DragEvent e) {
				Dragboard db = e.getDragboard();
				if(db.hasFiles()){
					for(File file : db.getFiles()){
						if(isFilePdf(file) || file.isDirectory()){
							File[] files = db.getFiles().toArray(new File[db.getFiles().size()]);
							openFiles(files);
							e.setDropCompleted(true);
							e.consume();
							return;
						}
					}
				}

				e.consume();
			}
		});

		sortManager = new SortManager(new SortEvent() {
			@Override public void call(String sortType, boolean order) {
				System.out.println("sort by " + sortType + " - " + (order ? "AZ" : "ZA"));

				if(sortType.equals("Nom")){
					List<File> toSort = files.getItems().stream().collect(Collectors.toList());
					files.getItems().clear();
					files.getItems().addAll(Sorter.sortByName(toSort, order));
				}else if(sortType.equals("Dossier")){
					List<File> toSort = files.getItems().stream().collect(Collectors.toList());
					files.getItems().clear();
					files.getItems().addAll(Sorter.sortByDir(toSort, order));
				}else if(sortType.equals("Édition")){
					List<File> toSort = files.getItems().stream().collect(Collectors.toList());
					files.getItems().clear();
					files.getItems().addAll(Sorter.sortByEdit(toSort, order));
				}else if(sortType.equals("Date d'Ajout")){
					backOpenFilesList(!order);
				}

			}
		}, null, null);
		sortManager.setup(options, "Date d'Ajout", "Date d'Ajout", "Édition", "\n", "Nom", "Dossier");

		// import last session files
		if(Main.settings.getOpenedFiles() != null){
			files.getItems().addAll(Main.settings.getOpenedFiles());
		}
		separator.getChildren().addAll(options, files);

		/*files.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<File>() {
			@Override public void changed(ObservableValue<? extends File> observableValue, File lastFile, File newFile) {
				Main.mainScreen.openFile(newFile);
			}
		});*/

	}

	private void openFile(File file){
		
		if(!file.isDirectory()){
			if(isFilePdf(file) && !files.getItems().contains(file)){
				files.getItems().add(file);
				addOpenFilesList(file);
				sortManager.simulateCall();
			}
		}else{
			for(File VFile : Objects.requireNonNull(file.listFiles())){
				if(isFilePdf(VFile) && !files.getItems().contains(VFile)){
					files.getItems().add(VFile);
					addOpenFilesList(VFile);
				}
			}
			sortManager.simulateCall();
		}
	}
	public void openFiles(File[] files){
		for(File file : files){
			openFile(file);
		}
	}
	public void clearFiles(boolean confirm){
		/*if(Main.mainScreen.getStatus() == -1){
			if(files.getItems().contains(Main.mainScreen.document.getFile())){
				if(!Main.mainScreen.closeFile(confirm)) return;
			}
		}*/
		files.getItems().clear();
		updateOpenFilesList();
	}
	public void removeFile(File file, boolean confirm){
		/*if(Main.mainScreen.getStatus() == -1){
			if(files.getItems().contains(Main.mainScreen.document.getFile())){
				if(!Main.mainScreen.closeFile(confirm)) return;
			}
		}*/
		files.getItems().remove(file);
		removeOpenFilesList(file);
	}
	
	private boolean isFilePdf(File file) {
        String fileName = file.getName();
        String ext = "";
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) 
        	ext = fileName.substring(fileName.lastIndexOf(".") + 1);

		return ext.equals("pdf");
	}

	private void updateOpenFilesList(){

		ArrayList<File> openedFilesList = new ArrayList<>();
		for(File file : Main.lbFilesTab.files.getItems()){
			openedFilesList.add(file);
		}
		Main.settings.setOpenedFiles(openedFilesList);

	}
	private void backOpenFilesList(boolean reverse){

		files.getItems().clear();
		ArrayList<File> openedFilesList = (ArrayList<File>) Main.settings.getOpenedFiles().clone();
		if(reverse) Collections.reverse(openedFilesList);
		for(File file : openedFilesList){
			files.getItems().add(file);
		}
	}
	private void addOpenFilesList(File file){
		Main.settings.addOpenedFiles(file);
	}
	private void removeOpenFilesList(File file){
		Main.settings.removeOpenedFiles(file);
	}

	

}