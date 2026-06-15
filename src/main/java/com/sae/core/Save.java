package com.sae.core;


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

    private String username = usernameBase;
    private String savename = savenameBase;
    private String difficulty = difficultyBase;
    private long creationDate = creationDateBase;
    private long lastSave = lastSaveBase;
    private double phase = phaseBase;
    private int time = timeBase;
    private int checksum = getChecksum();

    static char[] validCaract = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                          'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                          '_', '#', '@', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
    
    static String[] difficulties = {"Easy", "Normal", "Hard"};

    public String getUsername()    { return username; }
    public String getSavename()    { return savename; }
    public String getDifficulty()  { return difficulty; }
    public long getCreationDate()  { return creationDate; }
    public long getLastSave()      { return lastSave; }
    public int  getTime()          { return time; }
    public void addTime(int t)     { time += t; }
    public double getPhase()       { return phase; }

    public static void reset(Save s){
        s.username     = usernameBase;
        s.savename     = savenameBase;
        s.difficulty   = difficultyBase;
        s.creationDate = creationDateBase;
        s.lastSave     = lastSaveBase;
        s.phase        = phaseBase;
        s.time         = timeBase;
        s.checksum     = s.getChecksum();
    }

    public void reset(){ reset(this); }

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

    public int getChecksum(){ return getChecksum(this); }

    public static boolean initializeSave(Save s){

        /* La méthode initialise une sauvegarde si elle est valide et n'existe pas déjà en la stockant dans le dossier Saves. Elle renvoie un booléen selon si la création de la sauvegarde a réussi ou non. */

        if (s.isValidToStart() && !s.alreadyExists()){
            s.creationDate = Time.now();
            s.lastSave     = s.creationDate;
            s.phase        = 0.1;
            s.time         = 0;
            return s.save();
        }
        return false;
    }

    public void loadSave(Save s){

        /* Met à jour une sauvegarde pour recopier une autre. */

        this.username     = s.getUsername();
        this.savename     = s.getSavename();
        this.difficulty   = s.getDifficulty();
        this.creationDate = s.getCreationDate();
        this.lastSave     = s.getLastSave();
        this.phase        = s.getPhase();
        this.time         = s.getTime();
        this.checksum     = s.getChecksum();
    }

    public boolean initializeSave(){ return initializeSave(this); }

    public static boolean isInitial(Save s){

        /* Cette méthode renvoie un booléen selon si la Save est à l'état initial. */

        return s.username.equals(usernameBase) && s.savename.equals(savenameBase)
            && s.difficulty.equals(difficultyBase) && s.creationDate == creationDateBase
            && s.lastSave == lastSaveBase && s.phase == phaseBase
            && s.time == timeBase && s.checksum == getChecksum(s);
    }

    public boolean isInitial(){ return isInitial(this); }

    public static boolean isValid(Save s){

        /* Cette méthode renvoie si la partie est valide, intègre et sans triche. */
        /* ATTENTION : l'utilisation de LocalDateTime peut entraîner la suppression d'une sauvegarde créée dans un autre fuseau horaire ("retour dans le passé"). */

        boolean valid = s.isValidToStart()
                && s.creationDate != creationDateBase
                && s.lastSave     != lastSaveBase
                && s.phase >= 0.1;
        if (valid){
            valid = s.creationDate <= s.lastSave && s.lastSave <= Time.now();
            if (valid) valid = (s.checksum == getChecksum(s));
        }
        return valid;
    }

    public boolean isValid(){ return isValid(this); }

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

        /* La méthode modifie la difficulté et renvoie un booléen selon si elle est valide. */

        difficulty = newDifficulty;
        return difficultyIsValid();
    }

    public void setPhase(double n){

        /* Setter de phase */

        phase = n;
    }

    public void showSave(){

        /* Cette méthode affiche les informations de la sauvegarde en console. Ne doit être utilisé que pour le développement. */

        System.out.println("Username : "      + username);
        System.out.println("Savename : "      + savename);
        System.out.println("Difficulty : "    + difficulty);
        System.out.println("Creation date : " + Time.stringFromInstant(creationDate));
        System.out.println("Last save : "     + Time.stringFromInstant(lastSave));
        System.out.println("Phase : "         + phase);
        System.out.println("Time : "          + time);
        System.out.println("Checksum : "      + checksum);
        System.out.println("Est sauvegardée dans le dossier ? " + alreadyExists());
    }

    public static boolean isValidToStart(Save s){

        /* La méthode renvoie un booléen selon si le pseudo, le nom de sauvegarde et la difficulté est valide. */

        return s.usernameIsValid() && s.savenameIsValid() && s.difficultyIsValid();
    }

    public boolean isValidToStart(){ return isValidToStart(this); }

    public static boolean stringIsValid(String str){

        /* Cette méthode renvoie si une chaîne de caractère respecte les caractères valides ou non. */

        for (char c : str.toCharArray()){
            boolean ok = false;
            for (char car : validCaract){ if (car == c){ ok = true; break; } }
            if (!ok) return false;
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

        for (String str : difficulties){ if (str.equals(s.difficulty)) return true; }
        return false;
    }

    public boolean usernameIsValid()  { return usernameIsValid(this); }
    public boolean savenameIsValid()  { return savenameIsValid(this); }
    public boolean difficultyIsValid(){ return difficultyIsValid(this); }

    public static boolean alreadyExists(Save s){

        /* La méthode renvoie un booléen selon si une sauvegarde (Pseudo-NomSauvegarde) existe déjà */

        File folder = new File(FileManager.findRelativeFromUserDir("Saves").toString());
        if (folder.exists()){
            File file = new File(FileManager.findRelative(
                    folder.toPath(), s.username + "-" + s.savename + ".txt").toString());
            return file.exists();
        }
        return false;
    }

    public boolean alreadyExists(){ return alreadyExists(this); }

    public static boolean save(Save s){

        /* La méthode sauvegarde la partie. ATTENTION : La méthode ne vérifie pas la validité des infos données. */

        File folder = new File(FileManager.findRelativeFromUserDir("Saves").toString());
        if (!folder.exists()) folder.mkdir();
        File file = new File(FileManager.findRelative(
                folder.toPath(), s.username + "-" + s.savename + ".txt").toString());
        if (file.exists()) file.delete();
        if (!FileManager.createFile(file)) return false;
        s.lastSave = Time.now();
        String doc = "\n" + GameInfos.GAMENAME + "\n\n"
                + "WARNING : Any manual modification will PERMANENTLY DELETE the save.\n\n"
                + "USERNAME >>> "      + s.username      + "\n"
                + "SAVENAME >>> "      + s.savename      + "\n"
                + "DIFFICULTY >>> "    + s.difficulty    + "\n"
                + "CREATION DATE >>> " + s.creationDate  + "\n"
                + "LAST SAVE >>> "     + s.lastSave      + "\n"
                + "PHASE >>> "         + s.phase         + "\n"
                + "TIME >>> "          + Time.chronoToString(s.time) + "\n"
                + "CHECKSUM >>> "      + s.getChecksum();
        return FileManager.writeFile(file.toPath(), doc);
    }

    public boolean save(){ return save(this); }

    public static boolean delete(Save s){

        /* La méthode supprime la sauvegarde si elle existe et renvoie un booléen selon si la suppression a été effectuée ou non. */

        File folder = new File(FileManager.findRelativeFromUserDir("Saves").toString());
        if (!folder.exists()){ folder.mkdir(); return false; }
        File file = new File(FileManager.findRelative(
                folder.toPath(), s.username + "-" + s.savename + ".txt").toString());
        if (file.exists()){ file.delete(); return true; }
        return false;
    }

    public boolean delete(){ return delete(this); }

    public static boolean clearSaves(){
        for (Save s : getAllSaves()) s.delete();
        return (getAllSaves() == null || getAllSaves().length == 0);
    }

    public static Save getSave(String givenUsername, String givenSavename){

        /* Cette méthode renvoie un objet Save initialisé à partir d'un nom d'utilisateur et un nom de sauvegarde. */

        return getSave(givenUsername + "-" + givenSavename);
    }

    public static Save getSave(String fileName){

        /* Cette méthode renvoie un objet Save initialisé à partir d'un nom de fichier.
           Supprime la sauvegarde et reset si non-intègre.
           CORRECTION : la validation est effectuée APRÈS le parsing complet du fichier
           (et non ligne par ligne), et le split("-") limite=2 pour supporter les
           savenames contenant des tirets. */

        if (!fileName.contains(".")) fileName += ".txt";
        File folder = new File(FileManager.findRelativeFromUserDir("Saves").toString());
        FileManager.createFolder(folder);
        File file = new File(FileManager.findRelative(folder.toPath(), fileName).toString());
        Save s = new Save();

        if (file.exists() && FileManager.isGameFile(file)
                && fileName.endsWith(".txt")
                && !fileName.equals("scoreboard.txt")
                && fileName.contains("-")){

            // Extraction user / savename depuis le nom de fichier
            // On utilise indexOf pour ne splitter qu'au PREMIER tiret,
            // ce qui supporte les savenames contenant eux-mêmes des tirets
            String baseName = fileName;
            if (baseName.contains("."))
                baseName = baseName.substring(0, baseName.lastIndexOf('.'));
            int firstDash = baseName.indexOf('-');
            String expectedUser = baseName.substring(0, firstDash);
            String expectedSave = baseName.substring(firstDash + 1);

            String docStr = FileManager.readFile(file.toPath());
            if (docStr != null){
                // Parsing COMPLET avant toute validation
                for (String l : docStr.split("\n")){
                    String[] arr = l.split(">>>");
                    String key   = arr[0].replace(" ", "");
                    if (arr.length > 1){
                        String elem = arr[1].replace(" - ", ">>>")
                                           .replace(" ", "")
                                           .replace(">>>", " - ");
                        switch (key){
                            case "USERNAME"     -> s.username     = elem;
                            case "SAVENAME"     -> s.savename     = elem;
                            case "DIFFICULTY"   -> s.difficulty   = elem;
                            case "CREATIONDATE" -> s.creationDate = Long.parseLong(elem);
                            case "LASTSAVE"     -> s.lastSave     = Long.parseLong(elem);
                            case "PHASE"        -> s.phase        = Double.parseDouble(elem);
                            case "TIME"         -> s.time         = Time.chronoToInt(elem);
                            case "CHECKSUM"     -> s.checksum     = Integer.parseInt(elem);
                            default -> {}
                        }
                    }
                }
                // Validation APRÈS parsing complet
                if (!expectedUser.equals(s.username)
                        || !expectedSave.equals(s.savename)
                        || !s.isValid()){
                    file.delete();
                    s.reset();
                }
            } else {
                if (!s.isValid()){ file.delete(); s.reset(); }
            }
        }
        return s.isInitial() ? null : s;
    }

    public static boolean initSaveFromFile(Save s){

        /* Cette méthode initialise toute les infos d'une partie sauvegardée. Renvoie un booléen selon si la partie existe ou non. Supprime la sauvegarde et reset si non-intègre. */

        String fileName = s.username + "-" + s.savename + ".txt";
        String user     = s.username;
        String save     = s.savename;
        File folder = new File(FileManager.findRelativeFromUserDir("Saves").toString());
        if (!folder.exists()) FileManager.createFolder(folder);
        File file = new File(FileManager.findRelative(folder.toPath(), fileName).toString());
        if (!file.exists()) return false;
        String docStr = FileManager.readFile(file.toPath());
        if (docStr == null){ if (!s.isValid()){ file.delete(); s.reset(); } return false; }
        for (String l : docStr.split("\n")){
            String[] arr = l.split(">>>");
            String key   = arr[0].replace(" ", "");
            if (arr.length > 1){
                String elem = arr[1].replace(" - ", ">>>").replace(" ", "").replace(">>>", " - ");
                switch (key){
                    case "USERNAME"     -> s.username     = elem;
                    case "SAVENAME"     -> s.savename     = elem;
                    case "DIFFICULTY"   -> s.difficulty   = elem;
                    case "CREATIONDATE" -> s.creationDate = Long.parseLong(elem);
                    case "LASTSAVE"     -> s.lastSave     = Long.parseLong(elem);
                    case "PHASE"        -> s.phase        = Double.parseDouble(elem);
                    case "TIME"         -> s.time         = Time.chronoToInt(elem);
                    case "CHECKSUM"     -> s.checksum     = Integer.parseInt(elem);
                    default -> {}
                }
            }
        }
        if (!user.equals(s.username) || !save.equals(s.savename) || !s.isValid()){
            file.delete(); s.reset(); return false;
        }
        return true;
    }

    public boolean initializeSaveFromFile(){ return initSaveFromFile(this); }

    public static Save[] getAllSaves(){

        /* Cette méthode renvoie un tableau de toutes les parties sauvegardées. Supprime les parties non-valides / non-intègres. */
        
        File folder = new File(FileManager.findRelativeFromUserDir("Saves").toString());
        if (!folder.exists()) FileManager.createFolder(folder);
        String[] listeStr = folder.list();
        if (listeStr == null) return new Save[0];
        java.util.ArrayList<Save> result = new java.util.ArrayList<>();
        for (String fileName : listeStr){
            Save s = getSave(fileName);
            if (s != null && !isInitial(s)) result.add(s);
        }
        return result.toArray(new Save[0]);
    }

    /**
     * Recherche les sauvegardes dont le nom de joueur ou le nom de sauvegarde
     * contient la chaîne query (insensible à la casse).
     */
    public static Save[] searchSaves(String query){

        /* Cette méthode renvoie les sauvegardes dont le username ou le savename
           contient la chaîne query (recherche partielle, insensible à la casse). */

        if (query == null || query.isBlank()) return getAllSaves();
        String q = query.toLowerCase();
        java.util.ArrayList<Save> result = new java.util.ArrayList<>();
        for (Save s : getAllSaves()){
            if (s.username.toLowerCase().contains(q)
                    || s.savename.toLowerCase().contains(q)){
                result.add(s);
            }
        }
        return result.toArray(new Save[0]);
    }

    /**
     * Recherche les entrées du scoreboard dont le username contient la chaîne query
     * (insensible à la casse).
     */
    public static Save[] searchScoreboard(String query){

        /* Cette méthode renvoie les entrées du scoreboard dont le username
           contient la chaîne query (recherche partielle, insensible à la casse). */

        if (query == null || query.isBlank()) return scoreboardToSaves();
        String q = query.toLowerCase();
        java.util.ArrayList<Save> result = new java.util.ArrayList<>();
        for (Save s : scoreboardToSaves()){
            if (s.username.toLowerCase().contains(q)) result.add(s);
        }
        return result.toArray(new Save[0]);
    }

    public static Save[] playerFilter(Save[] arr, String userFilter){

        /* Cette méthode renvoie une liste de Save filtrée par nom de joueur (exact). */

        java.util.ArrayList<Save> result = new java.util.ArrayList<>();
        for (Save s : arr) if (s.username.equals(userFilter)) result.add(s);
        return result.toArray(new Save[0]);
    }

    public static Save[] difficultyFilter(Save[] arr, String diffFilter){

        /* Cette méthode renvoie une liste de Save filtrée par difficulté. */

        java.util.ArrayList<Save> result = new java.util.ArrayList<>();
        for (Save s : arr) if (diffFilter.equals("ALL") || s.difficulty.equals(diffFilter)) result.add(s);
        return result.toArray(new Save[0]);
    }

    private static void exchange(Save[] arr, int i, int j){
        Save temp = arr[i]; arr[i] = arr[j]; arr[j] = temp;
    }

    private static void quicksortLastSave(Save[] arr, int debut, int fin, int type){

        /* Cette méthode est une itération de l'algorithme Quicksort appliqué à un tableau de save sur le paramètre LastSave. */

        if (debut < fin){
            int pivot    = fin;
            int nb_small = debut;
            for (int i = debut; i < fin; i++){
                int c = (arr[i].lastSave <= arr[pivot].lastSave) ? type * -1 : type * 1;
                if (c < 0){ exchange(arr, i, nb_small); nb_small++; }
            }
            exchange(arr, nb_small, pivot);
            quicksortLastSave(arr, debut,      nb_small - 1, type);
            quicksortLastSave(arr, nb_small + 1, fin,        type);
        }
    }

    private static void quicksortLessTime(Save[] arr, int debut, int fin, int type){

        /* Cette méthode est une itération de l'algorithme Quicksort appliqué à un tableau de save sur le paramètre LessTime. */

        if (debut < fin){
            int pivot    = fin;
            int nb_small = debut;
            for (int i = debut; i < fin; i++){
                if (type * arr[i].time < arr[pivot].time * type){
                    exchange(arr, i, nb_small); nb_small++;
                }
            }
            exchange(arr, nb_small, pivot);
            quicksortLessTime(arr, debut,      nb_small - 1, type);
            quicksortLessTime(arr, nb_small + 1, fin,        type);
        }
    }

    public static void lessTimeOrder(Save[] arr, int type){

        /* Cette méthode tri le tableau de Save selon le temps. */

        if (arr.length > 1) quicksortLessTime(arr, 0, arr.length - 1, type);
    }

    public static void lastSaveOrder(Save[] arr, int type){

        /* Cette méthode tri le tableau de Save selon la dernière sauvegarde. */

        if (arr.length > 1) quicksortLastSave(arr, 0, arr.length - 1, type);
    }

    public static boolean createScoreboard(boolean rewrite){

        /* Cette méthode crée le fichier scoreboard.txt.
           Si rewrite est true, écrase le fichier existant.
           Retourne true si le fichier a été créé ou réécrit, false s'il existait déjà et rewrite=false. */

        File folder = new File(FileManager.findRelativeFromUserDir("Saves").toString());
        if (!folder.exists()) FileManager.createFolder(folder);
        File file = new File(FileManager.findRelative(folder.toPath(), "scoreboard.txt").toString());
        if (!file.exists() || rewrite){
            if (file.exists()) file.delete();
            if (FileManager.createFile(file)){
                FileManager.writeFile(file.toPath(),
                        "#" + GameInfos.GAMENAME + "\n" + "USERNAME;DIFFICULTY;TIME;DATE;CHECKSUM\n");
                return true;
            }
        }
        return false;
    }

    private static boolean createScoreboard(){ return createScoreboard(false); }

    private static long getChecksumScoreboard(String user, String diff, int t, long d){

        /* Cette méthode renvoie le checksum d'une ligne du scoreboard. */

        return (user + diff + t + d + alteration).hashCode();
    }

    public static String[] getScoreboardBase(){

        /* Cette méthode renvoie le scoreboard brut sous forme de tableau de chaînes de caractères,
           sans vérification d'intégrité (utilisée en interne pour éviter la récursion infinie).
           Chaque ligne est nettoyée avec trim() pour gérer les retours chariot Windows (\r\n). */

        File folder = new File(FileManager.findRelativeFromUserDir("Saves").toString());
        if (!folder.exists()){ FileManager.createFolder(folder); createScoreboard(); return new String[0]; }
        File file = new File(FileManager.findRelative(folder.toPath(), "scoreboard.txt").toString());
        if (!file.exists()){ createScoreboard(); return new String[0]; }
        String docStr = FileManager.readFile(file.toPath());
        if (docStr == null) return new String[0];
        java.util.ArrayList<String> lines = new java.util.ArrayList<>();
        for (String l : docStr.split("\n")){
            l = l.trim();
            if (!l.isEmpty() && !l.startsWith("#") && l.contains(";") && !l.contains("USERNAME"))
                lines.add(l);
        }
        return lines.toArray(new String[0]);
    }

    public static String[] getScoreboard(){

        /* Cette méthode renvoie le scoreboard vérifié sous forme de tableau de chaînes de caractères.
           Les lignes invalides ou corrompues sont supprimées avant le retour. */

        checkIntegrityScoreboard();
        return getScoreboardBase();
    }

    public static Save[] scoreboardToSaves(){

        /* Cette méthode renvoie le scoreboard sous forme de tableau de Save. (À n'utiliser que pour faciliter l'affichage et la lisibilité du code.) */

        java.util.ArrayList<Save> result = new java.util.ArrayList<>();
        for (String l : getScoreboard()){
            String[] lArr = l.split(";");
            if (lArr.length >= 4){
                Save s    = new Save();
                s.username   = lArr[0];
                s.difficulty = lArr[1];
                s.time       = Integer.parseInt(lArr[2]);
                s.lastSave   = Long.parseLong(lArr[3]);
                result.add(s);
            }
        }
        return result.toArray(new Save[0]);
    }

    private static boolean isValidScoreboard(String line){

        /* Cette méthode renvoie si la ligne du scoreboard est valide ou non. */

        String[] d = line.split(";");
        if (d.length != 5) return false;
        long cs = getChecksumScoreboard(d[0], d[1], Integer.parseInt(d[2]), Long.parseLong(d[3]));
        return d[4].equals(String.valueOf(cs)) && Long.parseLong(d[3]) <= Time.now();
    }

    private static void checkIntegrityScoreboard(){

        /* Cette méthode supprime les lignes du scoreboard non-valides ou corrompues.
           Utilise getScoreboardBase() pour éviter la récursion infinie. */

        for (String l : getScoreboardBase())
            if (!isValidScoreboard(l)) deleteLineFromScoreboardRaw(l);
    }

    private static boolean deleteLineFromScoreboardRaw(String l){

        /* Cette méthode supprime physiquement une ligne du fichier scoreboard sans passer
           par alreadySavedInScoreboard (évite la récursion). Utilisée uniquement par checkIntegrityScoreboard.
           Renvoie false si la ligne est malformée (moins de 2 colonnes après split). */

        String[] arr = l.split(";");
        if (arr.length < 2) return false;
        String targetUser = arr[0];
        String targetDiff = arr[1];
        String[] doc = getScoreboardBase();
        createScoreboard(true);
        File folder = new File(FileManager.findRelativeFromUserDir("Saves").toString());
        File file   = new File(FileManager.findRelative(folder.toPath(), "scoreboard.txt").toString());
        for (String line : doc){
            String[] parts = line.split(";");
            if (parts.length < 2 || !(parts[0].equals(targetUser) && parts[1].equals(targetDiff)))
                FileManager.appendToFile(file.toPath(), line + "\n");
        }
        return true;
    }

    public static boolean deleteLineFromScoreboard(Save s){

        /* Cette méthode supprime la ligne du scoreboard associée à une sauvegarde. */

        String[] doc = getScoreboardBase();
        boolean found = false;
        for (String l : doc){
            String[] parts = l.split(";");
            if (parts.length >= 2 && parts[0].equals(s.username) && parts[1].equals(s.difficulty)){
                found = true; break;
            }
        }
        if (!found) return false;
        createScoreboard(true);
        File folder = new File(FileManager.findRelativeFromUserDir("Saves").toString());
        File file   = new File(FileManager.findRelative(folder.toPath(), "scoreboard.txt").toString());
        for (String l : doc){
            String[] parts = l.split(";");
            if (parts.length < 2 || !(parts[0].equals(s.username) && parts[1].equals(s.difficulty)))
                FileManager.appendToFile(file.toPath(), l + "\n");
        }
        return true;
    }

    public static boolean clearScoreboard(){ return createScoreboard(true); }

    private static boolean addLineToScoreboard(Save s){

        /* Cette méthode rajoute une ligne à la fin du scoreboard si la clé (username;difficulty)
           n'existe pas déjà. Supprime la sauvegarde du dossier Saves après ajout réussi. */

        for (String l : getScoreboardBase()){
            String[] parts = l.split(";");
            if (parts.length >= 2 && parts[0].equals(s.username) && parts[1].equals(s.difficulty))
                return false;
        }
        File folder = new File(FileManager.findRelativeFromUserDir("Saves").toString());
        File file   = new File(FileManager.findRelative(folder.toPath(), "scoreboard.txt").toString());
        String str  = s.username + ";" + s.difficulty + ";" + s.time + ";" + s.lastSave + ";"
                    + getChecksumScoreboard(s.username, s.difficulty, s.time, s.lastSave);
        boolean added = FileManager.appendToFile(file.toPath(), str + "\n");
        if (added) s.delete();
        return added;
    }

    public static boolean updateLine(Save s){

        /* Cette méthode met à jour le scoreboard en fonction de la sauvegarde.
           - Aucune entrée pour (username, difficulty) → ajoute.
           - Entrée existante avec temps supérieur → remplace par le nouveau meilleur temps.
           - Entrée existante avec temps inférieur ou égal → supprime juste la save. */

        createScoreboard();
        checkIntegrityScoreboard();
        for (String sc1 : getScoreboardBase()){
            String[] arr = sc1.split(";");
            if (arr.length >= 3 && arr[0].equals(s.username) && arr[1].equals(s.difficulty)){
                if (Integer.parseInt(arr[2]) > s.time){
                    deleteLineFromScoreboard(s);
                    File folder = new File(FileManager.findRelativeFromUserDir("Saves").toString());
                    File file   = new File(FileManager.findRelative(folder.toPath(), "scoreboard.txt").toString());
                    String str  = s.username + ";" + s.difficulty + ";" + s.time + ";" + s.lastSave + ";"
                                + getChecksumScoreboard(s.username, s.difficulty, s.time, s.lastSave);
                    boolean added = FileManager.appendToFile(file.toPath(), str + "\n");
                    if (added) s.delete();
                    return added;
                }
                s.delete();
                return false;
            }
        }
        return addLineToScoreboard(s);
    }

    private static boolean alreadySavedInScoreboard(Save s){

        /* Cette méthode renvoie si le scoreboard possède déjà une ligne avec la même clé (username;difficulty).
           Utilise getScoreboardBase() pour éviter la récursion infinie. */

        createScoreboard();
        for (String l : getScoreboardBase()){
            String[] parts = l.split(";");
            if (parts.length >= 2 && parts[0].equals(s.username) && parts[1].equals(s.difficulty))
                return true;
        }
        return false;
    }

    public static void showScoreboard(){

        /* Cette méthode affiche le scoreboard. */

        for (String l : getScoreboard()) System.out.println(l);
    }

    public static void main(String[] args){}
}