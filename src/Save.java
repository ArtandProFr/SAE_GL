
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;

public class Save{

    static String usernameBase = "Enter username here...";
    static String savenameBase = "Enter savename here...";
    static String difficultyBase = "Choose difficulty";
    static long lastSaveBase = -1;
    static long creationDateBase = -1;
    static double phaseBase = 0.0;
    static int timeBase = 0;
    static String alteration = "YOUMUSTNOTCHEAT";
    static int nb_ligne_save = 15;

    String username = usernameBase;
    String savename = savenameBase;
    String difficulty = difficultyBase;
    long creationDate = creationDateBase;
    long lastSave = lastSaveBase;
    double phase = phaseBase;
    int time = timeBase;
    int checksum = getChecksum();

    static char[] validCaract = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                          'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                          '_', '#', '@', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
    
    static String[] difficulties = {"Easy", "Normal", "Hard"};

    public static void reset(Save s){
        s.username = usernameBase;
        s.savename = savenameBase;
        s.difficulty = difficultyBase;
        s.creationDate = creationDateBase;
        s.lastSave = lastSaveBase;
        s.phase = phaseBase;
        s.time = timeBase;
        s.checksum = s.getChecksum();
    }

    public void reset(){
        reset(this);
    }

    private static int getChecksum(Save s){

        /* Cette méthode renvoie la chaine de caractère associée au checksum de la Save passé en paramètre. */

        String cs = "";
        cs += s.username;
        cs += s.savename;
        cs += s.difficulty;
        cs += String.valueOf(s.creationDate);
        cs += String.valueOf(s.lastSave);
        cs += String.valueOf(s.phase);
        cs += String.valueOf(s.time);
        cs += alteration;
        return cs.hashCode();
    }

    public int getChecksum(){
        
        /* Cette méthode renvoie la chaine de caractère associée au checksum de la Save. */

        return getChecksum(this);
    }

    public boolean initializeSave(){

        /* La méthode initialise une sauvegarde si elle est valide et n'existe pas déjà en la stockant dans le dossier Saves. Elle renvoie un booléen selon si la création de la sauvegarde a réussi ou non. */

        if (isValidToStart() && !alreadyExists()){
            creationDate = Time.now();
            lastSave = creationDate;
            phase = 0.1;
            time = 0;
            return save();
        }
        return false;
    }

    public static boolean isInitial(Save s){

        /* Cette méthode renvoie un booléen selon si la Save est à l'état initial. */

        return ((s.username.equals(usernameBase)) && (s.savename.equals(savenameBase)) && (s.difficulty.equals(difficultyBase)) && (s.creationDate == creationDateBase) && (s.lastSave == lastSaveBase) && (s.phase == phaseBase) && (s.time == timeBase) && (s.checksum == getChecksum(s)));
    }

    public boolean isInitial(){

        /* Cette méthode renvoie un booléen selon si la Save est à l'état initial. */

        return isInitial(this);
    }

    public static boolean isValid(Save s){

        /* Cette méthode renvoie si la partie est valide, intègre et sans triche. */
        /* ATTENTION : l'utilisation de LocalDateTime peut entraîner la suppression d'une sauvegarde créée dans un autre fuseau horaire ("retour dans le passé"). */

        boolean valid = s.isValidToStart() && !(s.creationDate == creationDateBase) && !(s.lastSave == lastSaveBase) && (s.phase >= 0.1); // Vérification cas non-initial
        if (valid){
            valid = (s.creationDate <= s.lastSave) && (s.lastSave <= Time.now()); // Vérification intégrité des DateTime
            if (valid){
                valid = (s.checksum == getChecksum(s));
            }
        }
        return valid;
    }

    public boolean isValid(){

        /* Cette méthode renvoie si la partie est valide, intègre et sans triche. */
        /* ATTENTION : l'utilisation de LocalDateTime peut entraîner la suppression d'une sauvegarde créée dans un autre fuseau horaire ("retour dans le passé"). */

        return isValid(this);
    }

    public boolean modifyUsername(String newUsername){

        /* La méthode modifie le pseudo et renvoie un booléen selon s'il est valide. */

        username = newUsername;
        return usernameIsValid();
    }

    public boolean modifySavename(String newSavename){

        /* La méthode modifie le nom de sauvegarde et renvoie un booléen selon s'il est valide. */

        savename = newSavename;
        return savenameIsValid();
    }

