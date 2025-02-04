package fr.clementgre.pdf4teachers.datasaving;

import fr.clementgre.pdf4teachers.utils.StringUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

public class Config {

    public HashMap<String, Object> base = new HashMap<>();

    private Yaml yaml;
    private File file;
    private File destFile;
    private String name;
    public Config(){
        setupYAML();
    }
    public Config(File file) throws IOException{
        file.createNewFile();
        this.file = file;
        setupYAML();
    }

    private void setupYAML(){
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setWidth(120);
        yaml = new Yaml(new SafeConstructor());
    }

    public void load() throws IOException {
        if(file == null) return;
        InputStream input = new FileInputStream(file);
        base = yaml.load(input);
        input.close();

        if(base == null) base = new HashMap<>();
    }
    public void save() throws IOException {
        if(file == null) return;
        Writer output = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
        yaml.dump(base, output);
        output.close();
    }
    public void saveTo(File file) throws IOException {
        Writer output = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
        yaml.dump(base, output);
        output.close();
    }
    public void saveToDestFile() throws IOException {
        if(destFile == null) return;
        Writer output = new OutputStreamWriter(new FileOutputStream(destFile), StandardCharsets.UTF_8);
        yaml.dump(base, output);
        output.close();
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public File getFile() {
        return file;
    }
    public void setFile(File file) {
        this.file = file;
    }
    public File getDestFile() {
        return destFile;
    }
    public void setDestFile(File destFile) {
        this.destFile = destFile;
    }

    // GET SECTION / CASTS

    public static ArrayList<Object> castList(Object list){
        if(list instanceof List) return (ArrayList<Object>) list;
        return new ArrayList<>();
    }
    public static HashMap<String, Object> castSection(Object list){
        if(list instanceof Map) return (HashMap<String, Object>) list;
        return new HashMap<>();
    }

    public static Object getValue(HashMap<String, Object> base, String path){

        String[] splitedPath = StringUtils.cleanArray(path.split(Pattern.quote(".")));
        HashMap<String, Object> section = base;
        int i = splitedPath.length;

        for(String key : splitedPath){
            if(section.containsKey(key)){ // Key exist
                Object value = section.get(key);
                if(value == null) return "";
                else if(i == 1) return value; // Value is a value or this is the last iteration : return value
                else if(!(section.get(key) instanceof Map)) return "";
                else section = (HashMap<String, Object>) value; // Continue loop
                i--;
            }else{
                return "";
            }
        }
        System.err.println("WARNING: for loop return anything"); return "";
    }

    // GET VALUE

    public String getString(String path){
        return getString(base, path);
    }
    public long getLong(String path){
        return getLong(base, path);
    }
    public Long getLongNull(String path){
        return getLongNull(base, path);
    }
    public double getDouble(String path){
        return getDouble(base, path);
    }
    public Double getDoubleNull(String path){
        return getDoubleNull(base, path);
    }
    public boolean getBoolean(String path){
        return getBoolean(base, path);
    }
    public Boolean getBooleanNull(String path){
        return getBooleanNull(base, path);
    }
    public ArrayList<Object> getList(String path){
        return getList(base, path);
    }
    public ArrayList<Object> getListNull(String path){
        return getListNull(base, path);
    }

    public static String getString(HashMap<String, Object> base, String path){
        return getValue(base, path).toString();
    }
    public static long getLong(HashMap<String, Object> base, String path){
        return StringUtils.getAlwaysLong(getValue(base, path).toString());
    }
    public static Long getLongNull(HashMap<String, Object> base, String path){
        return StringUtils.getLong(getValue(base, path).toString());
    }
    public static double getDouble(HashMap<String, Object> base, String path){
        return StringUtils.getAlwaysDouble(getValue(base, path).toString());
    }
    public static Double getDoubleNull(HashMap<String, Object> base, String path){
        return StringUtils.getDouble(getValue(base, path).toString());
    }
    public static boolean getBoolean(HashMap<String, Object> base, String path){
        return Boolean.parseBoolean(getValue(base, path).toString());
    }
    public static Boolean getBooleanNull(HashMap<String, Object> base, String path){
        return StringUtils.getBoolean(getValue(base, path).toString());
    }
    public static ArrayList<Object> getList(HashMap<String, Object> base, String path){
        Object value = getValue(base, path);
        if(value instanceof List) return (ArrayList<Object>) value;
        return new ArrayList<>();
    }
    public static ArrayList<Object> getListNull(HashMap<String, Object> base, String path){
        Object value = getValue(base, path);
        if(value instanceof List) return (ArrayList<Object>) value;
        return null;
    }

    // SET VALUE
    public void set(String path, Object value){
        set(base, path, value);
    }
    public static void set(HashMap<String, Object> base, String path, Object value){
        createSectionAndSet(base, path, value);
    }

    // GET KEY (SECTION)

    public HashMap<String, Object> getSectionSecure(String path){
        createSection(path);
        return getSection(base, path);
    }
    public static HashMap<String, Object> getSectionSecure(HashMap<String, Object> base, String path){
        createSection(base, path);
        return getSection(base, path);
    }
    public HashMap<String, Object> getSection(String path){
        return getSection(base, path);
    }
    public LinkedHashMap<String, Object> getLinkedSection(String path){
        return getLinkedSection(base, path);
    }
    public static HashMap<String, Object> getSection(HashMap<String, Object> base, String path){
        Object value = getValue(base, path);
        if(value instanceof Map) return (HashMap<String, Object>) value;
        return new HashMap<>();
    }
    public static LinkedHashMap<String, Object> getLinkedSection(HashMap<String, Object> base, String path){
        Object value = getValue(base, path);
        if(value instanceof Map) return (LinkedHashMap<String, Object>) value;
        return new LinkedHashMap<>();
    }
    public static HashMap<String, Object> getSectionNull(HashMap<String, Object> base, String path){
        Object value = getValue(base, path);
        if(value instanceof Map) return (HashMap<String, Object>) value;
        return null;
    }

    // SET KEY (CREATE SECTION)

    public void createSection(String path){
        createSection(base, path);
    }
    public static void createSection(HashMap<String, Object> base, String path){
        String[] splitedPath = StringUtils.cleanArray(path.split(Pattern.quote(".")));

        HashMap<String, Object> section = base;
        for(String key : splitedPath){
            if(!section.containsKey(key) || !(section.get(key) instanceof Map)){ // section does not exist : Create section
                HashMap<String, Object> value = new HashMap<>();
                section.put(key, value);
                section = value;
            }else{ // use existing section
                section = (HashMap<String, Object>) section.get(key);
            }
        }
    }
    public static void createSectionAndSet(HashMap<String, Object> base, String path, Object value){
        String[] splitedPath = StringUtils.cleanArray(path.split(Pattern.quote(".")));

        HashMap<String, Object> section = base;
        int i = splitedPath.length;
        for(String key : splitedPath){
            if(i == 1){
                section.put(key, value);
            }else if(!section.containsKey(key) || !(section.get(key) instanceof Map)){ // section does not exist : Create section
                HashMap<String, Object> newSection = new HashMap<>();
                section.put(key, newSection);
                section = newSection;
            }else{ // use existing section
                section = (HashMap<String, Object>) section.get(key);
            }
            i--;
        }
    }

    // CHECK EXIST SECTION

    public boolean exist(String path){
        return exist(base, path);
    }
    public static boolean exist(HashMap<String, Object> base, String path){
        String[] splitedPath = StringUtils.cleanArray(path.split(Pattern.quote(".")));

        HashMap<String, Object> section = base;
        for(String key : splitedPath){
            if(!section.containsKey(key) || !(section.get(key) instanceof Map)){ // section does not exist
                return false;
            }else{ // use existing section to continue loop
                section = (HashMap<String, Object>) section.get(key);
            }
        }
        return true;
    }
}
