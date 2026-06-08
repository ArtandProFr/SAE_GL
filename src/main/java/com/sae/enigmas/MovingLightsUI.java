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

    public MovingLightsUI(Window parent, Save save) {
        super(parent, "Tableau de répartition - Phase 2/2", W, H);
        this.enigme = buildEnigme(save != null ? save.getDifficulty() : "Normal");
        setStatus("Déplacez les ampoules sur les rails. Chaque ampoule doit être seule de sa couleur dans son voisinage.",
                new Color(41, 128, 185));
    }

    private MovingLights buildEnigme(String diff) {
        // Grille 3x3, sliders colorés à déplacer sur des slides en CROIX
        Slide[][] slides = new Slide[3][3];
        for (int j = 0; j < 3; j++) for (int i = 0; i < 3; i++) slides[j][i] = new Slide("CROSS");
        Object[][] sliders = new Object[][]{
                { 0, 0, new Slider(Slider.RED)    },
                { 2, 0, new Slider(Slider.BLUE)   },
                { 1, 1, new Slider(Slider.YELLOW) },
                { 0, 2, new Slider(Slider.BLUE)   },
                { 2, 2, new Slider(Slider.RED)    }
        };
        return new MovingLights(new Vec2(W / 2.0, H / 2.0 - 10), 380, sliders, slides);
    }

    @Override
    protected void onMousePressed(Vec2 p) {
        if (enigme.win) return;
        enigme.update(p, true, true);
    }

    @Override
    protected void onMouseDragged(Vec2 p) {
        if (enigme.win) return;
        enigme.update(p, false, true);
    }

    @Override
    protected void onMouseReleased(Vec2 p) {
        if (enigme.win) {
            setStatus("Toutes les lampes sont alimentées : courant rétabli !", new Color(46, 204, 113));
            markSolvedAndClose();
            return;
        }
        enigme.update(p, false, false);
        if (enigme.win) {
            setStatus("Toutes les lampes sont alimentées : courant rétabli !", new Color(46, 204, 113));
            markSolvedAndClose();
        }
    }

    @Override
    protected void paintEnigme(Graphics2D g, int w, int h) {
        enigme.draw(g);
        g.setColor(new Color(180, 185, 195));
        g.setFont(new Font("SansSerif", Font.ITALIC, 12));
        g.drawString("Glissez-déposez les ampoules. Adjacentes : pas de couleur identique en diagonale/voisinage.",
                20, h - 18);
    }
}
