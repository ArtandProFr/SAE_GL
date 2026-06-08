package com.sae.enigmas;

import java.awt.Color;
import java.awt.Graphics2D;

public class MovingBallsButton {
    public Vec2 coord;
    public double taille;
    public String sens;
    public boolean pressed;
    public boolean clicked;

    public MovingBallsButton(Vec2 coord, double taille, String sens){
        this.coord = coord;
        this.taille = taille;
        this.sens = sens; // UP, DOWN, LEFT, RIGHT
        this.pressed = false;
        this.clicked = false;
    }

    public void update(Vec2 mouseCoord, boolean leftClickPushed, boolean leftClickPressed){
        pressed = false;
        clicked = false;
        if (leftClickPressed){
            if (Math.abs(mouseCoord.x - coord.x) < taille/2 && Math.abs(mouseCoord.y - coord.y) < taille/2){
                pressed = true;
                if (leftClickPushed){
                    clicked = true;
                }
            }
        }
    }

    /* ====== AFFICHAGE ====== */

    public void draw(Graphics2D g){
        Color bg = pressed ? new Color(241, 196, 15) : new Color(120, 135, 155);
        Draw.rect(g, coord.x - taille/2 + 4, coord.y - taille/2 + 4, taille - 8, taille - 8, bg);
        Draw.rectOutline(g, coord.x - taille/2 + 4, coord.y - taille/2 + 4, taille - 8, taille - 8,
                new Color(25, 28, 35), 2);

        // Flèche
        double cx = coord.x;
        double cy = coord.y;
        double h = taille / 4.0;
        g.setColor(new Color(20, 25, 35));
        int[] xs, ys;
        switch (sens){
            case Slide.UP -> {
                xs = new int[]{(int)(cx), (int)(cx - h), (int)(cx + h)};
                ys = new int[]{(int)(cy - h), (int)(cy + h/2), (int)(cy + h/2)};
                g.fillPolygon(xs, ys, 3);
            }
            case Slide.DOWN -> {
                xs = new int[]{(int)(cx), (int)(cx - h), (int)(cx + h)};
                ys = new int[]{(int)(cy + h), (int)(cy - h/2), (int)(cy - h/2)};
                g.fillPolygon(xs, ys, 3);
            }
            case Slide.LEFT -> {
                xs = new int[]{(int)(cx - h), (int)(cx + h/2), (int)(cx + h/2)};
                ys = new int[]{(int)(cy), (int)(cy - h), (int)(cy + h)};
                g.fillPolygon(xs, ys, 3);
            }
            case Slide.RIGHT -> {
                xs = new int[]{(int)(cx + h), (int)(cx - h/2), (int)(cx - h/2)};
                ys = new int[]{(int)(cy), (int)(cy - h), (int)(cy + h)};
                g.fillPolygon(xs, ys, 3);
            }
        }
    }
}
