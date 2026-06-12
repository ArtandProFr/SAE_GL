package com.sae.enigmas;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Window;

import com.sae.core.Save;

/**
 * Phase 3.5 — "Lumières" : faire correspondre les couleurs adjacentes.
 */
public class MovingLightsUI extends EnigmaDialog {

    private final MovingLights enigme;
    private static final int W = 640;
    private static final int H = 640;
    private Save save = null;

    public MovingLightsUI(Window parent, Save save) {
        super(parent, "Tableau électrique - Ampoules", W, H);
        this.save = save;
        this.enigme = buildEnigme(save != null ? save.getDifficulty() : "Normal");
        setStatus("Déplacez les ampoules sur les rails.",
                new Color(41, 128, 185));
    }

    private MovingLights buildEnigme(String diff) {
        if ("Easy".equals(save.getDifficulty()) || "Normal".equals(save.getDifficulty()) || "Hard".equals(save.getDifficulty())){
            int N = 7;
            Slide[][] slides = new Slide[N][N];
            
            // Ligne 0
            slides[0][0] = new Slide("DOWN_RIGHT");
            slides[0][1] = new Slide("HORI");
            slides[0][2] = new Slide("HORI");
            slides[0][3] = new Slide("LEFT");
            slides[0][4] = new Slide("DOWN");
            slides[0][5] = new Slide("DOWN");
            slides[0][6] = new Slide("DOWN");
            
            // Ligne 1
            slides[1][0] = new Slide("UP");
            slides[1][1] = new Slide("DOWN_RIGHT");
            slides[1][2] = new Slide("T_DOWN");
            slides[1][3] = new Slide("LEFT");
            slides[1][4] = new Slide("VERT");
            slides[1][5] = new Slide("VERT");
            slides[1][6] = new Slide("UP");
            
            // Ligne 2
            slides[2][0] = new Slide("DOWN");
            slides[2][1] = new Slide("T_RIGHT");
            slides[2][2] = new Slide("UP_LEFT");
            slides[2][3] = new Slide("DOWN_RIGHT");
            slides[2][4] = new Slide("T_LEFT");
            slides[2][5] = new Slide("T_RIGHT");
            slides[2][6] = new Slide("LEFT");
            
            // Ligne 3
            slides[3][0] = new Slide("VERT");
            slides[3][1] = new Slide("UP");
            slides[3][2] = new Slide("RIGHT");
            slides[3][3] = new Slide("T_UP");
            slides[3][4] = new Slide("UP_LEFT");
            slides[3][5] = new Slide("UP");
            slides[3][6] = new Slide("DOWN");
            
            // Ligne 4
            slides[4][0] = new Slide("T_RIGHT");
            slides[4][1] = new Slide("LEFT");
            slides[4][2] = new Slide("DOWN");
            slides[4][3] = new Slide("RIGHT");
            slides[4][4] = new Slide("HORI");
            slides[4][5] = new Slide("HORI");
            slides[4][6] = new Slide("UP_LEFT");
            
            // Ligne 5
            slides[5][0] = new Slide("UP");
            slides[5][1] = new Slide("DOWN");
            slides[5][2] = new Slide("UP");
            slides[5][3] = new Slide("DOWN");
            slides[5][4] = new Slide("DOWN_RIGHT");
            slides[5][5] = new Slide("T_DOWN");
            slides[5][6] = new Slide("DOWN_LEFT");
            
            // Ligne 6
            slides[6][0] = new Slide("RIGHT");
            slides[6][1] = new Slide("T_UP");
            slides[6][2] = new Slide("HORI");
            slides[6][3] = new Slide("UP_LEFT");
            slides[6][4] = new Slide("UP_RIGHT");
            slides[6][5] = new Slide("T_UP");
            slides[6][6] = new Slide("UP_LEFT");

            // Sliders correspondants au format { X, Y, Slider }
            Object[][] sliders = new Object[][]{
                { 1, 0, new Slider(Slider.BLUE)   },
                { 4, 0, new Slider(Slider.RED)    },
                { 0, 1, new Slider(Slider.RED)    },
                { 1, 1, new Slider(Slider.BLUE)   },
                { 3, 1, new Slider(Slider.YELLOW) },
                { 6, 1, new Slider(Slider.YELLOW) },
                { 2, 2, new Slider(Slider.RED)    },
                { 4, 2, new Slider(Slider.YELLOW) },
                { 6, 2, new Slider(Slider.RED)    },
                { 6, 3, new Slider(Slider.BLUE)   },
                { 1, 4, new Slider(Slider.RED)    },
                { 2, 4, new Slider(Slider.YELLOW) },
                { 4, 5, new Slider(Slider.YELLOW) },
                { 5, 5, new Slider(Slider.RED)    },
                { 6, 5, new Slider(Slider.YELLOW) },
                { 0, 6, new Slider(Slider.BLUE)   }
            };

            // Taille totale réhaussée à 460 pour que la grille 7x7 ne soit pas trop serrée
            return new MovingLights(new Vec2(W / 2.0, H / 2.0 - 50), 460, sliders, slides);
        } else {
            return null;
        }
    }

    @Override
    protected void onMousePressed(Vec2 p) {
        if (enigme.win) return;
        // Clic enfoncé : leftClick = true, leftRelease = false
        enigme.update(p, true, false);
    }

    @Override
    protected void onMouseDragged(Vec2 p) {
        if (enigme.win) return;
        // On glisse : leftClick = false, leftRelease = false
        enigme.update(p, false, false);
    }

    @Override
    protected void onMouseReleased(Vec2 p) {
        // Relâché : leftClick = false, leftRelease = true
        enigme.update(p, false, true);
        
        if (enigme.win) {
            setStatus("Toutes les lampes sont alimentées : courant rétabli !", new Color(46, 204, 113));
            markSolvedAndClose();
        }
    }

    @Override
    protected void paintEnigme(Graphics2D g, int w, int h) {
        enigme.draw(g);
        if (save.getDifficulty().equals("Easy")){
            g.setColor(new Color(180, 185, 195));
            g.setFont(new Font("SansSerif", Font.ITALIC, 12));
            g.drawString("Glissez-déposez les ampoules. Celles adjacentes de couleurs différentes s'éteignent.",
                    20, h - 15);
        }
    }
}
