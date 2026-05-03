
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.HashMap;

public class Save{
    public static boolean alreadyExists(HashMap infos){

        /* La méthode renvoie un booléen selon si une sauvegarde (Pseudo-NomSauvegarde) existe déjà */

        String userDir = System.getProperty("user.dir");
        Path folder_path = Path.of(userDir, "Saves");
        File folder = new File(folder_path.toString());
        if (folder.exists()){
            Path file_path = Path.of(folder_path.toString(), infos.get("USERNAME") + "-" + infos.get("SAVENAME") + ".txt");
            File file = new File(file_path.toString());
            return file.exists();
        } else {
            return false;
        }
    }

    @SuppressWarnings("ConvertToTryWithResources")
    public static boolean save(HashMap infos){

        /* La méthode sauvegarde la partie. ATTENTION : La méthode ne vérifie pas la validité des infos données. */

        String userDir = System.getProperty("user.dir");
        Path folder_path = Path.of(userDir, "Saves");
        File folder = new File(folder_path.toString());
        if (!folder.exists()){
            folder.mkdir();
        }
        Path file_path = Path.of(folder_path.toString(), infos.get("USERNAME") + "-" + infos.get("SAVENAME") + ".txt");
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
            bw.write("USERNAME >>> " + (String) infos.get("USERNAME"));
            bw.newLine();
            bw.write("SAVENAME >>> " + (String) infos.get("SAVENAME"));
            bw.newLine();
            bw.write("DIFFICULTY >>> " + (String) infos.get("DIFFICULTY"));
            bw.newLine();
            bw.write("CREATION DATE >>> " + (String) infos.get("CREATIONDATE"));
            bw.newLine();
            bw.write("LAST SAVE >>> " + (String) infos.get("LASTSAVE"));
            bw.newLine();
            bw.write("PHASE >>> " + (String) infos.get("PHASE"));
            bw.newLine();
            bw.write("TIME >>> " + (String) infos.get("TIME"));
            return true;
        } catch (IOException e){
            return false;
        }
    }

    public static boolean delete(HashMap infos){

        /* La méthode supprime la sauvegarde si elle existe et renvoie un booléen selon si la suppression a été effectuée ou non. (Si la sauvegarde existait avant ou non.) */

        String userDir = System.getProperty("user.dir");
        Path folder_path = Path.of(userDir, "Saves");
        File folder = new File(folder_path.toString());
        if (!folder.exists()){
            folder.mkdir();
            return false;
        }
        Path file_path = Path.of(folder_path.toString(), infos.get("USERNAME") + "-" + infos.get("SAVENAME") + ".txt");
        File file = new File(file_path.toString());
        if (file.exists()){
            file.delete();
            return true;
        }
        return false;
    }

    public static HashMap<String, String> getSave(String fileName){

        /* Cette méthode renvoie un hashmap contenant toute les infos d'une partie sauvegardée. Si la partie n'existe pas : renvoie null. */

        HashMap<String, String> hm = new HashMap<>();
        String userDir = System.getProperty("user.dir");
        Path folder_path = Path.of(userDir, "Saves");
        File folder = new File(folder_path.toString());
        if (!folder.exists()){
            folder.mkdir();
        }
        Path file_path = Path.of(folder_path.toString(), fileName + ".txt");
        File file = new File(file_path.toString());
        if (!file.exists()){
            hm = null;
        } else {
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
                for (int j = 0; j < i; j++){
                    l = doc[j];
                    arr = l.split(">>>");
                    key = arr[0].replace(" ", "");
                    elem = arr[1].replace(" - ", ">>>").replace(" ", "").replace(">>>", " - ");
                    hm.put(key, elem);
                }
            } catch (IOException e) {
                hm = null;
            }
        }
        return hm;
    }

    public static void main(String[] args){
        // TESTS
        
        /*
        HashMap<String, String> hm1 = new HashMap<>();
        hm1.put("USERNAME", "ArtandProFr");
        hm1.put("SAVENAME", "Premiere_Partie");
        hm1.put("DIFFICULTY", "MOYENNE");
        hm1.put("CREATIONDATE", "02/05/2026 - 22:46:00");
        hm1.put("LASTSAVE", "02/05/2026 - 22:47:00");
        hm1.put("PHASE", "1.2");
        hm1.put("TIME", "00:01:00");
        save(hm1);
        
        
        HashMap<String, String> hm2 = getSave("ArtandProFr-Premiere_Partie");
        if (hm2 != null && !hm2.isEmpty()){
            for (Map.Entry<String, String> e : hm2.entrySet()){
                System.out.println(e.getKey() + " >>> " + e.getValue());
            }
        } else {
            System.out.println("hm2 is null");
        }

        System.out.println("hm1 == hm2 ? : " + hm1.equals(hm2));
        */
        
        
    }
}