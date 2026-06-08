package com.sae.enigmas;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import java.util.HashSet;

public class Slide{
    public static final String UP = "UP";
    public static final String DOWN = "DOWN";
    public static final String LEFT = "LEFT";
    public static final String RIGHT = "RIGHT";

    private static final HashSet<String> HLEFT = getHashSet(LEFT);
    private static final HashSet<String> HRIGHT = getHashSet(RIGHT);
    private static final HashSet<String> HDOWN = getHashSet(DOWN);
    private static final HashSet<String> HUP = getHashSet(UP);
    private static final HashSet<String> HVERT = combineHashSet(HUP, HDOWN);
    private static final HashSet<String> HHORI = combineHashSet(HLEFT, HRIGHT);
    private static final HashSet<String> HCROSS = combineHashSet(HVERT, HHORI);
    private static final HashSet<String> HUPRIGHT = combineHashSet(HUP, HRIGHT);
    private static final HashSet<String> HUPLEFT = combineHashSet(HUP, HLEFT);
    private static final HashSet<String> HDOWNRIGHT = combineHashSet(HDOWN, HRIGHT);
    private static final HashSet<String> HDOWNLEFT = combineHashSet(HDOWN, HLEFT);
    private static final HashSet<String> HTRIGHT = combineHashSet(HVERT, HRIGHT);
    private static final HashSet<String> HTLEFT = combineHashSet(HVERT, HLEFT);
    private static final HashSet<String> HTDOWN = combineHashSet(HHORI, HDOWN);
    private static final HashSet<String> HTUP = combineHashSet(HHORI, HUP);

    public Vec2 coord;
    public double taille;
    public boolean isGoal;
    public HashSet<String> directions;
    public HashSet<String> provenances;

    private static HashSet<String> getHashSet(String s){
        HashSet<String> hs = new HashSet<>();
        hs.add(s);
        return hs;
    }
    private static HashSet<String> combineHashSet(HashSet<String> h1, HashSet<String> h2){
        HashSet<String> nv = new HashSet<>(h1);
        nv.addAll(h2);
        return nv;
    }

    public Slide(Slide s){
        this.coord = s.coord;
        this.taille = s.taille;
        this.isGoal = s.isGoal;
        this.directions = new HashSet<>(s.directions);
        this.provenances = new HashSet<>(s.provenances);
    }

    public Slide(String type){
        initWithType(type);
    }

    public Slide(HashSet<String> directions){
        initWithDirections(directions);
    }

    public Slide(String type, boolean isGoal){
        initIsGoal(isGoal);
        initWithType(type);
    }

    public Slide(HashSet<String> directions, boolean isGoal){
        initIsGoal(isGoal);
        initWithDirections(directions);
    }

    private void initWithDirections(HashSet<String> directions){
        this.directions = new HashSet<>(directions);
        initProvenances();
    }

    private void initWithType(String type){
        this.directions = switch(type){
            case UP -> new HashSet<>(HUP);
            case DOWN -> new HashSet<>(HDOWN);
            case LEFT -> new HashSet<>(HLEFT);
            case RIGHT -> new HashSet<>(HRIGHT);
            case UP+"_"+LEFT -> new HashSet<>(HUPLEFT);
            case UP+"_"+RIGHT -> new HashSet<>(HUPRIGHT);
            case DOWN+"_"+LEFT -> new HashSet<>(HDOWNLEFT);
            case DOWN+"_"+RIGHT -> new HashSet<>(HDOWNRIGHT);
            case "T_"+RIGHT -> new HashSet<>(HTRIGHT);
            case "T_"+LEFT -> new HashSet<>(HTLEFT);
            case "T_"+DOWN -> new HashSet<>(HTDOWN);
            case "T_"+UP -> new HashSet<>(HTUP);
            case "CROSS" -> new HashSet<>(HCROSS);
            case "VERT" -> new HashSet<>(HVERT);
            case "HORI" -> new HashSet<>(HHORI);
            default -> new HashSet<>();
        };
        initWithDirections(directions);
    }

    private void initIsGoal(boolean isGoal){
        this.isGoal = isGoal;
    }

    public void setDrawingInfo(Vec2 coord, double taille){
        this.coord = coord;
        this.taille = taille;
    }

    public boolean canGoTo(String dir){
        return directions.contains(dir);
    }

    public boolean canComeFrom(String dir){
        return provenances.contains(dir);
    }

    private void initProvenances(){
        this.provenances = new HashSet<>();
        if (canGoTo(UP)){
            this.provenances.add(DOWN);
        }
        if (canGoTo(DOWN)){
            this.provenances.add(UP);
        }
        if (canGoTo(LEFT)){
            this.provenances.add(RIGHT);
        }
        if (canGoTo(RIGHT)){
            this.provenances.add(LEFT);
        }
    }

    /* ====== AFFICHAGE ====== */

    public void draw(Graphics2D g){
        if (coord == null) return;
        // Fond de case
        Color bgCase = isGoal ? new Color(46, 100, 60) : new Color(30, 32, 38);
        Draw.rect(g, coord.x - taille/2, coord.y - taille/2, taille, taille, bgCase);
        Draw.rectOutline(g, coord.x - taille/2, coord.y - taille/2, taille, taille,
                new Color(60, 65, 75), 1);

        // Rails (épaisseur ~ taille/6, couleur claire)
        double w = Math.max(4, taille / 6.0);
        g.setStroke(new BasicStroke((float) w, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(160, 170, 185));
        if (canGoTo(UP))    g.drawLine((int) coord.x, (int) coord.y, (int) coord.x, (int) (coord.y - taille/2));
        if (canGoTo(DOWN))  g.drawLine((int) coord.x, (int) coord.y, (int) coord.x, (int) (coord.y + taille/2));
        if (canGoTo(LEFT))  g.drawLine((int) coord.x, (int) coord.y, (int) (coord.x - taille/2), (int) coord.y);
        if (canGoTo(RIGHT)) g.drawLine((int) coord.x, (int) coord.y, (int) (coord.x + taille/2), (int) coord.y);

        if (isGoal){
            Draw.circle(g, coord.x, coord.y, taille * 0.15, new Color(46, 204, 113, 180));
        }
    }
}
