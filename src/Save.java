
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

public class Save{

    static String usernameBase = "Enter username here...";
    static String savenameBase = "Enter savename here...";
    static String difficultyBase = "Choose difficulty";
    static long lastSaveBase = -1;
    static long creationDateBase = -1;
    static double phaseBase = 0.0;
    static int timeBase = 0;
    static String alteration = "YOUMUSTNOTCHEAT";

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

    public static boolean initializeSave(Save s){

        /* La méthode initialise une sauvegarde si elle est valide et n'existe pas déjà en la stockant dans le dossier Saves. Elle renvoie un booléen selon si la création de la sauvegarde a réussi ou non. */

        if (s.isValidToStart() && !s.alreadyExists()){
            s.creationDate = Time.now();
            s.lastSave = s.creationDate;
            s.phase = 0.1;
            s.time = 0;
            return s.save();
        }
        return false;
    }

    public boolean initializeSave(){

        /* La méthode initialise une sauvegarde si elle est valide et n'existe pas déjà en la stockant dans le dossier Saves. Elle renvoie un booléen selon si la création de la sauvegarde a réussi ou non. */

        return initializeSave(this);
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

    public static boolean isValidToStart(Save s){

        /* La méthode renvoie un booléen selon si le pseudo, le nom de sauvegarde et la difficulté est valide. */

        return s.usernameIsValid() && s.savenameIsValid() && s.difficultyIsValid();
    }

    public boolean isValidToStart(){

        /* La méthode renvoie un booléen selon si le pseudo, le nom de sauvegarde et la difficulté est valide. */

        return isValidToStart(this);
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

    public static boolean usernameIsValid(Save s){

        /* La méthode renvoie un booléen selon si le pseudo est valide. */

        return stringIsValid(s.username) && !s.username.equals("USERNAME");
    }

    public static boolean savenameIsValid(Save s){

        /* La méthode renvoie un booléen selon si le nom de sauvegarde est valide. */

        return stringIsValid(s.savename) && !s.savename.equals("SAVENAME");
    }

    public static boolean difficultyIsValid(Save s){

    /* La méthode renvoie un booléen selon si la difficulté est valide. */

    for (String str : difficulties){
        if (str.equals(s.difficulty)){
            return true;
        }
    }
    return false;
    }

    public boolean usernameIsValid(){

        /* La méthode renvoie un booléen selon si le pseudo est valide. */

        return usernameIsValid(this);
    }

    public boolean savenameIsValid(){

        /* La méthode renvoie un booléen selon si le nom de sauvegarde est valide. */

        return savenameIsValid(this);
    }

    public boolean difficultyIsValid(){

    /* La méthode renvoie un booléen selon si la difficulté est valide. */

    return difficultyIsValid(this);
    }

    public static boolean alreadyExists(Save s){

        /* La méthode renvoie un booléen selon si une sauvegarde (Pseudo-NomSauvegarde) existe déjà */

        File folder = new File (FileManager.findRelativeFromUserDir("Saves").toString());
        if (folder.exists()){
            File file = new File(FileManager.findRelative(folder.toPath(), s.username + "-" + s.savename + ".txt").toString());
            return file.exists();
        } else {
            return false;
        }
    }

    public boolean alreadyExists(){
        
        /* La méthode renvoie un booléen selon si une sauvegarde (Pseudo-NomSauvegarde) existe déjà */

        return alreadyExists(this);
    }

    public static boolean save(Save s){

        /* La méthode sauvegarde la partie. ATTENTION : La méthode ne vérifie pas la validité des infos données. */

        File folder = new File (FileManager.findRelativeFromUserDir("Saves").toString());
        if (!folder.exists()){
            folder.mkdir();
        }
        File file = new File(FileManager.findRelative(folder.toPath(), s.username + "-" + s.savename + ".txt").toString());
        if (file.exists()){
            file.delete();
        }
        if (!FileManager.createFile(file)){
            return false;
        }
        s.lastSave = Time.now();
        String doc = "";
        doc += "\n"+GameInfos.GAMENAME+"\n\n";
        doc += "WARNING : Any manual modification will PERMANENTLY DELETE the save.\n\n";
        doc += "USERNAME >>> " + s.username + "\n";
        doc += "SAVENAME >>> " + s.savename + "\n";
        doc += "DIFFICULTY >>> " + s.difficulty + "\n";
        doc += "CREATION DATE >>> " + s.creationDate + "\n";
        doc += "LAST SAVE >>> " + s.lastSave + "\n";
        doc += "PHASE >>> " + String.valueOf(s.phase) + "\n";
        doc += "TIME >>> " + Time.chronoToString(s.time) + "\n";
        doc += "CHECKSUM >>> " + String.valueOf(s.getChecksum());
        return FileManager.writeFile(file.toPath(), doc);
    }

    public boolean save(){

        /* La méthode sauvegarde la partie. ATTENTION : La méthode ne vérifie pas la validité des infos données. */

        return save(this);
    }

    public static boolean delete(Save s){

        /* La méthode supprime la sauvegarde si elle existe et renvoie un booléen selon si la suppression a été effectuée ou non. (Si la sauvegarde existait avant ou non.) */

        File folder = new File (FileManager.findRelativeFromUserDir("Saves").toString());
        if (!folder.exists()){
            folder.mkdir();
            return false;
        }
        File file = new File(FileManager.findRelative(folder.toPath(), s.username + "-" + s.savename + ".txt").toString());
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

    public static boolean clearSaves(){
        for (Save s : getAllSaves()){
            s.delete();
        }
        return (getAllSaves() == null || getAllSaves().length == 0);
    }

    public static Save getSave(String givenUsername, String givenSavename){
        
        /* Cette méthode renvoie un objet Save initialisé à partir d'un nom d'utilisateur et un nom de sauvegarde. Supprime la sauvegarde et reset si non-intègre. */

        String fileName = givenUsername + "-" + givenSavename;
        return getSave(fileName);
    }

    public static Save getSave(String fileName){

        /* Cette méthode renvoie un objet Save initialisé à partir d'un nom de fichier. Supprime la sauvegarde et reset si non-intègre. */

        if (!fileName.contains(".")){
            fileName += ".txt";
        }
        File folder = new File (FileManager.findRelativeFromUserDir("Saves").toString());
        FileManager.createFolder(folder);
        File file = new File(FileManager.findRelative(folder.toPath(), fileName).toString());
        Save s = new Save();
        if (file.exists() && FileManager.isGameFile(file) && file.toPath().endsWith(".txt") && !fileName.equals("scoreboard.txt") && fileName.contains("-")){
            String[] arr = fileName.split("/");
            String str = arr[arr.length-1];
            arr = str.split("\\.");
            str = arr[0];
            arr = str.split("-");
            String user = arr[0];
            String save = arr[1];
            String docStr = FileManager.readFile(file.toPath());
            if (docStr != null){
                String[] doc = docStr.split("\n");
                String key;
                String elem;
                for (String l : doc){
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
                    if (!user.equals(s.username) || !save.equals(s.savename)){
                        file.delete();
                        s.reset();
                    }
                }
                if (!s.isValid()){
                    file.delete();
                    s.reset();
                }
            } else {
                if (!s.isValid()){
                    file.delete();
                    s.reset();
                }
            }
        }
        if (s.isInitial()){
            s = null;
        }
        return s;
    }

    public static boolean initSaveFromFile(Save s){

        /* Cette méthode initialise toute les infos d'une partie sauvegardée. Renvoie un booléen selon si la partie existe ou non. Supprime la sauvegarde et reset si non-intègre. */

        String fileName = s.username + "-" + s.savename + ".txt";
        String user = s.username;
        String save = s.savename;
        File folder = new File (FileManager.findRelativeFromUserDir("Saves").toString());
        if (!folder.exists()){
            FileManager.createFolder(folder);
        }
        File file = new File(FileManager.findRelative(folder.toPath(), fileName).toString());
        if (file.exists()){
            String docStr = FileManager.readFile(file.toPath());
            if (docStr != null){
                String[] doc = docStr.split("\n");
                String key;
                String elem;
                String[] arr;
                for (String l : doc){
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
                if (!user.equals(s.username) || !save.equals(s.savename)){
                    file.delete();
                    s.reset();
                    return false;
                }
                return true;
            } else {
                if (!s.isValid()){
                    file.delete();
                    s.reset();
                    return false;
                }
            }
            if (!s.isValid()){
                file.delete();
                s.reset();
                return false;
            }
        }
        return false;
    }

    public boolean initializeSaveFromFile(){

        /* Cette méthode initialise toute les infos d'une partie sauvegardée. Renvoie un booléen selon si la partie existe ou non. Supprime la sauvegarde et reset si non-intègre. */

        return initSaveFromFile(this);
    }

    public static Save[] getAllSaves(){

        /* Cette méthode renvoie un tableau de toutes les parties sauvegardées. Supprime les parties non-valides / non-intègres. Supprime les sauvegardes si non-intègres.*/
        
        File folder = new File (FileManager.findRelativeFromUserDir("Saves").toString());
        if (!folder.exists()){
            FileManager.createFolder(folder);
        }
        String[] listeStr = folder.list();
        Save[] liste = new Save[listeStr.length];
        int count = 0;
        for (int i = 0; i < liste.length; i++){
            Save s = getSave(listeStr[i]);
            if (s != null && !isInitial(s)){
                count += 1;
            }
        }
        Save[] finalListe = new Save[count];
        int decal = 0;
        for (int i = 0; i < liste.length; i++){
            Save s = getSave(listeStr[i]);
            if (s != null && !s.isInitial()){
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

    public static boolean createScoreboard(boolean rewrite){

        /* Cette méthode crée le fichier leaderboard.csv. */

        File folder = new File (FileManager.findRelativeFromUserDir("Saves").toString());
        if (!folder.exists()){
            FileManager.createFolder(folder);
        }
        File file = new File(FileManager.findRelative(folder.toPath(), "scoreboard.txt").toString());
        if (!file.exists() || rewrite){
            if (FileManager.createFile(file)){
                String comment = "#" + GameInfos.GAMENAME+"\n";
                String labels = "USERNAME;DIFFICULTY;TIME;DATE;CHECKSUM\n";
                FileManager.writeFile(file.toPath(), comment+labels);
                return true;
            }
        }
        return false;
    }

    private static boolean createScoreboard(){
        
        /* Cette méthode crée le fichier leaderboard.csv. */

        return createScoreboard(false);
    }

    private static long getChecksumScoreboard(String user, String diff, int t, long d){

        /* Cette méthode renvoie le checksum d'une ligne du scoreboard. */

        String cs = "";
        cs += user;
        cs += diff;
        cs += String.valueOf(t);
        cs += String.valueOf(d);
        cs += alteration;
        return cs.hashCode();
    }

    public static String[] getScoreboard(){

        /* Cette méthode renvoie le scoreboard sous forme de liste de chaine de caractere; */

        File folder = new File (FileManager.findRelativeFromUserDir("Saves").toString());
        if (!folder.exists()){
            FileManager.createFolder(folder);
        } else {
            createScoreboard();
            return null;
        }
        File file = new File(FileManager.findRelative(folder.toPath(), "scoreboard.txt").toString());
        if (file.exists()){
            checkIntegrityScoreboard();
            String docStr = FileManager.readFile(file.toPath());
            String[] docCplt = docStr.split("\n");
            int nb_lig = 0;
            for (String l : docCplt){
                if (!l.startsWith("#") && !l.equals("") && l.contains(";") && !l.contains("USERNAME")){
                    nb_lig++;
                }
            }
            int decal = 0;
            String[] doc = new String[nb_lig];
            String l;
            for (int i = 0; i < docCplt.length; i++){
                l = docCplt[i];
                if (!l.startsWith("#") && !l.equals("") && l.contains(";") && !l.contains("USERNAME")){
                    doc[i-decal] = l;
                    decal++;
                }
            }
            return doc;
        } else {
            createScoreboard();
            return null;
        }
    }

    public static Save[] scoreboardToSaves(){

        /* Cette méthode renvoie le scoreboard sous forme de tableau de Save. (À n'utiliser que pour faciliter l'affichage et la lisibilité du code.) */

        String[] arrStr = getScoreboard();
        Save[] arr = new Save[arrStr.length];
        int i = 0;
        String[] lArr;
        for (String l : arrStr){
            arr[i] = new Save();
            lArr = l.split(";");
            arr[i].username = lArr[0];
            arr[i].difficulty = lArr[1];
            arr[i].time = Integer.parseInt(lArr[2]);
            arr[i].lastSave = Long.parseLong(lArr[3]);
            i++;
        }
        return arr;
    }

    private static boolean isValidScoreboard(String line){

        /* Cette méthode renvoie si la ligne du scoreboard est valide ou non. */

        String[] d = line.split(";");
        if (d.length != 5){
            return false;
        }
        String cs_file = d[4];
        long cs_verif_long = getChecksumScoreboard(d[0], d[1], Integer.parseInt(d[2]), Long.parseLong(d[3]));
        String cs_verif = String.valueOf(cs_verif_long);
        return cs_file.equals(cs_verif) && Long.parseLong(d[3]) <= Time.now();
    }

    private static void checkIntegrityScoreboard(){

        /* Cette méthode supprime les lignes du scoreboard non-valides. */

        for (String l : getScoreboard()){
            if (!isValidScoreboard(l)){
                deleteLineFromScoreboard(l);
            }
        }
    }

    private static boolean deleteLineFromScoreboard(String l){

        /* Cette méthode supprime la ligne du scoreboard associée à une ligne. */

        String[] arr = l.split(";");
        Save s = new Save();
        s.username = arr[0];
        s.difficulty = arr[1];
        return deleteLineFromScoreboard(s);
    }

    public static boolean deleteLineFromScoreboard(Save s){
        
        /* Cette méthode supprime la ligne du scoreboard associée à une sauvegarde. */

        String[] doc = getScoreboard();
        if (doc == null){
            return false;
        }
        if (alreadySavedInScoreboard(s)){
            createScoreboard(true);
            File folder = new File (FileManager.findRelativeFromUserDir("Saves").toString());
            File file = new File(FileManager.findRelative(folder.toPath(), "scoreboard.txt").toString());
            for (String l : doc){
                if (!l.contains(s.username+";"+s.difficulty)){
                    FileManager.appendToFile(file.toPath(), l+"\n");
                }
            }
            return true;
        }
        return false;
    }

    public static boolean clearScoreboard(){

        /* Cette méthode supprime tous les scores. */

        return createScoreboard(true);
    }

    private static boolean addLineToScoreboard(String s){

        /* Cette méthode rajoute une ligne à la fin du scoreboard. */

        File folder = new File (FileManager.findRelativeFromUserDir("Saves").toString());
        File file = new File(FileManager.findRelative(folder.toPath(), "scoreboard.txt").toString());
        checkIntegrityScoreboard();
        return FileManager.appendToFile(file.toPath(), s + "\n");
    }

    private static boolean addLineToScoreboard(Save s){

        /* Cette méthode rajoute une ligne à la fin du scoreboard. */

        String str = s.username + ";" + s.difficulty + ";" + String.valueOf(s.time) + ";" + String.valueOf(s.lastSave) + ";" + String.valueOf(getChecksumScoreboard(s.username, s.difficulty, s.time, s.lastSave));
        return addLineToScoreboard(str);
    }

    public static boolean updateLine(Save s){

        /* Cette méthode met à jour le scoreboard en fonction de la sauvegarde. */

        createScoreboard();
        checkIntegrityScoreboard();
        if (!alreadySavedInScoreboard(s)){
            return addLineToScoreboard(s);
        }
        String[] sc = getScoreboard();
        String[] arr;
        for (String sc1 : sc) {
            if (sc1.contains(s.username+";"+s.difficulty)) {
                arr = sc1.split(";");
                if (Integer.parseInt(arr[2]) > s.time){
                    deleteLineFromScoreboard(s);
                    addLineToScoreboard(s);
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean alreadySavedInScoreboard(Save s){

        /* Cette méthode renvoie si le scoreboard possède déjà une ligne avec la même clé. */

        if (!createScoreboard()) {
            return false;
        }
        checkIntegrityScoreboard();
        for (String l : getScoreboard()){
            if (l.contains(s.username+";"+s.difficulty)){
                return true;
            }
        }
        return false;
    }

    public static void showScoreboard(){

        /* Cette méthode affiche le scoreboard. */

        for (String l : getScoreboard()){
            System.out.println(l);
        }
    }

    public static void main(String[] args){
        
    }
}