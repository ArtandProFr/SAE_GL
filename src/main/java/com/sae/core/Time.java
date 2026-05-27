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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Time{

    static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm:ss");

    public static LocalDateTime instantToDateTime(long instant){

        /* Cette méthode renvoie un DateTime relatif à l'instant (timestamp) et au lieu actuel. */

        return LocalDateTime.ofInstant(Instant.ofEpochMilli(instant), ZoneId.systemDefault());
    }

    public static long dateTimeToInstant(LocalDateTime dateTime){

        /* Cette méthode renvoie l'instant (timestamp) relatif au LocalDateTime et au lieu actuel. */

        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public static long instantFromString(String dateTime, DateTimeFormatter formatter){

        /* Cette méthode renvoie l'instant (timestamp) lié à une heure en chaine de caractere selon le format. */

        return dateTimeToInstant(toDateTimeFormat(dateTime, formatter));
    }

    public static long instantFromString(String dateTime){

        /* Cette méthode renvoie l'instant (timestamp) lié à une heure en chaine de caractere selon le format par défaut. */

        return instantFromString(dateTime, dateTimeFormatter);
    }

    public static String stringFromInstant(long dateTime, DateTimeFormatter formatter){

        /* Cette méthode renvoie la chaîne de caractère en fonction de l'instant et du format. */

        return dateTimeToString(instantToDateTime(dateTime), formatter);
    }

    public static String stringFromInstant(long dateTime){

        /* Cette méthode renvoie la chaîne de caractère en fonction de l'instant et du format. */

        return stringFromInstant(dateTime, dateTimeFormatter);
    }

    public static String chronoToString(int secondes){

        /* Cette méthode renvoie la chaine du chrono hh:mm:ss correspondante aux secondes. */

        if (secondes < 0){
            return "..:..:..";
        }
        int s = secondes;
        int h = s/3600;
        s -= h*3600;
        int m = s/60;
        s -= m*60;
        String c1 = "", c2 = "", c3 = "";
        if (h < 10){
            c1 = "0";
        }
        if (m < 10){
            c2 = "0";
        }
        if (s < 10){
            c3 = "0";
        }
        return String.format(c1+"%d:"+c2+"%d:"+c3+"%d", h, m, s);
    }

    public static int chronoToInt(String chrono){

        /* Cette méthode renvoie le nombre de secondes correspondantes à un chrono hh:mm:ss. */

        String[] arr = chrono.split(":");
        return Integer.parseInt(arr[0]) * 3600 + Integer.parseInt(arr[1]) * 60 + Integer.parseInt(arr[2]);
    }

    public static String dateTimeToString(LocalDateTime dateTime, DateTimeFormatter formatter){

        /* Cette méthode renvoie la chaîne de caractère correspondant au dateTime suivant le format formatter. */

        return dateTime.format(formatter);
    }

    public static String dateTimeToString(LocalDateTime dateTime){
        
        /* Cette méthode renvoie la chaîne de caractère correspondant au dateTime suivant le format formatter de sauvegarde. */

        return dateTimeToString(dateTime, dateTimeFormatter);
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

    public static long now(){
        
        /* Cette méthode renvoie l'instant correspondant à l'instant NOW. */

        return System.currentTimeMillis();
    }

    public static void main(String[] args) {
        
    }
}