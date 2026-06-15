package com.roxane.app;

import java.util.Map;

public class Translations {
    private static final Map<String, String> EN = Map.ofEntries(
        Map.entry("MENU", "MENU"),
        Map.entry("LANCER UNE PARTIE", "PLAY"),
        Map.entry("PARAMETRES", "SETTINGS"),
        Map.entry("LISTE DES SCORES", "SCOREBOARD"),
        Map.entry("MEILLEURS TEMPS", "BEST TIMES"),
        Map.entry("RETOUR MENU", "BACK TO MENU"),
        Map.entry("RETOUR AU JEU", "BACK TO GAME"),
        Map.entry("SAUVEGARDES", "SAVES"),
        Map.entry("LANCER UNE NOUVELLE PARTIE", "NEW GAME"),
        Map.entry("NOUVELLE PARTIE", "NEW GAME"),
        Map.entry("NOM_SAUVEGARDE", "SAVE'S NAME"),
        Map.entry("NOM_SAUVEGARDE:", "SAVE'S NAME:"),
        Map.entry("DATE", "DATE"),
        Map.entry("TEMPS", "TIME"),
        Map.entry("NOM", "NAME"),
        Map.entry("LUMINOSITE", "BRIGHTNESS"),
        Map.entry("SON", "SOUND"),
        Map.entry("LANGUES", "LANGUAGE"),
        Map.entry("CREER", "CREATE"),
        Map.entry("RETOUR LISTE", "BACK TO LIST"),
        Map.entry("DIFFICULTE", "DIFFICULTY"),
        Map.entry("Normale", "Normal"),
        Map.entry("Facile", "Easy"),
        Map.entry("Difficile", "Hard"),
        Map.entry("MA_PARTIE", "MY_GAME"),
        Map.entry("JOUEUR", "PLAYER"),
        Map.entry("NOM_JOUEUR", "PLAYER_NAME"),
        Map.entry("INFORMATIONS_INVALIDES", "INVALID_INFORMATIONS"),
        Map.entry("SAUVEGARDE_EXISTE", "SAVE_EXISTS"),
        Map.entry("LANCER PARTIE", "LAUNCH GAME"),
        Map.entry("TRIER PAR", "SORT BY"),
        Map.entry("AUCUNE SAUVEGARDE", "NO SAVE"),
        Map.entry("SELECTIONNEZ UNE PARTIE", "SELECT A GAME"),
        Map.entry("MON PROPRE ENNEMI", "MY OWN ENEMY"),
        Map.entry("Mon Propre Ennemi", "My Own Enemy"),
        Map.entry("TOUTES", "ALL"),
        Map.entry("RECHERCHER...", "SEARCH..."),
        Map.entry("AUCUNE SELECTION", "NO SELECTION"),
        Map.entry("SUPPRIMER PARTIE", "DELETE SAVE")
    );
    private static final Map<String, String> FR = Map.ofEntries(
        Map.entry("ALL", "TOUTES"),
        Map.entry("Easy", "Facile"),
        Map.entry("Normal", "Normale"),
        Map.entry("Hard", "Difficile"),
        Map.entry("MY OWN ENEMY", "MON PROPRE ENNEMI"),
        Map.entry("My Own Enemy", "Mon Propre Ennemi")
    );

    public static String toEN(String key){
        return EN.getOrDefault(key, key);
    }


    public static String t(String key) {
        if ("English".equals(Settings.getInstance().getLanguage())) {
            return EN.getOrDefault(key, key);
        } else if ("Francais".equals(Settings.getInstance().getLanguage())){
            return FR.getOrDefault(key, key);
        }
        return key;
    }
}
