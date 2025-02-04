package fr.clementgre.pdf4teachers.panel.sidebar.grades;

import fr.clementgre.pdf4teachers.document.editions.elements.Element;
import fr.clementgre.pdf4teachers.document.editions.elements.GradeElement;
import fr.clementgre.pdf4teachers.document.render.display.PageRenderer;
import fr.clementgre.pdf4teachers.interfaces.autotips.AutoTipsManager;
import fr.clementgre.pdf4teachers.panel.MainScreen.MainScreen;
import fr.clementgre.pdf4teachers.panel.sidebar.SideTab;
import fr.clementgre.pdf4teachers.panel.sidebar.grades.export.GradeExportWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.FontUtils;
import fr.clementgre.pdf4teachers.utils.PaneUtils;
import fr.clementgre.pdf4teachers.utils.StringUtils;
import fr.clementgre.pdf4teachers.utils.image.ImageUtils;
import fr.clementgre.pdf4teachers.utils.image.SVGPathIcons;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.HashMap;

@SuppressWarnings("serial")
public class GradeTab extends SideTab {

    public VBox pane = new VBox();
    public HBox optionPane = new HBox();

    public GradeTreeView treeView;

    public static HashMap<Integer, TiersFont> fontTiers = new HashMap<>();

    public ToggleButton sumByDecrement = new ToggleButton();
    public ToggleButton lockGradeScale = new ToggleButton();
    private Button settings = new Button();
    private Button link = new Button();
    private Button export = new Button();

