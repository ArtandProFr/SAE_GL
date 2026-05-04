import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Time{

    static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm:ss");

    public static String toString(LocalDateTime dateTime, DateTimeFormatter formatter){

        /* Cette méthode renvoie la chaîne de caractère correspondant au dateTime suivant le format formatter. */

        return dateTime.format(formatter);
    }

    public static String toString(LocalDateTime dateTime){
        
        /* Cette méthode renvoie la chaîne de caractère correspondant au dateTime suivant le format formatter de sauvegarde. */

        return toString(dateTime, dateTimeFormatter);
    }

    public static String toDateTimeFormat(){
        
        /* Cette méthode renvoie la chaîne de caractère correspondant au dateTime nul. */

        return "../../.... - ..:..:..";
    }

    public static LocalDateTime toDateTimeFormat(String str, DateTimeFormatter formatter){
        
        /* Cette méthode renvoie le DateTime correspondant à la chaine de caractère suivant le format formatter. */

        return LocalDateTime.parse(str, formatter);
    }

    public LocalDateTime toDateTimeFormat(String str){
        
        /* Cette méthode renvoie le DateTime correspondant à la chaine de caractère suivant le format formatter de sauvegarde. */

        return toDateTimeFormat(str, dateTimeFormatter);
    }

    public static LocalDateTime now(){
        
        /* Cette méthode renvoie le DateTime correspondant à l'instant NOW. */

        return LocalDateTime.now();
    }

    public static void main(String[] args) {
        
    }
}