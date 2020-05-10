package fr.themsou.panel.leftBar.texts;

import fr.themsou.document.editions.elements.Element;
import fr.themsou.yaml.Config;
import javafx.scene.control.TreeItem;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class TextListItem {

    private Font font;
    private String text;
    private Color color;

    private long uses;
    private long creationDate;

    public TextListItem(Font font, String text, Color color, long uses, long creationDate) {
        this.font = font;
        this.text = text;
        this.color = color;
        this.uses = uses;
        this.creationDate = creationDate;
    }

    public TextTreeItem toTextTreeItem(int type){
        return new TextTreeItem(font, text, color, type, uses, creationDate);
    }

    public void writeData(DataOutputStream writer) throws IOException {

        writer.writeFloat((float) font.getSize());
        writer.writeBoolean(Element.getFontWeight(font) == FontWeight.BOLD);
        writer.writeBoolean(Element.getFontPosture(font) == FontPosture.ITALIC);
        writer.writeUTF(font.getFamily());
        writer.writeByte((int) (color.getRed() * 255.0 - 128));
        writer.writeByte((int) (color.getGreen() * 255.0 - 128));
        writer.writeByte((int) (color.getBlue() * 255.0 - 128));
        writer.writeLong(uses);
        writer.writeLong(creationDate);
        writer.writeUTF(text);
    }
    public HashMap<Object, Object> getYAMLData(){
        HashMap<Object, Object> data = new HashMap<>();
        data.put("color", color.toString());
        data.put("font", font.getFamily());
        data.put("size", font.getSize());
        data.put("bold", Element.getFontWeight(font) == FontWeight.BOLD);
        data.put("italic", Element.getFontPosture(font) == FontPosture.ITALIC);
        data.put("uses", uses);
        data.put("date", creationDate);
        data.put("text", text);

        return data;
    }

    public static TextListItem readDataAndGive(DataInputStream reader) throws IOException {

        double fontSize = reader.readFloat();
        boolean isBold = reader.readBoolean();
        boolean isItalic = reader.readBoolean();
        String fontName = reader.readUTF();
        short colorRed = (short) (reader.readByte() + 128);
        short colorGreen = (short) (reader.readByte() + 128);
        short colorBlue = (short) (reader.readByte() + 128);
        long uses = reader.readLong();
        long creationDate = reader.readLong();
        String text = reader.readUTF();

        Font font = Element.getFont(fontName, isBold, isItalic, (int) fontSize);

        return new TextListItem(font, text, Color.rgb(colorRed, colorGreen, colorBlue), uses, creationDate);
    }
    public static TextListItem readYAMLDataAndGive(HashMap<String, Object> data){

        double fontSize = Config.getDouble(data, "size");
        boolean isBold = Config.getBoolean(data, "bold");
        boolean isItalic = Config.getBoolean(data, "italic");
        String fontName = Config.getString(data, "font");
        Color color = Color.valueOf(Config.getString(data, "color"));
        long uses = Config.getLong(data, "uses");
        long creationDate = Config.getLong(data, "date");
        String text = Config.getString(data, "text");

        Font font = Element.getFont(fontName, isBold, isItalic, (int) fontSize);

        return new TextListItem(font, text, color, uses, creationDate);
    }

        public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public long getUses() {
        return uses;
    }

    public void setUses(long uses) {
        this.uses = uses;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

}
