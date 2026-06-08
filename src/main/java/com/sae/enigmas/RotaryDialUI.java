package com.sae.enigmas;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Window;

import com.sae.core.Save;

/**
 * Phase 3.1 — "Cadran Rotatif" : déverrouillage de la porte de la chambre de Louis.
 * Le RotaryDial sous-jacent gère toute la logique.
 */
public class RotaryDialUI extends EnigmaDialog {

    private final RotaryDial dial;
    private final int W = 640;
    private final int H = 560;

    public RotaryDialUI(Window parent, Save save) {
        super(parent, "Verrou rotatif - Porte de Louis", 640, 600);
        String diff = (save != null && save.getDifficulty() != null) ? save.getDifficulty() : "Normal";
        this.dial = RotaryDial.create(diff, new Vec2(W / 2.0, H / 2.0 - 10), 210, 0.55);
        setStatus("Tournez le cadran pour aligner les secteurs verts. Évitez les pièges.",
                new Color(41, 128, 185));
    }

    @Override
    protected void onMousePressed(Vec2 p) {
        if (dial == null) return;
        if (dial.win) return;
        dial.update(p, true);
        if (dial.win) {
            setStatus("Porte déverrouillée !", new Color(46, 204, 113));
            markSolvedAndClose();
        }
    }

    @Override
    protected void paintEnigme(Graphics2D g, int w, int h) {
        if (dial == null) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 18));
            g.drawString("Erreur : énigme non chargée.", 20, 40);
            return;
        }
        dial.draw(g);
        g.setColor(new Color(180, 185, 195));
        g.setFont(new Font("SansSerif", Font.ITALIC, 12));
        g.drawString("Cliquez sur un secteur jaune pour avancer. Cliquer sur un secteur sombre réinitialise.",
                20, h - 16);
    }

    @Override
    protected boolean isCursorInteractive(Vec2 p) {
        if (dial == null) return false;
        double d = p.distanceTo(dial.coord);
        return d > dial.rayon_int && d < dial.rayon_ext;
    }
}
