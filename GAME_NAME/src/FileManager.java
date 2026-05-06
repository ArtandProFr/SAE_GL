
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

public class FileManager {

    public static final Path userDir = Path.of(System.getProperty("user.dir"));

    public String path = "";
    public File file = null;

    public static String toString(Path p){

        /* Renvoie le chemin sous forme de chaine de caractère. */

        return p.toString();
    }

    public static File getFile(Path p){

        /* Renvoie un fichier à partir d'un chemin. */

        return getFile(toString(p));
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

        setFile(toString(p));
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

    public boolean isGameFile(){

        /* Cette méthode renvoie un booléen selon si le document est un fichier du jeu. */

        if (!file.exists() || !isFile()){
            return false;
        }
        return readFile(file.toPath()).contains(GameInfos.GAMENAME);
    }

    public void delete(){
        
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

    public static void createFolder(Path folderPath) {

        /* Cette méthode crée un dossier si celui-ci n'existe pas. */

        try {
            Files.createDirectories(folderPath);
        } catch (IOException e) {
        }
    }

    public static void createFolder(File f){
        
        /* Cette méthode crée un dossier si celui-ci n'existe pas. */
        
        if (isFolder(f) && !exists(f)){
            f.mkdir();
        }
    }

    public static void createFile(File f){

        /* Cette méthode crée un fichier si celui-ci n'existe pas. */

        if (isFile(f) && !exists(f)){
            try {
                f.createNewFile();
            } catch (IOException e){
                
            }
        }
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