package fr.clementgre.pdf4teachers.document.render.display;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.utils.interfaces.CallBackArg;
import fr.clementgre.pdf4teachers.utils.objects.PositionDimensions;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

public class PageZoneSelector extends Pane {

    public enum SelectionZoneType{
        LIGHT_ON_DARK,
        PDF_ON_DARK
    }

    private PageRenderer page;
    private SelectionZoneType selectionZoneType;
    private Region selectionZone = new Region();

    private double startX = 0;
    private double startY = 0;
    CallBackArg<PositionDimensions> callBack;

    public PageZoneSelector(PageRenderer page){
        super();
        this.page = page;

        setCursor(Cursor.CROSSHAIR);
        setVisible(false);

        prefWidthProperty().bind(page.widthProperty());
        prefHeightProperty().bind(page.heightProperty());

        getChildren().add(selectionZone);
        page.getChildren().add(this);
    }

    public void setupSelectionZoneOnce(CallBackArg<PositionDimensions> callBack){
        this.callBack = callBack;

        selectionZone.setPrefWidth(0);
        selectionZone.setPrefHeight(0);
        selectionZone.setVisible(false);

        setOnMousePressed((e) -> {
            e.consume();
            if(e.getButton() != MouseButton.PRIMARY){
                end();
            }else{
                startX = e.getX();
                startY = e.getY();
                selectionZone.setLayoutX(startX);
                selectionZone.setLayoutY(startY);
                selectionZone.setPrefWidth(0);
                selectionZone.setPrefHeight(0);
                selectionZone.setVisible(true);
            }
        });

        setOnMouseDragged(this::updateSelectionPositionDimensions);

        setOnKeyPressed((e) -> {
            if(e.getCode() == KeyCode.ESCAPE) end();
        });

        setOnMouseReleased((e) -> {
            e.consume();
            updateSelectionPositionDimensions(e);
            end();
        });

    }
    private void updateSelectionPositionDimensions(MouseEvent e){
        if(startX < 0) startX = 0; if(startY < 0) startY = 0;

        double x = e.getX() > getWidth()+1 ? getWidth()+1 : (e.getX() < -1 ? -1 : e.getX());
        double y = e.getY() > getHeight()+1 ? getHeight()+1 : (e.getY() < -1 ? -1 : e.getY());
        double width = x - startX;
        double height = y - startY;

        if(width < 0) {
            width = -width;
            selectionZone.setLayoutX(startX-width);
        }
        if(height < 0){
            height = -height;
            selectionZone.setLayoutY(startY-height);
        }
        selectionZone.setPrefWidth(width);
        selectionZone.setPrefHeight(height);

        if(selectionZoneType == SelectionZoneType.PDF_ON_DARK && page.getBackground().getImages().size() >= 1){
            Image image = page.getBackground().getImages().get(0).getImage();
            BackgroundPosition backgroundPosition = new BackgroundPosition(Side.LEFT, -selectionZone.getLayoutX(), false, Side.TOP, -selectionZone.getLayoutY(), false);
            BackgroundSize backgroundSize = new BackgroundSize(page.getWidth(), page.getHeight(), false, false, false, false);
            BackgroundImage background = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, backgroundPosition, backgroundSize);
            selectionZone.setBackground(new Background(background));
        }
    }
    public PositionDimensions getSelectionPositionDimensions(){
        return new PositionDimensions(selectionZone.getWidth()-2, selectionZone.getHeight()-2, selectionZone.getLayoutX()+1, selectionZone.getLayoutY()+1);
    }
    private void end(){
        if(selectionZone.getWidth() > 10 && selectionZone.getHeight() > 10){
            callBack.call(getSelectionPositionDimensions());
        }
        setOnMousePressed(null);
        setOnMouseDragged(null);
        setOnMouseReleased(null);
        setOnKeyPressed(null);
        selectionZone.setVisible(false);
        setShow(false);
    }

    public void setShow(boolean visible){
        setVisible(visible);
        if(visible){
            toFront();
            requestFocus();
        }
    }
    public void setSelectionZoneType(SelectionZoneType type) {
        this.selectionZoneType = type;

        if(type == SelectionZoneType.LIGHT_ON_DARK){
            selectionZone.setBackground(new Background(new BackgroundFill(Color.rgb(255, 255, 255, 0.5), CornerRadii.EMPTY, Insets.EMPTY)));
            selectionZone.setBorder(new Border(new BorderStroke(Color.DARKGRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));
            setBackground(new Background(new BackgroundFill(Color.rgb(0, 0, 0, 0.8), CornerRadii.EMPTY, Insets.EMPTY)));

        }else if(type == SelectionZoneType.PDF_ON_DARK){
            Image image = page.getBackground().getImages().get(0).getImage();
            BackgroundPosition backgroundPosition = new BackgroundPosition(Side.LEFT, -startX, false, Side.TOP, -startY, false);
            BackgroundSize backgroundSize = new BackgroundSize(page.getWidth(), page.getHeight(), false, false, false, false);
            BackgroundImage background = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, backgroundPosition, backgroundSize);

            selectionZone.setBackground(new Background(background));
            selectionZone.setBorder(new Border(new BorderStroke(Color.DODGERBLUE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));
            setBackground(new Background(new BackgroundFill(Color.rgb(0, 0, 0, 0.6), CornerRadii.EMPTY, Insets.EMPTY)));
        }
    }

    public PageRenderer getPage() {
        return page;
    }
    public void setPage(PageRenderer page) {
        this.page = page;
    }
}
