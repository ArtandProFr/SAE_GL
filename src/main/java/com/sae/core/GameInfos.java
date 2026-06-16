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

import java.util.HashMap;
import java.util.HashSet;

public class GameInfos{
    public static String GAMENAME = "BLURRED";
    public static String GAMENAME_TYPE_2 = "Blurred";

    /** Version courante du jeu. */
    public static final String VERSION = "Alpha";

    /**
     * Note de version affichée dans le menu PAUSE.
     * Chaque entrée est un couple de clés de traduction {titre, contenu}.
     * Ces notes documentent les bugs connus et leurs solutions.
     */
    public static final String[][] RELEASE_NOTES = {
        { "BUG_1_TITLE", "BUG_1_FIX" },
        { "BUG_2_TITLE", "BUG_2_FIX" }
    };

    public static HashMap CREDITS = new HashMap<>();
    
    static {
        final HashSet<String> conception = new HashSet<>();
        conception.add("DWORNICZAK Arthur");
        conception.add("PERROT Roxane");
        conception.add("GAMON Thomas");
        conception.add("OUERHANI Farès");
        CREDITS.put("REALISATION & DEVELOPPEMENT", conception);
    }
}