    public GradeTab(){
        super("grades", SVGPathIcons.ON_TWENTY, 29, 0, new int[]{500, 440});

        setContent(pane);
        setup();
    }
    public void setup(){

        fontTiers.put(0, new TiersFont(Font.loadFont(FontUtils.getFontFile("Open Sans", false, false), 28), Color.valueOf("#990000"), true, false));
        fontTiers.put(1, new TiersFont(Font.loadFont(FontUtils.getFontFile("Open Sans", false, false), 24), Color.valueOf("#b31a1a"), false, false));
        fontTiers.put(2, new TiersFont(Font.loadFont(FontUtils.getFontFile("Open Sans", false, false), 18), Color.valueOf("#cc3333"), false, false));
        fontTiers.put(3, new TiersFont(Font.loadFont(FontUtils.getFontFile("Open Sans", false, false), 18), Color.valueOf("#e64d4d"), false, false));
        fontTiers.put(4, new TiersFont(Font.loadFont(FontUtils.getFontFile("Open Sans", false, false), 18), Color.valueOf("#ff6666"), false, false));

        PaneUtils.setHBoxPosition(sumByDecrement, 45, 35, 0);
        sumByDecrement.setCursor(Cursor.HAND);
        sumByDecrement.setSelected(false);
        sumByDecrement.setGraphic(SVGPathIcons.generateImage(SVGPathIcons.LEVEL_DOWN, "black", 0, 26, 26, 0, new int[]{1, 1}, ImageUtils.defaultDarkColorAdjust));
        sumByDecrement.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            treeView.updateAllSum();
            if(newValue){
                AutoTipsManager.showByAction("gradescaleinvert");
            }
        });
        sumByDecrement.setTooltip(PaneUtils.genToolTip(TR.trO("Compter les points par retranchement") + "\n" + TR.trO("Les notes sont initialisées par défaut à leur valeur max. La non saisie des notes mène à la note max au lieu de 0.")));

        PaneUtils.setHBoxPosition(lockGradeScale, 45, 35, 0);
        lockGradeScale.setCursor(Cursor.HAND);
        lockGradeScale.setSelected(false);
        lockGradeScale.setGraphic(ImageUtils.buildImage(getClass().getResource("/img/GradesTab/cadenas.png") + "", 0, 0, ImageUtils.defaultDarkColorAdjust));
        lockGradeScale.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if(newValue){
                lockGradeScale.setGraphic(ImageUtils.buildImage(getClass().getResource("/img/GradesTab/cadenas-ferme.png") + "", 0, 0, ImageUtils.defaultDarkColorAdjust));
                AutoTipsManager.showByAction("gradescalelock");
            }
            else lockGradeScale.setGraphic(ImageUtils.buildImage(getClass().getResource("/img/GradesTab/cadenas.png") + "", 0, 0, ImageUtils.defaultDarkColorAdjust));

            // Update the selected cell
            if(treeView.getSelectionModel().getSelectedItem() != null){
                int selected = treeView.getSelectionModel().getSelectedIndex();
                treeView.getSelectionModel().select(null);
                treeView.getSelectionModel().select(selected);
            }
        });
        lockGradeScale.setTooltip(PaneUtils.genToolTip(TR.trO("Vérouiller le barème, il ne pourra plus être modifié.")));

        PaneUtils.setHBoxPosition(settings, 45, 35, 0);
        settings.setCursor(Cursor.HAND);
        settings.setGraphic(ImageUtils.buildImage(getClass().getResource("/img/GradesTab/engrenage.png")+"", 0, 0, ImageUtils.defaultDarkColorAdjust));
        settings.setOnAction((e) -> new GradeSettingsWindow());
        settings.setTooltip(PaneUtils.genToolTip(TR.trO("Modifier les polices, couleurs et préfixe de chaque niveau de notes.")));

        PaneUtils.setHBoxPosition(link, 45, 35, 0);
        link.setCursor(Cursor.HAND);
        link.setGraphic(ImageUtils.buildImage(getClass().getResource("/img/GradesTab/link.png")+"", 0, 0, ImageUtils.defaultDarkColorAdjust));
        link.disableProperty().bind(MainWindow.mainScreen.statusProperty().isNotEqualTo(MainScreen.Status.OPEN));
        link.setOnAction((e) -> new GradeCopyGradeScaleDialog().show());
        link.setTooltip(PaneUtils.genToolTip(TR.trO("Envoyer le barème sur d'autres éditions.")));

        PaneUtils.setHBoxPosition(export, 45, 35, 0);
        export.setCursor(Cursor.HAND);
        export.setGraphic(ImageUtils.buildImage(getClass().getResource("/img/GradesTab/exporter.png")+"", 0, 0, ImageUtils.defaultDarkColorAdjust));
        export.disableProperty().bind(MainWindow.mainScreen.statusProperty().isNotEqualTo(MainScreen.Status.OPEN));
        export.setOnAction((e) -> new GradeExportWindow());
        export.setTooltip(PaneUtils.genToolTip(TR.trO("Exporter les notes d'une ou plusieurs copies, dans un ou plusieurs fichier CSV. Ceci permet ensuite d'importer les notes dans un logiciel tableur")));

        optionPane.setStyle("-fx-padding: 5 0 5 0;");
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        optionPane.getChildren().addAll(spacer, sumByDecrement, lockGradeScale, settings, link, export);

        treeView = new GradeTreeView(this);
        pane.getChildren().addAll(optionPane, treeView);

    }

    public GradeElement newGradeElementAuto(GradeTreeItem parent){

        PageRenderer page = MainWindow.mainScreen.document.pages.get(0);
        if(MainWindow.mainScreen.document.getCurrentPage() != -1) page = MainWindow.mainScreen.document.pages.get(MainWindow.mainScreen.document.getCurrentPage());

        MainWindow.mainScreen.setSelected(null);

        String name = TR.trO("Nouvelle note");
        if(parent.getChildren().size() >= 1){
            String lastName = ((GradeTreeItem) parent.getChildren().get(parent.getChildren().size()-1)).getCore().getName();
            String newName = StringUtils.incrementName(lastName);
            if(!lastName.equals(newName)) name = newName;
        }

        GradeElement current = new GradeElement((int) (60 * Element.GRID_WIDTH / page.getWidth()), (int) (page.getMouseY() * Element.GRID_HEIGHT / page.getHeight()), page.getPage(),
                true, -1, 0, parent.getChildren().size(), GradeTreeView.getElementPath(parent), name, false);

        page.addElement(current, true);
        current.centerOnCoordinatesY();
        MainWindow.mainScreen.setSelected(current);

        return current;
    }

    public GradeElement newGradeElement(String name, double value, double total, int index, String parentPath, boolean update){

        PageRenderer page = MainWindow.mainScreen.document.pages.get(0);
        if(MainWindow.mainScreen.document.getCurrentPage() != -1) page = MainWindow.mainScreen.document.pages.get(MainWindow.mainScreen.document.getCurrentPage());

        if(update) MainWindow.mainScreen.setSelected(null);

        GradeElement current = new GradeElement((int) (60 * Element.GRID_WIDTH / page.getWidth()), (int) (page.getMouseY() * Element.GRID_HEIGHT / page.getHeight()), page.getPage(),
                true, value, total, index, parentPath, name, false);

        page.addElement(current, update);
        current.centerOnCoordinatesY();
        if(update) MainWindow.mainScreen.setSelected(current);

        return current;
    }

    public void updateElementsFont(){
        if(treeView.getRoot() != null){
            GradeTreeItem root = ((GradeTreeItem) treeView.getRoot());
            if(root.hasSubGrade()) updateElementFont(root);
            root.getCore().updateFont();
        }
    }
    private void updateElementFont(GradeTreeItem parent){

        for(int i = 0; i < parent.getChildren().size(); i++){
            GradeTreeItem children = (GradeTreeItem) parent.getChildren().get(i);

            children.getCore().updateFont();
            if(children.hasSubGrade()) updateElementFont(children);
        }
    }

    public static Font getTierFont(int index){
        return fontTiers.get(index).getFont();
    }
    public static Color getTierColor(int index){
        return fontTiers.get(index).getColor();
    }
    public static boolean getTierShowName(int index){
        return fontTiers.get(index).isShowName();
    }
    public static boolean getTierHide(int index) {
        return fontTiers.get(index).isHide();
    }

    public BooleanProperty isLockGradeScaleProperty(){
        return lockGradeScale.selectedProperty();
    }
}
