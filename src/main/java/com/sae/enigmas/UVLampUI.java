package com.sae.enigmas;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Window;

/**
 * Phase 5.2 (optionnelle) — "Lampe UV" : indication du bon tiroir au plafond.
 * Visuel non finalisé : ouverture, attente courte, autosolve.
 */
public class UVLampUI extends EnigmaDialog {

    private static final int W = 560;
    private static final int H = 360;
    private final Rectangle btnAuto = new Rectangle(W/2 - 110, 240, 220, 42);
    private long start = System.currentTimeMillis();

    public UVLampUI(Window parent) {
        super(parent, "Lampe UV - Plafond de Jacques", W, H);
        setStatus("Vous balayez le plafond avec la lampe UV...", new Color(108, 92, 231));
    }

    @Override
    protected void tick() {
        // Autosolve après 2,5s ou clic sur le bouton.
        if (System.currentTimeMillis() - start > 2500 && !reussite) {
            setStatus("Marquage révélé : tiroir n°2.", new Color(46, 204, 113));
            markSolvedAndClose();
        }
    }

    @Override
    protected void onMousePressed(Vec2 p) {
        if (btnAuto.contains((int) p.x, (int) p.y)) {
            setStatus("Marquage révélé : tiroir n°2.", new Color(46, 204, 113));
            markSolvedAndClose();
        }
    }

    @Override
    protected boolean isCursorInteractive(Vec2 p) {
        return btnAuto.contains((int) p.x, (int) p.y);
    }

    @Override
    protected void paintEnigme(Graphics2D g, int w, int h) {
        // Faux plafond
        Draw.rect(g, 30, 30, w - 60, 180, new Color(18, 18, 30));
        Draw.rectOutline(g, 30, 30, w - 60, 180, new Color(108, 92, 231), 2);

        // Cône UV (rayon souris)
        int mx = (int) mousePos.x, my = (int) mousePos.y;
        g.setColor(new Color(140, 100, 255, 70));
        g.fillOval(mx - 90, my - 90, 180, 180);
        g.setColor(new Color(170, 140, 255, 120));
        g.fillOval(mx - 50, my - 50, 100, 100);

        // Marquage caché révélé sous le cône s'il est dans la zone
        Rectangle marquage = new Rectangle(W/2 - 30, 110, 60, 40);
        if (marquage.intersects(new Rectangle(mx - 90, my - 90, 180, 180))) {
            g.setColor(new Color(180, 150, 255));
            g.setFont(new Font("Monospaced", Font.BOLD, 22));
            g.drawString("TIROIR 2", marquage.x - 12, marquage.y + 26);
        }

        // Bouton autosolve (failback : utile si pas de visuel finalisé)
        g.setColor(new Color(108, 92, 231));
        g.fillRoundRect(btnAuto.x, btnAuto.y, btnAuto.width, btnAuto.height, 14, 14);
        g.setStroke(new BasicStroke(2));
        g.setColor(Color.WHITE);
        g.drawRoundRect(btnAuto.x, btnAuto.y, btnAuto.width, btnAuto.height, 14, 14);
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        String t = "Continuer (vous avez vu le marquage)";
        int tw = g.getFontMetrics().stringWidth(t);
        g.drawString(t, btnAuto.x + (btnAuto.width - tw) / 2, btnAuto.y + 27);
    }
}
