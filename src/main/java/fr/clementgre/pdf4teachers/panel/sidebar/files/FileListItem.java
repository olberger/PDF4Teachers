package fr.clementgre.pdf4teachers.panel.sidebar.files;

import fr.clementgre.pdf4teachers.document.editions.Edition;
import fr.clementgre.pdf4teachers.document.render.export.ExportWindow;
import fr.clementgre.pdf4teachers.components.NodeMenuItem;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.StringUtils;
import fr.clementgre.pdf4teachers.utils.dialog.DialogBuilder;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;

import java.io.File;
import java.util.Collections;
import java.util.Optional;

public class FileListItem extends ListCell<File>{

    VBox pane;
    HBox nameBox;
    Label name;
    Label path;

    ImageView check = new ImageView();
    ImageView checkLow = new ImageView();

    ContextMenu menu;
    EventHandler<MouseEvent> onClick = e -> {
        if(e.getButton().equals(MouseButton.PRIMARY) && e.getClickCount() == 2) MainWindow.mainScreen.openFile(getItem());
    };

    public FileListItem(){
        setupGraphic();
    }

    public void setupGraphic(){

        pane = new VBox();
        nameBox = new HBox();
        name = new Label();
        path = new Label();

        HBox.setMargin(checkLow, new Insets(0, 4, 0, 0));
        HBox.setMargin(check, new Insets(0, 4, 0, 0));

        path.setStyle("-fx-font-size: 9;");
        pane.getChildren().addAll(nameBox, path);
        setStyle("-fx-padding: 2 15;");
    }

