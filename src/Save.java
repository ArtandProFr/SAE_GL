
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Save{

    String username = "Enter username here...";
    String savename = "Enter savename here...";
    String difficulty = "Normal";
    String creationDate = "";
    String lastSave = "";
    double phase = 0.0;
    int time = 0;

    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm:ss");

    static char[] validCaract = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                          'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                          '_', '#', '@', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

    public boolean initializeSave(){

        /* La méthode initialise une sauvegarde si elle est valide et n'existe pas déjà en la stockant dans le dossier Saves. Elle renvoie un booléen selon si la création de la sauvegarde a réussi ou non. */

        if (isValid() && !alreadyExists()){
            creationDate = LocalDateTime.now().format(formatter);
            lastSave = creationDate;
            phase = 0.1;
            time = 0;
            return save();
        }
        return false;
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

    public void showSave(){

        /* Cette méthode affiche les informations de la sauvegarde en console. Ne doit être utilisé que pour le développement. */

        System.out.println("Username : " + username);
        System.out.println("Savename : " + savename);
        System.out.println("Difficulty : " + difficulty);
        System.out.println("Creation date : " + creationDate);
        System.out.println("Last save : " + lastSave);
        System.out.println("Phase : " + phase);
        System.out.println("Time : " + time);
        System.out.println("Est sauvegardée dans le dossier ? " + alreadyExists());
    }

    public boolean isValid(){

        /* La méthode renvoie un booléen selon si le pseudo et le nom de sauvegarde est valide. */

        return usernameIsValid() && savenameIsValid();
    }
    
    public boolean usernameIsValid(){

        /* La méthode renvoie un booléen selon si le pseudo est valide. */

        boolean isValidCaract;
        for (char c : username.toCharArray()){
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

public boolean savenameIsValid(){

    /* La méthode renvoie un booléen selon si le nom de sauvegarde est valide. */

    boolean isValidCaract;
    for (char c : savename.toCharArray()){
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
            int t = time;
            int h = t/3600;
            t -= h*3600;
            int m = t/60;
            t -= m*60;
            String c1 = "";
            String c2 = "";
            String c3 = "";
            if (h < 10){
                c1 = "0";
            }
            if (m < 10){
                c2 = "0";
            }
            if (t < 10){
                c3 = "0";
            }
            bw.write("TIME >>> " + c1 + h + ":" + c2 + m + ":" + c3 + t);
            return true;
        } catch (IOException e){
            return false;
        }
    }

    public boolean delete(){

        /* La méthode supprime la sauvegarde si elle existe et renvoie un booléen selon si la suppression a été effectuée ou non. (Si la sauvegarde existait avant ou non.) */

        String userDir = System.getProperty("user.dir");
        Path folder_path = Path.of(userDir, "Saves");
        File folder = new File(folder_path.toString());
        if (!folder.exists()){
            folder.mkdir();
            return false;
        }
        Path file_path = Path.of(folder_path.toString(), username + "-" + savename + ".txt");
        File file = new File(file_path.toString());
        if (file.exists()){
            file.delete();
            return true;
        }
        return false;
    }

    public boolean getSave(){

        /* Cette méthode initialise toute les infos d'une partie sauvegardée. Renvoie un booléen selon si la partie existe ou non. */

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
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
                String[] doc = new String[10]; // Pour l'instant 7 lignes suffisent, peut-être qu'une 8eme sera à ajouter pour l'intégrité
                int i = 0;
                String line = reader.readLine();
                while (line != null){
                    doc[i] = line;
                    line = reader.readLine();
                    i++;
                }
                String key;
                String elem;
                String[] arr;
                String l;
                String[] arr2;
                for (int j = 0; j < i; j++){
                    l = doc[j];
                    arr = l.split(">>>");
                    key = arr[0].replace(" ", "");
                    elem = arr[1].replace(" - ", ">>>").replace(" ", "").replace(">>>", " - ");
                    switch (key){
                        case "USERNAME" -> username = elem;
                        case "SAVENAME" -> savename = elem;
                        case "DIFFICULY" -> difficulty = elem;
                        case "CREATIONDATE" -> creationDate = elem;
                        case "LASTSAVE" -> lastSave = elem;
                        case "PHASE" -> phase = Double.parseDouble(elem);
                        case "TIME" -> {
                            arr2 = elem.split(":"); 
                            time = Integer.parseInt(arr2[0]) * 3600 + Integer.parseInt(arr2[1]) * 60 + Integer.parseInt(arr2[2]);}
                        default -> {
                        }
                    }
                }
                return true;
            } catch (IOException e) {
            }
        }
        return false;
    }

    public static void main(String[] args){
        /*
        Save s = new Save();
        s.modifySavename("");
        s.modifyUsername("");
        
        s.initializeSave();
        
        s.getSave();
        s.showSave();
        */
    }
}