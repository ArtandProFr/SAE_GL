
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
            bw.write("USERNAME : " + (String) infos.get("USERNAME"));
            bw.newLine();
            bw.write("SAVENAME : " + (String) infos.get("SAVENAME"));
            bw.newLine();
            bw.write("DIFFICULTY : " + (String) infos.get("DIFFICULTY"));
            bw.newLine();
            bw.write("CREATION DATE : " + (String) infos.get("CREATIONDATE"));
            bw.newLine();
            bw.write("LAST SAVE : " + (String) infos.get("LASTSAVE"));
            bw.newLine();
            bw.write("PHASE : " + (String) infos.get("PHASE"));
            bw.newLine();
            bw.write("TIME : " + (String) infos.get("TIME"));
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

    public static void main(String[] args){
        // TESTS
        /*
        HashMap<String, String> hm = new HashMap<>();
        hm.put("USERNAME", "ArtandProFr");
        hm.put("SAVENAME", "Premiere_Partie");
        hm.put("DIFFICULTY", "MOYENNE");
        hm.put("CREATIONDATE", "02/05/2026 - 22:46:00");
        hm.put("LASTSAVE", "02/05/2026 - 22:47:00");
        hm.put("PHASE", "1.2");
        hm.put("TIME", "00:01:00");
        System.out.println(alreadyExists(hm));
        save(hm);
        System.out.println(alreadyExists(hm));
        */
    }
}