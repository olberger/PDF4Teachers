package fr.clementgre.pdf4teachers.panel.MainScreen;

import java.io.File;
import java.io.IOException;

import fr.clementgre.pdf4teachers.document.Document;
import fr.clementgre.pdf4teachers.document.editions.Edition;
import fr.clementgre.pdf4teachers.document.editions.elements.Element;
import fr.clementgre.pdf4teachers.document.render.convert.ConvertDocument;
import fr.clementgre.pdf4teachers.document.render.display.PageRenderer;
import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.document.render.display.PageZoneSelector;
import fr.clementgre.pdf4teachers.interfaces.autotips.AutoTipsManager;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import fr.clementgre.pdf4teachers.utils.dialog.DialogBuilder;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;

public class MainScreen extends Pane {

	public Pane pane = new Pane();
	public ZoomOperator zoomOperator;

	private int pageWidth = 596;

	public double paneMouseX = 0;
	public double paneMouseY = 0;

	public double mouseX = 0;
	public double mouseY = 0;

	private IntegerProperty status = new SimpleIntegerProperty(Status.CLOSED);
	public ObjectProperty<Element> selected = new SimpleObjectProperty<>();
	public Document document;
	public String failedEditFile = "";

	private Label info = new Label();
	private Hyperlink infoLink = new Hyperlink();

	public static class Status {
		public static final int CLOSED = 0;
		public static final int OPEN = 1;
		public static final int ERROR = 2;
		public static final int ERROR_EDITION = 3;
	}

	private static int dragNScrollFactor = 0;
	double dragStartX;
	double dragStartY;

	private static final Thread dragNScrollThread = new Thread(() -> {
		while(true){
			if(dragNScrollFactor != 0){
				Platform.runLater(() -> {
					if(dragNScrollFactor < 0){
						MainWindow.mainScreen.zoomOperator.scrollUp((dragNScrollFactor+50)/2, true);
					}else if(dragNScrollFactor > 0){
						MainWindow.mainScreen.zoomOperator.scrollDown(dragNScrollFactor/2, true);
					}
				});
				try{ Thread.sleep(20); }catch(InterruptedException ex){ ex.printStackTrace(); }
			}else{
				try{ Thread.sleep(200); }catch(InterruptedException ex){ ex.printStackTrace(); }
			}

		}

	}, "DragNScroll");

	public MainScreen(){

		//if(Main.isOSX()) PageRenderer.PAGE_HORIZONTAL_MARGIN = 15;

		setPrefWidth(Double.MAX_VALUE);

		setup();
		repaint();

	}

