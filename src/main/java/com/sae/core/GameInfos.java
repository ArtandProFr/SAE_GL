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
    public static String GAMENAME = "MY OWN ENEMY";
    public static String GAMENAME_TYPE_2 = "My Own Enemy";
    public static HashMap CREDITS = new HashMap<>();
    private final HashSet<String> conception = new HashSet<>();
    {
        conception.add("DWORNICZAK Arthur");
        conception.add("PERROT Roxane");
        conception.add("GAMON Thomas");
        conception.add("OUERHANI Farès");
        CREDITS.put("REALISATION", conception);
    }
}