
/*
        GAME_NAME
        
        Etablissement : INSA Hauts-de-France
        Formation : Sciences et Humanités pour l'Ingénieur (SHpI)
        Année d'étude : 2A
        Module : SAE Génie Logiciel / Responsable : M. Kolski
        Encadrement : M. Benameur, M. Boudia, M. Kolski, Mme Mourali
        Année scolaire : 2025 - 2026

        Groupe 2:
        DWORNICZAK Arthur
        GAMON Thomas
        OUERHANI Farès
        PERROT Roxane
 */

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Set;

public class FileManager {

    public static final Path userDir = Path.of(System.getProperty("user.dir"));
    public static final Set<String> ImageExtensions = Set.of(".png", ".jpg",".jpeg",".apng",".avif",".gif",".svg",".webp",".bmp",".ico",".tiff",".odi");;
    public static final Set<String> TextExtensions = Set.of(".odm",".odt",".doc",".docx",".txt",".rtf");
    public static final Set<String> DataExtensions = Set.of(".csv",".ods",".odb",".json",".sql");
    public static final Set<String> LogExtensions = Set.of(".log", ".git");
    public static final Set<String> ScriptExtensions = Set.of(".py",".c",".java",".js",".mjs",".sql",".sh",".zsh",".ps1",".rb",".php",".lua",".html",".r",".cpp",".cs",".ts",".tsx",".swift",".dart",".go");
    public static final Set<String> ExecutableExtensions = Set.of(".bin",".elf",".exe",".sdc",".bat");
    public static final Set<String> PageExtensions = Set.of(".pdf",".ps",".html",".xhtml",".xml",".php");
    public static final Set<String> SoundExtensions = Set.of(".flac",".mp3",".wav",".ogg",".wma",".aac");
    public static final Set<String> VideoExtensions = Set.of(".mpeg",".avi",".mp4",".flv",".ogm");

    public String path = "";
    public File file = null;

    public static String pathToString(Path p){

        /* Renvoie le chemin sous forme de chaine de caractère. */

        return p.toString();
    }

    public static File getFile(Path p){

        /* Renvoie un fichier à partir d'un chemin. */

        return getFile(pathToString(p));
    }

    public static File getFile(String s){

        /* Renvoie un fichier à partir d'un chemin. */

        return new File(s);
    }

    public void setFile(String s){

        /* Cette méthode initialise un fichier/dossier à l'aide du chemin. */

        file = getFile(s);
    }

    public void setFile(Path p){

        /* Cette méthode initialise un fichier/dossier à l'aide du chemin. */

        setFile(pathToString(p));
    }

    public static boolean isFile(File f){

        /* Cette méthode renvoie un booléen selon si le fichier/dossier est un fichier. */

        return f.isFile();
    }

    public boolean isFile(){

        /* Cette méthode renvoie un booléen selon si le fichier/dossier est un fichier. */

        return isFile(file);
    }

    public static boolean isFolder(File f){

        /* Cette méthode renvoie un booléen selon si le fichier/dossier est un dossier. */

        return !f.isFile();
    }

    public boolean isFolder(){

        /* Cette méthode renvoie un booléen selon si le fichier/dossier est un dossier. */

        return !isFile();
    }

    public static String getExtension(File f){

        /* Cette méthode renvoie l'extension d'un fichier. */

        if (f.exists()){
            String p = pathToString(f.toPath());
            String[] arr = p.split("/");
            p = arr[arr.length-1];
            arr = p.split("\\.");
            if (arr.length > 1){
                return arr[1];
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public boolean isGameFile(File f){

        /* Cette méthode renvoie un booléen selon si le document est un fichier du jeu. */

        if (!f.exists() || !f.isFile()){
            return false;
        }
        String ext = getExtension(f);
        if (TextExtensions.contains(ext) || ScriptExtensions.contains(ext)){
            return readFile(f.toPath()).contains(GameInfos.GAMENAME);
        } else return ImageExtensions.contains(ext) || VideoExtensions.contains(ext) || SoundExtensions.contains(ext);
    }

    public boolean delete(FileManager f){

        /* Cette méthode supprime le fichier associé au FileManager et renvoie un booléen selon s'il existait ou non. */

        return f.file.delete();
    }

    public static Path findRelative(Path source, String relative){

        /* Cette méthode renvoie le chemin vers un fichier à partir du chemin source et du chemin relatif. */

        return source.resolve(relative);
    }

    public static Path findRelativeFromUserDir(String relative){

        /* Cette méthode renvoie le chemin vers un fichier à partir du chemin relatif entre le dossier utilisateur actif et le fichier recherché. */

        return findRelative(userDir, relative);
    }

    public static String readFile(Path path) {

        /* Cette méthode lit un fichier et retourne son contenu sous forme de String avec les sauts de ligne. */

        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }

    public static boolean writeFile(Path path, String content) {

        /* Cette méthode écrit (ou remplace) le contenu d'un fichier. */

        try {
            // Crée les dossiers parents s'ils n'existent pas
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            Files.writeString(path, content, StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static void appendToFile(Path path, String content) {

        /* Cette méthode ajoute du texte à la fin d'un fichier existant. */

        try {
            Files.writeString(path, content, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        } catch (IOException e) {
        }
    }

    public static boolean createFolder(Path folderPath) {

        /* Cette méthode crée un dossier si celui-ci n'existe pas. */

        try {
            Files.createDirectories(folderPath);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean createFolder(File f){
        
        /* Cette méthode crée un dossier si celui-ci n'existe pas. */
        
        if (isFolder(f) && !exists(f)){
            return f.mkdir();
        }
        return false;
    }

    public static boolean createFile(File f){

        /* Cette méthode crée un fichier si celui-ci n'existe pas. */

        if (isFile(f) && !exists(f)){
            try {
                return f.createNewFile();
            } catch (IOException e){
            }
        }
        return false;
    }

    public static boolean exists(File f){

        /* Cette méthode renvoie un booléen selon si un fichier/dossier existe. */

        return f.exists();
    }

    public static boolean exists(Path path) {

        /* Cette méthode renvoie un booléen selon si un élément existe. */

        return Files.exists(path);
    }
}