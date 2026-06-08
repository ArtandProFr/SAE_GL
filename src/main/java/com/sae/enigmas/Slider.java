package com.sae.enigmas;

import java.awt.Color;
import java.awt.Graphics2D;

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

    public Slider(){ /* MOVING_BALLS */ }
    public Slider(String color){ this.color = color; }
    public Slider(Slider s){ this.color = s.color; this.rayon = s.rayon; this.state = s.state; }

    private static HashMap<String, String> getHashMapColor(String red, String blue, String yellow, String colRed, String colBlue, String colYellow){
        HashMap<String, String> hs = new HashMap<>();
        hs.put(red, colRed);
        hs.put(blue, colBlue);
        hs.put(yellow, colYellow);
        return hs;
    }

    public void setDrawingInfo(Vec2 coord, double rayon){
        this.rayon = rayon;
        setCoord(coord);
    }

    public void setCoord(Vec2 coord){
        this.coord = coord;
    }

    /* ====== AFFICHAGE ====== */

    /** Affichage pour MovingBalls (bille blanche). */
    public void drawBall(Graphics2D g){
        if (coord == null) return;
        Draw.circle(g, coord.x, coord.y, rayon, new Color(230, 230, 230));
        Draw.circle(g, coord.x, coord.y, rayon, new Color(40, 45, 55), 2);
    }

    /** Affichage pour MovingLights (boule colorée éteinte/allumée). */
    public void drawLight(Graphics2D g){
        if (coord == null) return;
        Color base = Draw.color(COLORS.getOrDefault(color, "#888888"));
        Color shown = state ? base : Draw.transition(base, Color.BLACK, 65);
        Draw.circle(g, coord.x, coord.y, rayon, shown);
        Draw.circle(g, coord.x, coord.y, rayon, new Color(20, 22, 28), 2);
        if (state){
            Draw.circle(g, coord.x, coord.y, rayon * 0.35,
                    new Color(255, 255, 255, 180));
        }
    }
}
