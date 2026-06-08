package com.sae.enigmas;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Window;

import com.sae.core.Save;

/**
 * Phase 5.2 — "Boules" : déverrouille le bon tiroir de Jacques.
 */
public class MovingBallsUI extends EnigmaDialog {

    private final MovingBalls enigme;
    private static final int W = 720;
    private static final int H = 720;

    public MovingBallsUI(Window parent, Save save) {
        super(parent, "Verrou à billes - Tiroir de Jacques", W, H);
        this.enigme = buildEnigme(save != null ? save.getDifficulty() : "Normal");
        setStatus("Cliquez sur les boutons périphériques pour faire glisser les billes jusqu'aux cibles vertes.",
                new Color(41, 128, 185));
    }

    private MovingBalls buildEnigme(String diff) {
        int N = "Hard".equalsIgnoreCase(diff) ? 5 : 4;
        Slide[][] slides = new Slide[N][N];
        for (int j = 0; j < N; j++) for (int i = 0; i < N; i++) slides[j][i] = new Slide("CROSS");
        int[][] balls = new int[][]{ {0,0}, {N-1,0}, {0,N-1} };
        int[][] goals = new int[][]{ {N-1,N-1}, {N-1,0}, {0,N-1} };
        return new MovingBalls(new Vec2(W / 2.0, H / 2.0 - 10), 460, balls, goals, slides);
    }

    @Override
    protected void onMousePressed(Vec2 p) {
        if (enigme.win) return;
        enigme.update(p, true, true);
        if (enigme.win) {
            setStatus("Tiroir déverrouillé !", new Color(46, 204, 113));
            markSolvedAndClose();
        }
    }

    @Override
    protected void onMouseReleased(Vec2 p) {
        enigme.update(p, false, false);
    }

    @Override
    protected void paintEnigme(Graphics2D g, int w, int h) {
        enigme.draw(g);
        g.setColor(new Color(180, 185, 195));
        g.setFont(new Font("SansSerif", Font.ITALIC, 12));
        g.drawString("Toutes les billes doivent reposer sur une case 'objectif' (point vert).",
                20, h - 18);
    }
}
