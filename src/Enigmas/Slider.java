
import java.util.HashMap;

public class Slider{

    public static final String RED = "RED";
    public static final String BLUE = "BLUE";
    public static final String YELLOW = "YELLOW";
    public static final String COL_RED = "#B30707";
    public static final String COL_BLUE = "#0710B3";
    public static final String COL_YELLOW = "#B37007";
    public static final HashMap<String, String> COLORS = getHashMapColor(RED, BLUE, YELLOW, COL_RED, COL_BLUE, COL_YELLOW);
    public static final Slider SL_RED = new Slider(RED);
    public static final Slider SL_BLUE = new Slider(BLUE);
    public static final Slider SL_YELLOW = new Slider(YELLOW);

    public Vec2 coord;
    public double rayon;
    public boolean state;
    public String color;

    public Slider(){

        /* Constructeur dans le cas de l'énigme MOVING_BALLS */

    }

    public Slider(String color){

        /* Constructeur dans le cas de l'énigme MOVING_LIGHTS */

        this.color = color;
    }

    public Slider(Slider s){

        /* Constructeur par recopie, utile pour MOVING_LIGHTS */

        this.color = s.color;
    }

    private static HashMap<String, String> getHashMapColor(String red, String blue, String yellow, String colRed, String colBlue, String colYellow){

        /* Cette méthode initialise le hashmap de couleurs (nom --> HEX). */

        HashMap<String, String> hs = new HashMap<>();
        hs.put(red, colRed);
        hs.put(blue, colBlue);
        hs.put(yellow, colYellow);
        return hs;
    }

    public void setDrawingInfo(Vec2 coord, double rayon){

        /* Cette méthode initialise les infos pour l'affichage. */

        this.rayon = rayon;
        setCoord(coord);
    }

    public void setCoord(Vec2 coord){

        /* Cette méthode modifie les coordonnées. */

        this.coord = coord;
    }
}