    public boolean modifyDifficulty(String newDifficulty){

        /* La méthode modifie le nom de sauvegarde et renvoie un booléen selon s'il est valide. */

        difficulty = newDifficulty;
        return difficultyIsValid();
    }

    public void showSave(){

        /* Cette méthode affiche les informations de la sauvegarde en console. Ne doit être utilisé que pour le développement. */

        System.out.println("Username : " + username);
        System.out.println("Savename : " + savename);
        System.out.println("Difficulty : " + difficulty);
        System.out.println("Creation date : " + Time.stringFromInstant(creationDate));
        System.out.println("Last save : " + Time.stringFromInstant(lastSave));
        System.out.println("Phase : " + phase);
        System.out.println("Time : " + time);
        System.out.println("Checksum : " + checksum);
        System.out.println("Est sauvegardée dans le dossier ? " + alreadyExists());
    }

    public boolean isValidToStart(){

        /* La méthode renvoie un booléen selon si le pseudo, le nom de sauvegarde et la difficulté est valide. */

        return usernameIsValid() && savenameIsValid() && difficultyIsValid();
    }
    
    public static boolean stringIsValid(String str){

        /* Cette méthode renvoie si une chaîne de caractère respecte les caractères valides ou non. */

        boolean isValidCaract;
        for (char c : str.toCharArray()){
            isValidCaract = false;
            for (char car : validCaract){
                if (car == c){
                    isValidCaract = true;
                    break;
                }
            }
            if (!isValidCaract){
                return false;
            }
        }
        return true;
    } 

    public boolean usernameIsValid(){

        /* La méthode renvoie un booléen selon si le pseudo est valide. */

        return stringIsValid(username);
    }

    public boolean savenameIsValid(){

        /* La méthode renvoie un booléen selon si le nom de sauvegarde est valide. */

        return stringIsValid(savename);
    }

    public boolean difficultyIsValid(){

    /* La méthode renvoie un booléen selon si la difficulté est valide. */

    for (String s : difficulties){
        if (s.equals(difficulty)){
            return true;
        }
    }
    return false;
    }

    public boolean alreadyExists(){

        /* La méthode renvoie un booléen selon si une sauvegarde (Pseudo-NomSauvegarde) existe déjà */

        String userDir = System.getProperty("user.dir");
        Path folder_path = Path.of(userDir, "Saves");
        File folder = new File(folder_path.toString());
        if (folder.exists()){
            Path file_path = Path.of(folder_path.toString(), username + "-" + savename + ".txt");
            File file = new File(file_path.toString());
            return file.exists();
        } else {
            return false;
        }
    }

    @SuppressWarnings("ConvertToTryWithResources")
    public boolean save(){

        /* La méthode sauvegarde la partie. ATTENTION : La méthode ne vérifie pas la validité des infos données. */

        String userDir = System.getProperty("user.dir");
        Path folder_path = Path.of(userDir, "Saves");
        File folder = new File(folder_path.toString());
        if (!folder.exists()){
            folder.mkdir();
        }
        Path file_path = Path.of(folder_path.toString(), username + "-" + savename + ".txt");
        File file = new File(file_path.toString());
        if (file.exists()){
            file.delete();
        }
        try {
            file.createNewFile();
        } catch (IOException e){
            return false;
        }
        try (FileWriter writer = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(writer);){
            lastSave = Time.now();
            bw.newLine();
            bw.write("WARNING : Any manual modification will PERMANENTLY DELETE the save.");
            bw.newLine();
            bw.newLine();
            bw.write("USERNAME >>> " + username);
            bw.newLine();
            bw.write("SAVENAME >>> " + savename);
            bw.newLine();
            bw.write("DIFFICULTY >>> " + difficulty);
            bw.newLine();
            bw.write("CREATION DATE >>> " + creationDate);
            bw.newLine();
            bw.write("LAST SAVE >>> " + lastSave);
            bw.newLine();
            bw.write("PHASE >>> " + String.valueOf(phase));
            bw.newLine();
            bw.write("TIME >>> " + Time.chronoToString(time));
            bw.newLine();
            bw.write("CHECKSUM >>> " + String.valueOf(getChecksum()));
            return true;
        } catch (IOException e){
            return false;
        }
    }