	public void repaint(){
		if(status.get() != Status.OPEN) {
			info.setVisible(true);

			if(status.get() == Status.CLOSED){
				infoLink.setVisible(true);
				info.setText(TR.trO("Aucun document ouvert"));
				infoLink.setText(TR.trO("Convertir des images en documents PDF"));
				infoLink.setOnAction(e -> new ConvertDocument());

			}else if(status.get() == Status.ERROR){
				info.setText(TR.trO("Impossible de charger ce document") + "\n\n" +
						TR.trO("Vérifiez que le fichier n'est pas corrompu") + "\n" +
						TR.trO("et que l'utilisateur a les droits de lecture sur ce fichier."));
				infoLink.setVisible(false);

			}else if(status.get() == Status.ERROR_EDITION){
				infoLink.setVisible(true);
				info.setText(TR.trO("Impossible de charger l'édition du document") + "\n\n" +
						TR.trO("Supprimez l'édition ou réparez la en modifiant le fichier d'éditions (YAML) dans :"));
				infoLink.setText(Main.dataFolder + "editions" + File.separator);
				infoLink.setOnAction(e -> PlatformUtils.openDirectory(failedEditFile));
			}
		}else{
			info.setVisible(false);
			infoLink.setVisible(false);
		}
	}
	public void setup(){

		setStyle("-fx-padding: 0; -fx-background-color: #484848;");
		setBorder(Border.EMPTY);

		pane.setStyle("-fx-background-color: #484848;");
		pane.setBorder(Border.EMPTY);
		getChildren().add(pane);

		info.setStyle("-fx-text-fill: white; -fx-font-size: 22;");
		info.setTextAlignment(TextAlignment.CENTER);

		info.translateXProperty().bind(widthProperty().divide(2).subtract(info.widthProperty().divide(2)));
		info.translateYProperty().bind(heightProperty().divide(2).subtract(info.heightProperty().divide(2)));
		getChildren().add(info);

		infoLink.setStyle("-fx-text-fill: white; -fx-font-size: 15;");
		infoLink.setLayoutY(60);
		infoLink.setTextAlignment(TextAlignment.CENTER);

		infoLink.translateXProperty().bind(widthProperty().divide(2).subtract(infoLink.widthProperty().divide(2)));
		infoLink.translateYProperty().bind(heightProperty().divide(2).subtract(infoLink.heightProperty().divide(2)));
		getChildren().add(infoLink);

		zoomOperator = new ZoomOperator(pane, this);

		// Update show status when scroll level change
		pane.translateYProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
			if(document != null){
				Platform.runLater(() -> document.updateShowsStatus());
			}
		});
		pane.scaleXProperty().addListener((observable, oldValue, newValue) -> {
			if(document != null){
				Platform.runLater(() -> document.updateZoom());
			}
		});

		addEventFilter(ZoomEvent.ZOOM, (ZoomEvent e) -> {
			if(getStatus() == Status.OPEN){
				zoomOperator.zoom(e.getZoomFactor(), e.getX(), e.getY());
			}
		});

		addEventFilter(ScrollEvent.SCROLL, e -> {
			if(e.isControlDown()){ // ZOOM

				if(getStatus() == Status.OPEN){
					if(e.getDeltaY() < 0){
						zoomOperator.zoom(1+e.getDeltaY()/200, e.getSceneX(), e.getSceneY());
					}else if(e.getDeltaY() > 0){
						zoomOperator.zoom(1+e.getDeltaY()/200, e.getSceneX(), e.getSceneY());
					}
				}
			}else{ // SCROLL

				if(Math.abs(e.getDeltaX()) > Math.abs(e.getDeltaY())/2){ // Accept side scrolling only if the scroll is not too vertical

					if(e.getDeltaX() != 0){
						if(e.getDeltaX() > 0){
							zoomOperator.scrollLeft((int) (e.getDeltaX() * 2.5), false);
						}else{
							zoomOperator.scrollRight((int) (-e.getDeltaX() * 2.5), false);
						}
					}
				}

				if(e.getDeltaY() != 0){
					if(e.getDeltaY() > 0){
						zoomOperator.scrollUp((int) (e.getDeltaY() * 2.5), false);
					}else{
						zoomOperator.scrollDown((int) (-e.getDeltaY() * 2.5), false);
					}
				}



			}

		});
		heightProperty().addListener((observable, oldValue, newValue) -> {
			if(document != null){
				Platform.runLater(() -> document.updateShowsStatus());
			}
		});

		setOnMouseDragged(e -> {
			if(!(((Node) e.getTarget()).getParent() instanceof Element) && !(e.getTarget() instanceof Element) && !(e.getTarget() instanceof PageZoneSelector)){ // GrabNScroll
				double distY = e.getY() - dragStartY;
				double distX = e.getX() - dragStartX;

				if(distY > 0){
					zoomOperator.scrollUp((int) distY, true);
				}else if(distY < 0){
					zoomOperator.scrollDown((int) -distY, true);
				}

				if(distX > 0){
					zoomOperator.scrollLeft((int) distX, true);
				}else if(distX < 0){
					zoomOperator.scrollRight((int) -distX, true);
				}
			}else{ // DragNScroll with an Element
				double y = Math.max(1, Math.min(getHeight(), e.getY()));
				if(y < 50){
					dragNScrollFactor = (int) (y*-1);
				}else if(getHeight() - y < 50){
					dragNScrollFactor = (int) ((getHeight()-y)*-1 + 50);
				}else{
					dragNScrollFactor = 0;
				}
			}

			dragStartY = e.getY();
			dragStartX = e.getX();

			mouseY = e.getY();
			mouseX = e.getX();
		});
		setOnMousePressed(e -> {
			dragStartX = e.getX();
			dragStartY = e.getY();
			setSelected(null);
			if(hasDocument(false)) setCursor(Cursor.CLOSED_HAND);
		});
		setOnMouseReleased(e -> {
			dragNScrollFactor = 0;
			setCursor(Cursor.DEFAULT);
		});
		setOnMouseMoved(e -> {
			mouseY = e.getY();
			mouseX = e.getX();
		});
		pane.setOnMouseMoved(e -> {
			paneMouseY = e.getY();
			paneMouseX = e.getX();
		});
		pane.setOnMouseDragged(e -> {
			paneMouseY = e.getY();
			paneMouseX = e.getX();
		});

		// window's name
		status.addListener((observable, oldValue, newValue) -> {
			updateWindowName();
		});
		Edition.isSaveProperty().addListener((observable, oldValue, newValue) -> {
			updateWindowName();
		});

		// Start the Drag and Scroll Thread
		if(!dragNScrollThread.isAlive()) dragNScrollThread.start();

	}
	private void updateWindowName(){
		if(status.get() == Status.OPEN){
			Main.window.setTitle("PDF4Teachers - " + document.getFile().getName() + (Edition.isSave() ? "" : "*"));
		}else{
			Main.window.setTitle(TR.tr("mainWindow.title.noDocument"));
		}
	}
	public void openFile(File file){

		if(!closeFile(!Main.settings.autoSave.getValue())){
			return;
		}

		repaint();
		MainWindow.footerBar.repaint();

		try{
			document = new Document(file);
		}catch(IOException e){
			e.printStackTrace();
			failOpen();
			return;
		}

		// FINISH OPEN
		MainWindow.footerBar.leftInfo.textProperty().bind(Bindings.createStringBinding(() -> TR.trO("zoom") + " : " + (int) (pane.getScaleX()*100) + "%", pane.scaleXProperty()));

		status.set(Status.OPEN);
		MainWindow.filesTab.files.getSelectionModel().select(file);

		document.showPages();
		try{
			document.loadEdition();
		}catch(Exception e){
			System.err.println("ERREUR : Impossible de changer l'édition");
			e.printStackTrace();
			closeFile(false);
			failedEditFile = Edition.getEditFile(file).getAbsolutePath();
			status.set(Status.ERROR_EDITION);
		}

		repaint();
		MainWindow.footerBar.repaint();
		Platform.runLater(() -> zoomOperator.updatePaneHeight(0, 0.5));
		AutoTipsManager.showByAction("opendocument");
	}
	public void failOpen(){

		document = null;
		status.set(Status.ERROR);
		repaint();
		MainWindow.footerBar.repaint();

	}
	public boolean closeFile(boolean confirm){

	    if(document != null){

	    	if(!Edition.isSave()){
				if(confirm){
					if(!document.save()){
						return false;
					}
				}else document.edition.save();
			}
			document.documentSaver.stop();
			document.close();
            document = null;
        }

	    pane.getChildren().clear();

		pane.setScaleX(Main.settings.defaultZoom.getValue()/100.0);
		pane.setScaleY(Main.settings.defaultZoom.getValue()/100.0);

		pane.setPrefHeight(1);
		pane.setPrefWidth(1);

		status.set(Status.CLOSED);
		selected.set(null);

		repaint();
		MainWindow.footerBar.repaint();

		System.runFinalization();
		return true;
	}
	public boolean hasDocument(boolean confirm){

		if(status.get() != Status.OPEN){
			if(confirm){
				Alert alert = DialogBuilder.getAlert(Alert.AlertType.ERROR, TR.trO("Erreur"));
				alert.setHeaderText(TR.trO("Aucun document n'est ouvert !"));
				alert.setContentText(TR.trO("Cette action est censée s'éxécuter sur un document ouvert"));

				alert.showAndWait();
			}
			return false;
		}
		return true;
	}

	public Element getSelected() {
		return selected.get();
	}
	public ObjectProperty<Element> selectedProperty() {
		return selected;
	}
	public void setSelected(Element selected) {
		this.selected.set(selected);
	}

	public IntegerProperty statusProperty() {
		return status;
	}
	public int getStatus(){
		return this.status.get();
	}

	public double getZoomFactor(){
		return pane.getScaleX();
	}
	public double getZoomPercent(){
		return getZoomFactor()*100;
	}

	public int getPageWidth() {
		return pageWidth;
	}

	public void addPage(PageRenderer page){
		pane.getChildren().add(page);
	}
	public void updateSize(int totalHeight){

		pane.setPrefWidth(pageWidth + (PageRenderer.PAGE_HORIZONTAL_MARGIN *2));
		pane.setPrefHeight(totalHeight);
	}
}