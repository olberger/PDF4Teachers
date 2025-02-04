package fr.clementgre.pdf4teachers.interfaces.autotips;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import javafx.application.Platform;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AutoTipsManager {

    @ToolTipVar(actionKey = "", prerequisiteKey = "document", objectWhereDisplay = "mainscreen")
    private static final String documentContextMenu = "autoTips.documentContextMenu";

    @ToolTipVar(actionKey = "", prerequisiteKey = "document", objectWhereDisplay = "mainscreen")
    private static final String documentZoom = "autoTips.documentZoom";

    @ToolTipVar(actionKey = "", prerequisiteKey = "texttabselected", objectWhereDisplay = "leftbar")
    private static final String textElementsLists = "autoTips.textElementsLists";

    @ToolTipVar(actionKey = "", prerequisiteKey = "texttabselected", objectWhereDisplay = "leftbar")
    private static final String textElementsSort = "autoTips.textElementsSort";

    @ToolTipVar(actionKey = "", prerequisiteKey = "texttabselected", objectWhereDisplay = "leftbar")
    private static final String textAddLink = "autoTips.textAddLink";

    @ToolTipVar(actionKey = "", prerequisiteKey = "texttabselected", objectWhereDisplay = "leftbar")
    private static final String textKeyboardShortcutAddFavorite = "autoTips.textKeyboardShortcutAddFavorite";

    @ToolTipVar(actionKey = "", prerequisiteKey = "texttabselected", objectWhereDisplay = "leftbar")
    private static final String textSettingsMenuBar = "autoTips.textSettingsMenuBar";

    @ToolTipVar(actionKey = "", prerequisiteKey = "gradetabselected", objectWhereDisplay = "leftbar")
    private static final String gradeCustomFont = "autoTips.gradeCustomFont";

    @ToolTipVar(actionKey = "", prerequisiteKey = "gradetabselected", objectWhereDisplay = "leftbar")
    private static final String gradeExportCsv = "autoTips.gradeExportCsv";

    @ToolTipVar(actionKey = "", prerequisiteKey = "gradetabselected", objectWhereDisplay = "leftbar")
    private static final String gradeCopyGradeScale = "autoTips.gradeCopyGradeScale";


    @ToolTipVar(actionKey = "opendocument", prerequisiteKey = "", objectWhereDisplay = "mainscreen")
    private static final String editSystemAndExportation = "autoTips.editSystemAndExportation";

    @ToolTipVar(actionKey = "newtextelement", prerequisiteKey = "", objectWhereDisplay = "")
    private static final String newTextElement = "autoTips.newTextElement";

    @ToolTipVar(actionKey = "gradescalelock", prerequisiteKey = "", objectWhereDisplay = "")
    private static final String gradeScaleLock = "autoTips.gradeScaleLock";

    @ToolTipVar(actionKey = "gradeselect", prerequisiteKey = "", objectWhereDisplay = "")
    private static final String gradeContextMenu = "autoTips.gradeContextMenu";

    @ToolTipVar(actionKey = "gradecreate", prerequisiteKey = "", objectWhereDisplay = "")
    private static final String createGradeSameTiers = "autoTips.createGradeSameTiers";

    @ToolTipVar(actionKey = "graderename", prerequisiteKey = "", objectWhereDisplay = "")
    private static final String gradeBonus = "autoTips.gradeBonus";

    @ToolTipVar(actionKey = "graderename", prerequisiteKey = "", objectWhereDisplay = "")
    private static final String gradeKeyboardShortcuts = "autoTips.gradeKeyboardShortcuts";

    @ToolTipVar(actionKey = "textedit", prerequisiteKey = "hastextsimilarelements", objectWhereDisplay = "")
    private static final String textAutoCompletion = "autoTips.textAutoCompletion";

    @ToolTipVar(actionKey = "textedit", prerequisiteKey = "", objectWhereDisplay = "")
    private static final String textAutoWrap = "autoTips.textAutoWrap";

    @ToolTipVar(actionKey = "textedit", prerequisiteKey = "", objectWhereDisplay = "")
    private static final String textLatex = "autoTips.textLatex";

    @ToolTipVar(actionKey = "textedit", prerequisiteKey = "", objectWhereDisplay = "")
    private static final String textUrl = "autoTips.textUrl";

    @ToolTipVar(actionKey = "textselect", prerequisiteKey = "", objectWhereDisplay = "")
    private static final String textContextMenu = "autoTips.textContextMenu";

    @ToolTipVar(actionKey = "textclone", prerequisiteKey = "", objectWhereDisplay = "")
    private static final String textDuplicate = "autoTips.textDuplicate";

    @ToolTipVar(actionKey = "gradereset", prerequisiteKey = "", objectWhereDisplay = "")
    private static final String gradeSet0WithDoubleClick = "autoTips.gradeSet0WithDoubleClick";

    private static HashMap<String, AutoTipTooltip> uiTips = new HashMap<>();

    public static void setup(){
        Main.settings.allowAutoTips.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue){
                addAllTips();
            }else{
                removeAllTips();
            }
        });
    }

    private static Thread autoTipsThread = new Thread(() -> {
        while (true){
            try{
                Thread.sleep(3*60*1000);
            }catch(InterruptedException e){ e.printStackTrace(); }

            Platform.runLater(AutoTipsManager::showRandom);
        }
    }, "AutoTipsTimer");

    public static void load(){
        uiTips.clear();

        if(!Main.settings.allowAutoTips.getValue()) return;

        boolean stillHasAuto = false;

        for(Field field : AutoTipsManager.class.getDeclaredFields()) {
            if(field.isAnnotationPresent(ToolTipVar.class)){
                try{

                    String name = field.getName();
                    if(!MainWindow.userData.autoTipsValidated.contains(name)){

                        ToolTipVar data = field.getAnnotation(ToolTipVar.class);
                        uiTips.put(name, new AutoTipTooltip(name, data.actionKey(), data.prerequisiteKey(), data.objectWhereDisplay()));

                        if(data.actionKey().isEmpty()) stillHasAuto = true;

                    }

                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }

        if(!stillHasAuto) return;
        if(!autoTipsThread.isAlive()) autoTipsThread.start();

    }
    public static List<String> getCompletedAutoTips(){

        List<String> completed = new ArrayList<>();

        for(Field field : AutoTipsManager.class.getDeclaredFields()) {
            if(field.isAnnotationPresent(ToolTipVar.class)){
                try{
                    String name = field.getName();
                    if(!uiTips.containsKey(name)){
                        completed.add(name);
                    }

                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
        return completed;
    }

    public static boolean showRandom(){

        for(AutoTipTooltip uiTip : uiTips.values()){
            if(uiTip.getActionKey().isEmpty()){
                if(showByObject(uiTip)) return true;
            }
        }
        return false;
    }

    public static boolean showByAction(String actionKey){

        for(AutoTipTooltip uiTip : uiTips.values()){
            if(uiTip.getActionKey().equalsIgnoreCase(actionKey)){
                if(showByObject(uiTip)) return true;
            }
        }
        return false;
    }
    public static boolean showByName(String name){

        if(uiTips.containsKey(name)){
            return showByObject(uiTips.get(name));
        }
        return false;
    }
    public static boolean showByObject(AutoTipTooltip uiTip){

        if(isPrerequisiteValid(uiTip.getPrerequisiteKey())){
            uiTip.show(Main.window);
            return true;
        }
        return false;
    }

    public static void removeTip(String name){
        uiTips.remove(name);
    }
    public static void removeAllTips(){
        uiTips.clear();
    }
    public static void addAllTips(){
        MainWindow.userData.autoTipsValidated.clear();
        load();
    }

    private static boolean isPrerequisiteValid(String prerequisiteKey){
        if(prerequisiteKey.isEmpty()) return true;
        switch (prerequisiteKey){
            case "document" -> {
                return MainWindow.mainScreen.hasDocument(false);
            }
            case "texttabselected" -> {
                return MainWindow.textTab.isSelected();
            }
            case "gradetabselected" -> {
                return MainWindow.gradeTab.isSelected();
            }
            case "hastextsimilarelements" -> {
                return MainWindow.textTab.treeView.getSelectionModel().getSelectedIndices().size() >= 2;
            }
        }
        return false;
    }
}
