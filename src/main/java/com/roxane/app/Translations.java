package com.roxane.app;

import java.util.Map;

public class Translations {
    private static final Map<String, String> EN = Map.ofEntries(
        Map.entry("MENU", "MENU"),
        Map.entry("LANCER UNE PARTIE", "PLAY"),
        Map.entry("PARAMETRES", "SETTINGS"),
        Map.entry("MEILLEUR TEMPS", "BEST TIMES"),
        Map.entry("RETOUR MENU", "BACK TO MENU"),
        Map.entry("SAUVEGARDES", "SAVES"),
        Map.entry("LANCER UNE NOUVELLE PARTIE", "NEW GAME"),
        Map.entry("NOUVELLE PARTIE", "NEW GAME"),
        Map.entry("NOM_SAUVEGARDE", "SAVE NAME"),
        Map.entry("NOM_SAUVEGARDE:", "SAVE NAME:"),
        Map.entry("DATE", "DATE"),
        Map.entry("TEMPS", "TIME"),
        Map.entry("NOM", "NAME"),
        Map.entry("LUMINOSITE", "BRIGHTNESS"),
        Map.entry("SON", "SOUND"),
        Map.entry("LANGUES", "LANGUAGE"),
        Map.entry("CREER", "CREATE"),
        Map.entry("RETOUR LISTE", "BACK TO LIST")
    );

    public static String t(String key) {
        if ("English".equals(Settings.getInstance().getLanguage())) {
            return EN.getOrDefault(key, key);
        }
        return key;
    }
}
