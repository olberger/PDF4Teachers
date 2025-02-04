package fr.clementgre.pdf4teachers.utils;

import java.io.File;
import java.util.regex.Pattern;

public class FilesUtils {

    public static long getSize(File f) {
        if(f.isFile()){
            return (f.length());
        }

        // DIR

        File[] files = f.listFiles();
        if(files == null) return 0;

        long x = 0L;
        for(int i = 0; i < files.length ; i++){
            x += getSize(files[i]);
        }
        return (x);
    }

    public static float convertOctetToMo(long octet){

        return (float) (octet / 1000) / 1000f;

    }

    public static String getExtension(String fileName){
        String[] splitted = fileName.split(Pattern.quote("."));
        if(splitted.length == 0 || splitted.length == 1) return "";
        return splitted[splitted.length-1];
    }

    public static boolean isInSameDir(File file1, File file2){
        return file1.getParentFile().getAbsolutePath().equals(file2.getParentFile().getAbsolutePath());
    }

    public static String getPathReplacingUserHome(File file){
        return getPathReplacingUserHome(file.getAbsolutePath());
    }
    public static String getPathReplacingUserHome(String path){
        if(path.startsWith(System.getProperty("user.home"))){
            return path.replaceFirst(Pattern.quote(System.getProperty("user.home")), "~");
        }else return path;
    }
}