    public static boolean delete(Save s){

        /* La méthode supprime la sauvegarde si elle existe et renvoie un booléen selon si la suppression a été effectuée ou non. (Si la sauvegarde existait avant ou non.) */

        String userDir = System.getProperty("user.dir");
        Path folder_path = Path.of(userDir, "Saves");
        File folder = new File(folder_path.toString());
        if (!folder.exists()){
            folder.mkdir();
            return false;
        }
        Path file_path = Path.of(folder_path.toString(), s.username + "-" + s.savename + ".txt");
        File file = new File(file_path.toString());
        if (file.exists()){
            file.delete();
            return true;
        }
        return false;
    }

    public boolean delete(){

        /* La méthode supprime la sauvegarde si elle existe et renvoie un booléen selon si la suppression a été effectuée ou non. (Si la sauvegarde existait avant ou non.) */

        return delete(this);
    }

    public static Save getSave(String givenUsername, String givenSavename){
        
        /* Cette méthode renvoie un objet Save initialisé à partir d'un nom d'utilisateur et un nom de sauvegarde. Supprime la sauvegarde et reset si non-intègre. */

        String fileName = givenUsername + "-" + givenSavename;
        return getSave(fileName);
    }

    public static Save getSave(String fileName){

        /* Cette méthode renvoie un objet Save initialisé à partir d'un nom de fichier. Supprime la sauvegarde et reset si non-intègre. */

        if (!fileName.endsWith(".txt")){
            fileName += ".txt";
        }
        Save s = new Save();
        String userDir = System.getProperty("user.dir");
        Path folder_path = Path.of(userDir, "Saves");
        File folder = new File(folder_path.toString());
        if (!folder.exists()){
            folder.mkdir();
        }
        Path file_path = Path.of(folder_path.toString(), fileName);
        File file = new File(file_path.toString());
        if (file.exists()){
            try {
                String[] doc;
                int i;
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
                    doc = new String[nb_ligne_save];
                    i = 0;
                    String line = reader.readLine();
                    while (line != null){
                        doc[i] = line;
                        line = reader.readLine();
                        i++;
                    }
                }
                String key;
                String elem;
                String[] arr;
                String l;
                for (int j = 0; j < i; j++){
                    l = doc[j];
                    arr = l.split(">>>");
                    key = arr[0].replace(" ", "");
                    if (arr.length > 1){
                        elem = arr[1].replace(" - ", ">>>").replace(" ", "").replace(">>>", " - ");
                        switch (key){
                            case "USERNAME" -> s.username = elem;
                            case "SAVENAME" -> s.savename = elem;
                            case "DIFFICULTY" -> s.difficulty = elem;
                            case "CREATIONDATE" -> s.creationDate = Long.parseLong(elem);
                            case "LASTSAVE" -> s.lastSave = Long.parseLong(elem);
                            case "PHASE" -> s.phase = Double.parseDouble(elem);
                            case "TIME" -> s.time = Time.chronoToInt(elem);
                            case "CHECKSUM" -> s.checksum = Integer.parseInt(elem);
                            default -> {
                            }
                        }
                    }
                }
                if (!s.isValid()){
                    file.delete();
                    s.reset();
                }
            } catch (IOException e) {
                if (!s.isValid()){
                    file.delete();
                    s.reset();
                }
            }
        }
        return s;
    }

    public boolean initSaveFromFile(){

        /* Cette méthode initialise toute les infos d'une partie sauvegardée. Renvoie un booléen selon si la partie existe ou non. Supprime la sauvegarde et reset si non-intègre. */

        String fileName = username + "-" + savename;
        String userDir = System.getProperty("user.dir");
        Path folder_path = Path.of(userDir, "Saves");
        File folder = new File(folder_path.toString());
        if (!folder.exists()){
            folder.mkdir();
        }
        Path file_path = Path.of(folder_path.toString(), fileName + ".txt");
        File file = new File(file_path.toString());
        if (file.exists()){
            try {
                String[] doc;
                int i;
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
                    doc = new String[nb_ligne_save]; // Pour l'instant 7 lignes suffisent, peut-être qu'une 8eme sera à ajouter pour l'intégrité
                    i = 0;
                    String line = reader.readLine();
                    while (line != null){
                        doc[i] = line;
                        line = reader.readLine();
                        i++;
                    }
                } 
                String key;
                String elem;
                String[] arr;
                String l;
                for (int j = 0; j < i; j++){
                    l = doc[j];
                    arr = l.split(">>>");
                    key = arr[0].replace(" ", "");
                    if (arr.length > 1){
                        elem = arr[1].replace(" - ", ">>>").replace(" ", "").replace(">>>", " - ");
                        switch (key){
                            case "USERNAME" -> username = elem;
                            case "SAVENAME" -> savename = elem;
                            case "DIFFICULTY" -> difficulty = elem;
                            case "CREATIONDATE" -> creationDate = Long.parseLong(elem);
                            case "LASTSAVE" -> lastSave = Long.parseLong(elem);
                            case "PHASE" -> phase = Double.parseDouble(elem);
                            case "TIME" -> time = Time.chronoToInt(elem);
                            case "CHECKSUM" -> checksum = Integer.parseInt(elem);
                            default -> {
                            }
                        }
                    }
                }
                return true;
            } catch (IOException e) {
                if (!isValid()){
                    file.delete();
                    reset();
                    return false;
                }
            }
            if (!isValid()){
                file.delete();
                reset();
                return false;
            }
        }
        return false;
    }

    public static Save[] getAllSaves(){

        /* Cette méthode renvoie un tableau de toutes les parties sauvegardées. Supprime les parties non-valides / non-intègres. Supprime les sauvegardes si non-intègres.*/
        
        String userDir = System.getProperty("user.dir");
        Path folder_path = Path.of(userDir, "Saves");
        File folder = new File(folder_path.toString());
        if (!folder.exists()){
            folder.mkdir();
        }
        String[] listeStr = folder.list();
        Save[] liste = new Save[listeStr.length];
        int count = 0;
        for (int i = 0; i < liste.length; i++){
            Save s = getSave(listeStr[i]);
            if (!isInitial(s)){
                count += 1;
            }
        }
        Save[] finalListe = new Save[count];
        int decal = 0;
        for (int i = 0; i < liste.length; i++){
            Save s = getSave(listeStr[i]);
            if (!s.isInitial()){
                finalListe[decal] = s;
                decal++;
            }
        }
        return finalListe;
    }

    public static Save[] playerFilter(Save[] arr, String userFilter){

        /* Cette méthode renvoie une liste de Save filtrée par nom de joueur. */

        int count = 0;
        for (Save s : arr){
            if (s.username.equals(userFilter)){
                count++;
            }
        }
        Save[] f = new Save[count];
        int decal = 0;
        for (Save s : arr){
            if (s.username.equals(userFilter)){
                f[decal] = s;
                decal++;
            }
        }
        return f;
    }

    public static Save[] difficultyFilter(Save[] arr, String diffFilter){

        /* Cette méthode renvoie une liste de Save filtrée par difficulté. */

        int count = 0;
        for (Save s : arr){
            if (s.difficulty.equals(diffFilter)){
                count++;
            }
        }
        Save[] f = new Save[count];
        int decal = 0;
        for (Save s : arr){
            if (s.difficulty.equals(diffFilter)){
                f[decal] = s;
                decal++;
            }
        }
        return f;
    }

    private static void exchange(Save[] arr, int i, int j){

        /* Cette méthode échange 2 éléments d'un tableau. */

        Save temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }

    private static int compareLastSave(Save[] arr, int i, int j, int type){
        if (arr[i].lastSave <= arr[j].lastSave){
            return type * -1;
        } else {
            return type * 1;
        }
    }

    private static void quicksortLastSave(Save[] arr, int debut, int fin, int type){

        /* Cette méthode est une itération de l'algorithme Quicksort appliqué à un tableau de save sur le paramètre LastSave. */

        if (debut < fin){
            int pivot = fin;
            int nb_small = debut;
            int c;
            for (int i = debut; i < fin; i++){
                c = compareLastSave(arr, i, pivot, type);
                if (c < 0){
                    exchange(arr, i, nb_small);
                    nb_small++;
                }
            }
            exchange(arr, nb_small, pivot);
            quicksortLastSave(arr, debut, nb_small-1, type);
            quicksortLastSave(arr, nb_small+1, fin, type);
        }
    }

    public static void lastSaveOrder(Save[] arr, int type){

        /* Cette méthode tri le tableau de Save selon la dernière sauvegarde. */

        if (arr.length > 1){
            quicksortLastSave(arr, 0, arr.length - 1, type);
        }
    }

    public static void main(String[] args){
        Save[] l = getAllSaves();
        for (Save s : l){
            s.showSave();
        }
    }
}