    @Override
    public void updateItem(File file, boolean empty) {
        super.updateItem(file, empty);

        if(empty){
            setGraphic(null);
            setTooltip(null);
            setContextMenu(null);
            setOnMouseClicked(null);

        }else{

            if(!file.exists()){
                MainWindow.filesTab.removeFile(file);
                return;
            }

            path.setText(getItem().getAbsolutePath().replace(System.getProperty("user.home"), "~").replace(getItem().getName(), ""));

            name.setText(StringUtils.removeAfterLastRegex(file.getName(), ".pdf"));
            if(file.getName().equals(".pdf")) name.setText(".pdf");
            name.setStyle("-fx-font-size: 13;");

            nameBox.getChildren().clear();

            try{
                double[] elementsCount = Edition.countElements(Edition.getEditFile(file));

                if(elementsCount.length > 0){ // has edit file
                    String grade = (elementsCount[4] == -1 ? "?" : MainWindow.format.format(elementsCount[4])) + "/" + MainWindow.format.format(elementsCount[5]);

                    if(elementsCount[0] > 0){ // Has Elements

                        name.setStyle("-fx-font-size: 12; -fx-font-weight: bold;");

                        path.setText(path.getText() + " | " + MainWindow.format.format(elementsCount[0]) + " " + TR.trO("Éléments") + " | " + grade);
                        setTooltip(new Tooltip(MainWindow.format.format(elementsCount[0]) + " " + TR.trO("Éléments") + " | " + grade + "\n" + MainWindow.format.format(elementsCount[1]) + " " + TR.trO("Commentaires") + "\n" + MainWindow.format.format(elementsCount[2]) + "/" + MainWindow.format.format(elementsCount[6]) + " " + TR.trO("Notes") + "\n" + MainWindow.format.format(elementsCount[3]) + " " + TR.trO("Figures")));

                        if(elementsCount[2] == elementsCount[6]){ // Edition completed : Green check
                            if(check.getImage() == null) check.setImage(new Image(getClass().getResource("/img/FilesTab/check.png") + ""));
                            nameBox.getChildren().add(check);
                        }else if(elementsCount[2] >= 1){ // Edition semi-completed : Orange check
                            if(checkLow.getImage() == null) checkLow.setImage(new Image(getClass().getResource("/img/FilesTab/check_low.png") + ""));
                            nameBox.getChildren().add(checkLow);
                        }

                    }else{ // Don't have elements
                        path.setText(path.getText() + " | " + TR.trO("Non édité") + " | " + grade);
                        setTooltip(new Tooltip(TR.trO("Non édité") + " | " + grade + "\n" + MainWindow.format.format(elementsCount[6]) + " " + TR.trO("Barèmes")));
                    }
                }else{ // don't have edit file
                    path.setText(path.getText() + " | " + TR.trO("Non édité"));
                    setTooltip(new Tooltip(TR.trO("Non édité")));
                }
            }catch(Exception e){
                path.setText(path.getText() + " | " + TR.trO("Impossible de récupérer les informations"));
                setTooltip(new Tooltip(e.getMessage()));
            }
            nameBox.getChildren().add(name);
            setGraphic(pane);

            setOnMouseClicked(onClick);

            ContextMenu menu = new ContextMenu();

            NodeMenuItem item1 = new NodeMenuItem(new HBox(), TR.trO("Ouvrir"), false);
            item1.setToolTip(TR.trO("Ouvre le fichier avec l'éditeur de PDF4Teachers. Il est aussi possible de l'ouvrir avec un double clic."));
            NodeMenuItem item2 = new NodeMenuItem(new HBox(), TR.trO("Retirer"), false);
            item2.setToolTip(TR.trO("Retire le fichier de la liste. Le fichier ne sera en aucun cas supprimé."));
            NodeMenuItem item3 = new NodeMenuItem(new HBox(), TR.trO("Supprimer l'édition"), false);
            item3.setToolTip(TR.trO("Réinitialise l'édition du document, retire tous les éléments ajoutés auparavant."));
            NodeMenuItem item4 = new NodeMenuItem(new HBox(), TR.trO("Supprimer le fichier"), false);
            item4.setToolTip(TR.trO("Supprime le fichier PDF sur l'ordinateur."));
            NodeMenuItem item5 = new NodeMenuItem(new HBox(), TR.trO("Exporter"), false);
            item5.setToolTip(TR.trO("Crée un nouveau fichier PDF à partir de celui-ci, avec tous les éléments ajoutés."));
            NodeMenuItem item6 = new NodeMenuItem(new HBox(), TR.trO("Vider la liste"), false);
            item6.setToolTip(TR.trO("Retire tous les fichiers de la liste. Les fichiers ne seront en aucun cas supprimé."));

            menu.getItems().addAll(item1, item2, item3, item4, item5, new SeparatorMenuItem(), item6);
            NodeMenuItem.setupMenu(menu);

            item1.setOnAction(e -> Platform.runLater(() -> MainWindow.mainScreen.openFile(file)));

            item2.setOnAction(e -> MainWindow.filesTab.removeFile(file));

            item3.setOnAction(e ->  Edition.clearEdit(file, true));

            item4.setOnAction(e -> {

                Alert alert = DialogBuilder.getAlert(Alert.AlertType.CONFIRMATION, TR.trO("Confirmation"));
                alert.setHeaderText(TR.trO("Êtes vous sûr de vouloir supprimer le document") + " " + file.getName() + " " + TR.trO("et son édition ?"));
                alert.setContentText(TR.trO("Cette action est irréversible."));

                Optional<ButtonType> result = alert.showAndWait();
                if(result.isEmpty()) return;
                if(result.get() == ButtonType.OK){
                    if(MainWindow.mainScreen.hasDocument(false)){
                        if(MainWindow.mainScreen.document.getFile().getAbsolutePath().equals(file.getAbsolutePath())){
                            MainWindow.mainScreen.closeFile(false);
                        }
                    }
                    MainWindow.filesTab.removeFile(file);
                    Edition.clearEdit(file, false);
                    file.delete();
                }

            });
            item5.setOnAction(e -> {
                if(file.exists()){

                    if(MainWindow.mainScreen.hasDocument(false)){
                        if(MainWindow.mainScreen.document.getFile().getAbsolutePath().equals(file.getAbsolutePath())){
                            MainWindow.mainScreen.document.save();
                        }
                    }

                    new ExportWindow(Collections.singletonList(file));
                }

            });
            item6.setOnAction(e -> MainWindow.filesTab.clearFiles());

            setContextMenu(menu);
        }
    }

}
