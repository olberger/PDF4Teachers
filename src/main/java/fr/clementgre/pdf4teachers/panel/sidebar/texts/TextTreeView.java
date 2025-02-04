package fr.clementgre.pdf4teachers.panel.sidebar.texts;

import fr.clementgre.pdf4teachers.document.editions.Edition;
import fr.clementgre.pdf4teachers.document.render.display.PageRenderer;
import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.panel.MainScreen.MainScreen;
import fr.clementgre.pdf4teachers.panel.sidebar.texts.TreeViewSections.TextTreeFavorites;
import fr.clementgre.pdf4teachers.panel.sidebar.texts.TreeViewSections.TextTreeLasts;
import fr.clementgre.pdf4teachers.panel.sidebar.texts.TreeViewSections.TextTreeOnFile;
import fr.clementgre.pdf4teachers.panel.sidebar.texts.TreeViewSections.TextTreeSection;
import fr.clementgre.pdf4teachers.components.NodeMenuItem;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.utils.sort.Sorter;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class TextTreeView extends TreeView<String>{

    public TreeItem<String> treeViewRoot = new TreeItem<>();

    public TextTreeFavorites favoritesSection = new TextTreeFavorites();
    public TextTreeLasts lastsSection = new TextTreeLasts();
    public TextTreeOnFile onFileSection = new TextTreeOnFile();

    public TextTreeView(Pane pane){

        setMaxWidth(400);
        setShowRoot(false);
        setEditable(true);
        setRoot(treeViewRoot);
        getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        treeViewRoot.getChildren().addAll(favoritesSection, lastsSection, onFileSection);

        disableProperty().bind(MainWindow.mainScreen.statusProperty().isNotEqualTo(MainScreen.Status.OPEN));
        setBackground(new Background(new BackgroundFill(Color.rgb(244, 244, 244), CornerRadii.EMPTY, Insets.EMPTY)));

        prefHeightProperty().bind(pane.heightProperty().subtract(layoutYProperty()));
        prefWidthProperty().bind(pane.widthProperty());
        widthProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            // Update element's graphic only if it is the last width value
            new Thread(() -> {
                try{ Thread.sleep(200); }catch(InterruptedException e){ e.printStackTrace(); }
                Platform.runLater(() -> {
                    if(getWidth() == newValue.longValue()) updateListsGraphic();
                });
            }).start();
        });

        setCellFactory((TreeView<String> param) -> new TreeCell<>() {
            @Override protected void updateItem(String item, boolean empty) {

                super.updateItem(item, empty);

                // Null
                if(empty){
                    setGraphic(null);
                    setStyle(null);
                    setContextMenu(null);
                    setOnMouseClicked(null);
                    return;
                }

                // TextElement
                if(getTreeItem() instanceof TextTreeItem){
                    ((TextTreeItem) getTreeItem()).updateCell(this);
                    return;
                }
                // TreeSection
                if(getTreeItem() instanceof TextTreeSection){
                    ((TextTreeSection) getTreeItem()).updateCell(this);
                    return;
                }
                // SortPanel
                if(getTreeItem() instanceof SortPanelTreeItem){
                    ((SortPanelTreeItem) getTreeItem()).updateCell(this);
                    return;
                }

                // Other
                setStyle(null);
                setGraphic(null);
                setContextMenu(null);
                setOnMouseClicked(null);
            }
        });

        Main.settings.textSmall.valueProperty().addListener((observable, oldValue, newValue) -> {
            refresh();
        });
        Main.settings.textOnlyStart.valueProperty().addListener((observable, oldValue, newValue) -> {
            refresh();
        });
    }

    public static ContextMenu getCategoryMenu(TextTreeSection section){

        ContextMenu menu = new ContextMenu();


        if(section.sectionType == TextTreeSection.ONFILE_TYPE){
            NodeMenuItem item3 = new NodeMenuItem(new HBox(), TR.trO("Supprimer tous les éléments textuels"), false);
            item3.setToolTip(TR.trO("Supprime tous les éléments textuels ajoutés au document, cela va donc supprimer une partie de l'édition."));
            menu.getItems().addAll(item3);

            item3.setOnAction(e -> {
                for(PageRenderer page : MainWindow.mainScreen.document.pages){
                    page.clearTextElements();
                }
                MainWindow.textTab.treeView.onFileSection.updateElementsList();
                Edition.setUnsave();
            });
        }else{
            NodeMenuItem item1 = new NodeMenuItem(new HBox(), TR.trO("Vider la liste"), false);
            item1.setToolTip(TR.trO("Supprime tous les éléments de la liste. Ne supprime en aucun cas les éléments sur le document."));
            NodeMenuItem item2 = new NodeMenuItem(new HBox(), TR.trO("Supprimer les donnés d'utilisation"), false);
            item2.setToolTip(TR.trO("Réinitialise les donnés des éléments de la liste indiquant le nombre d'utilisation de l'élément. Cela va réinitialiser l'ordre du tri par Utilisation."));
            menu.getItems().addAll(item1, item2);

            item1.setOnAction(e -> {
                section.clearElements();
            });
            item2.setOnAction(e -> {
                for(Object element : section.getChildren()){
                    if(element instanceof TextTreeItem){
                        ((TextTreeItem) element).setUses(0);
                    }
                }
                if(section.sortManager.getSelectedButton().getText().equals(TR.trO("Utilisation"))){
                    section.sortManager.simulateCall();
                }
            });
        }
        NodeMenuItem.setupMenu(menu);
        return menu;
    }

    public void updateAutoComplete(){

        getSelectionModel().clearSelection();
        String matchText = MainWindow.textTab.txtArea.getText();

        if(!MainWindow.textTab.txtArea.isDisabled() && !matchText.isBlank()){


            int totalIndex = 1;
            int i;
            for(i = 0; i < favoritesSection.getChildren().size(); i++){
                if(favoritesSection.getChildren().get(i) instanceof TextTreeItem){
                    TextTreeItem item = (TextTreeItem) favoritesSection.getChildren().get(i);
                    if(item.getCore() != MainWindow.mainScreen.getSelected() && item.getText().toLowerCase().contains(matchText.toLowerCase())){
                        getSelectionModel().selectIndices(totalIndex + i, getSelectionModel().getSelectedIndices().stream().mapToInt(value -> value).toArray());
                    }
                }
            }
            totalIndex += i + 1;

            for(i = 0; i < lastsSection.getChildren().size(); i++){
                if(lastsSection.getChildren().get(i) instanceof TextTreeItem){
                    TextTreeItem item = (TextTreeItem) lastsSection.getChildren().get(i);
                    if(item.getCore() != MainWindow.mainScreen.getSelected() && item.getText().toLowerCase().contains(matchText.toLowerCase())){
                        getSelectionModel().selectIndices(totalIndex + i, getSelectionModel().getSelectedIndices().stream().mapToInt(value -> value).toArray());
                    }
                }
            }
            totalIndex += i + 1;

            for(i = 0; i < onFileSection.getChildren().size(); i++){
                if(onFileSection.getChildren().get(i) instanceof TextTreeItem){
                    TextTreeItem item = (TextTreeItem) onFileSection.getChildren().get(i);
                    if(item.getCore() != MainWindow.mainScreen.getSelected() && item.getText().toLowerCase().contains(matchText.toLowerCase())){
                        getSelectionModel().selectIndices(totalIndex + i, getSelectionModel().getSelectedIndices().stream().mapToInt(value -> value).toArray());
                    }
                }
            }

        }
        getSelectionModel().select(null);

    }
    public void selectNextInSelection(){
        if(getSelectionModel().getSelectedIndices().size() != 0){

            if(getSelectionModel().getSelectedItem() == null){
                selectFromSelectedIndex(0);
            }else{
                int lastIndex = getSelectionModel().getSelectedIndices().size() - 1;
                int toSelectIndex = getSelectionModel().getSelectedIndices().indexOf(getSelectionModel().getSelectedIndex()) + 1;

                if(toSelectIndex > lastIndex) selectTextField();
                else selectFromSelectedIndex(toSelectIndex);
            }

        }
    }
    public void selectPreviousInSelection(){
        if(getSelectionModel().getSelectedIndices().size() != 0){
            int lastIndex = getSelectionModel().getSelectedIndices().size() - 1;

            if(getSelectionModel().getSelectedItem() == null){
                selectFromSelectedIndex(lastIndex);
            }else{
                int toSelectIndex = getSelectionModel().getSelectedIndices().indexOf(getSelectionModel().getSelectedIndex()) - 1;

                if(toSelectIndex < 0) selectTextField();
                else selectFromSelectedIndex(toSelectIndex);
            }

        }
    }
    private void selectTextField(){
        MainWindow.textTab.treeView.scrollTo(0);
        getSelectionModel().select(null);
        MainWindow.textTab.txtArea.requestFocus();
    }
    private void selectFromSelectedIndex(int index){
        MainWindow.textTab.treeView.scrollTo(getSelectionModel().getSelectedIndices().get(index)-3);
        Platform.runLater(() -> {
            int realIndex = getSelectionModel().getSelectedIndices().get(index);
            getSelectionModel().select(realIndex);
        });
    }


    public static ContextMenu getNewMenu(TextTreeItem element){

        ContextMenu menu = new ContextMenu();
        NodeMenuItem item1 = new NodeMenuItem(new HBox(), TR.trO("Ajouter et lier"), false);
        item1.setToolTip(TR.trO("Ajoute cet élément à l'édition et lie l'élément de l'édition avec celui de la liste. Toute modification apportée à l'élément de l'édition entrainera la modification de l'élément dans la liste."));
        NodeMenuItem item2 = new NodeMenuItem(new HBox(), TR.trO("Retirer"), false);
        item2.setToolTip(TR.trO("Retire cet élément de la liste. Dans la liste Éléments sur ce document, ceci supprime aussi l'élément sur le document édité"));
        NodeMenuItem item3 = new NodeMenuItem(new HBox(), TR.trO("Ajouter aux favoris"), false);
        item3.setToolTip(TR.trO("Ajoute cet élément à la liste des éléments précédents."));
        NodeMenuItem item4 = new NodeMenuItem(new HBox(), TR.trO("Ajouter aux éléments précédents"), false);
        item4.setToolTip(TR.trO("Ajoute cet élément à la liste des éléments favoris."));
        NodeMenuItem item5 = new NodeMenuItem(new HBox(), TR.trO("Dé-lier l'élément"), false);
        item5.setToolTip(TR.trO("L'élément de la liste ne sera plus synchronisé avec l'élément du document"));


        // Ajouter les items en fonction du type
        if(element.getType() != TextTreeSection.ONFILE_TYPE) menu.getItems().add(item1);
        menu.getItems().add(item2);
        if(element.getType() != TextTreeSection.FAVORITE_TYPE) menu.getItems().add(item3); // onFile & lasts
        if(element.getType() == TextTreeSection.ONFILE_TYPE) menu.getItems().add(item4); // onFile
        if(element.getType() != TextTreeSection.ONFILE_TYPE && element.getCore() != null) menu.getItems().add(item5); // élément précédent qui est lié

        NodeMenuItem.setupMenu(menu);

        // Définis les actions des boutons
        item1.setOnAction((e) -> {
            element.addToDocument(true);
            if(element.getType() == TextTreeSection.FAVORITE_TYPE){
                if(MainWindow.textTab.treeView.favoritesSection.sortManager.getSelectedButton().getText().equals(TR.trO("Utilisation"))){
                    MainWindow.textTab.treeView.favoritesSection.sortManager.simulateCall();
                }
            }else if(element.getType() == TextTreeSection.LAST_TYPE){
                if(MainWindow.textTab.treeView.lastsSection.sortManager.getSelectedButton().getText().equals(TR.trO("Utilisation"))){
                    MainWindow.textTab.treeView.lastsSection.sortManager.simulateCall();
                }
            }
        });
        item2.setOnAction((e) -> {
            if(element.getType() == TextTreeSection.ONFILE_TYPE){
                element.getCore().delete();
            }else{
                removeSavedElement(element);
            }
        });
        item3.setOnAction((e) -> {
            addSavedElement(new TextTreeItem(element.getFont(), element.getText(), element.getColor(), TextTreeSection.FAVORITE_TYPE, 0, System.currentTimeMillis()/1000));
            if(element.getType() == TextTreeSection.LAST_TYPE){
                if(Main.settings.textAutoRemove.getValue()){
                    removeSavedElement(element);
                }
            }
        });
        item4.setOnAction((e) -> {
            addSavedElement(new TextTreeItem(element.getFont(), element.getText(), element.getColor(), TextTreeSection.LAST_TYPE, 0, System.currentTimeMillis()/1000));
        });
        item5.setOnAction((e) -> {
            element.unLink();
        });
        return menu;

    }

    public static void updateListsGraphic(){
        MainWindow.textTab.treeView.favoritesSection.updateChildrenGraphics();
        MainWindow.textTab.treeView.lastsSection.updateChildrenGraphics();
        MainWindow.textTab.treeView.onFileSection.updateChildrenGraphics();
    }

    public static void addSavedElement(TextTreeItem element){
        if(element.getType() == TextTreeSection.FAVORITE_TYPE){
            MainWindow.textTab.treeView.favoritesSection.addElement(element);
        }else if(element.getType() == TextTreeSection.LAST_TYPE){
            MainWindow.textTab.treeView.lastsSection.addElement(element);
        }
    }
    public static void removeSavedElement(TextTreeItem element){
        if(element.getType() == TextTreeSection.FAVORITE_TYPE){
            MainWindow.textTab.treeView.favoritesSection.removeElement(element);
        }else if(element.getType() == TextTreeSection.LAST_TYPE){
            MainWindow.textTab.treeView.lastsSection.removeElement(element);
        }
    }

    public static List<TextTreeItem> autoSortList(List<TextTreeItem> toSort, String sortType, boolean order){

        if(sortType.equals(TR.trO("Ajout"))){
            return Sorter.sortElementsByDate(toSort, order);
        }else if(sortType.equals(TR.trO("Nom"))){
            return Sorter.sortElementsByName(toSort, order);
        }else if(sortType.equals(TR.trO("Utilisation"))){
            return Sorter.sortElementsByUtils(toSort, order);
        }else if(sortType.equals(TR.trO("Police"))){
            return Sorter.sortElementsByPolice(toSort, order);
        }else if(sortType.equals(TR.trO("Taille"))){
            return Sorter.sortElementsBySize(toSort, order);
        }else if(sortType.equals(TR.trO("Couleur"))){
            return Sorter.sortElementsByColor(toSort, order);
        }else if(sortType.equals(TR.trO("Position"))){
            return Sorter.sortElementsByCorePosition(toSort, order);
        }
        return toSort;
    }

    public static List<TextTreeItem> getMostUseElements(){

        List<TextTreeItem> toSort = new ArrayList<>();
        for(int i = 0; i < MainWindow.textTab.treeView.favoritesSection.getChildren().size(); i++){
            if(MainWindow.textTab.treeView.favoritesSection.getChildren().get(i) instanceof TextTreeItem){
                toSort.add((TextTreeItem) MainWindow.textTab.treeView.favoritesSection.getChildren().get(i));
            }
        }
        for(int i = 0; i < MainWindow.textTab.treeView.lastsSection.getChildren().size(); i++){
            if(MainWindow.textTab.treeView.lastsSection.getChildren().get(i) instanceof TextTreeItem){
                toSort.add((TextTreeItem) MainWindow.textTab.treeView.lastsSection.getChildren().get(i));
            }
        }
        return autoSortList(toSort, TR.trO("Utilisation"), true);

    }
}


