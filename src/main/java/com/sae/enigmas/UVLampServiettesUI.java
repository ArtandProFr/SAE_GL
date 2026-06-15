package com.sae.enigmas;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Window;

/**
 * Phase 4.3 — Examen des serviettes de la salle de bain à la lampe UV.
 * <p>
 * Six serviettes brodées des initiales des colocataires (JA., PA., Pi., LD.,
 * TH., FR.) sont disposées sur un mur. Le joueur balaie le mur avec la lampe ;
 * une tache suspecte n'est visible que sous le cône UV sur la serviette de
 * Jacques (JA.). Un bouton "Continuer" permet de quitter la scène une fois la
 * tache révélée (ou en cas de difficulté visuelle).
 */
public class UVLampServiettesUI extends EnigmaDialog {

    private static final int W = 640;
    private static final int H = 460;

    private final Rectangle btnContinuer = new Rectangle(W/2 - 110, H - 70, 220, 32);

    /** Indique si le joueur a éclairé la serviette de Jacques au moins une fois. */
    private boolean tacheRevelee = false;

    /** Six serviettes : libellé + zone à l'écran (en coordonnées du panneau interne). */
    private final String[] initiales = { "JA.", "PA.", "Pi.", "LD.", "TH.", "FR." };
    private final Rectangle[] serviettes = new Rectangle[6];
    /** Index de la serviette portant la tache (JA. = 0). */
    private static final int IDX_JACQUES = 0;

    public UVLampServiettesUI(Window parent) {
        super(parent, "Lampe UV - Serviettes de la salle de bain", W, H);
        setStatus("Balayez les serviettes avec la lampe UV...", new Color(108, 92, 231));

        // Disposition des six serviettes en grille 2 colonnes x 3 lignes
        int gridX = 80, gridY = 70;
        int cellW = 200, cellH = 90;
        int gapX = 40,  gapY = 16;
        for (int i = 0; i < 6; i++) {
            int col = i % 2;
            int row = i / 2;
            int x = gridX + col * (cellW + gapX);
            int y = gridY + row * (cellH + gapY);
            serviettes[i] = new Rectangle(x, y, cellW, cellH);
        }
    }

    @Override
    protected void tick() { /* rien : tout est piloté par la souris */ }

    @Override
    protected void onMousePressed(Vec2 p) {
        if (btnContinuer.contains((int) p.x, (int) p.y)) {
            if (tacheRevelee) {
                setStatus("Tache identifiée sur la serviette de Jacques (JA.).",
                          new Color(46, 204, 113));
            } else {
                setStatus("Vous quittez la salle de bain.", new Color(200, 200, 200));
            }
            markSolvedAndClose();
        }
    }

    @Override
    protected boolean isCursorInteractive(Vec2 p) {
        return btnContinuer.contains((int) p.x, (int) p.y);
    }

    @Override
    protected void paintEnigme(Graphics2D g, int w, int h) {
        // Mur sombre derrière les serviettes (ambiance "pièce éteinte")
        g.setColor(new Color(20, 22, 30));
        g.fillRect(20, 40, w - 40, h - 130);
        g.setColor(new Color(108, 92, 231));
        g.setStroke(new BasicStroke(2));
        g.drawRect(20, 40, w - 40, h - 130);

        // Position du cône UV (autour de la souris)
        int mx = (int) mousePos.x, my = (int) mousePos.y;
        Rectangle coneBounds = new Rectangle(mx - 80, my - 80, 160, 160);

        // Dessin des six serviettes
        g.setFont(new Font("Monospaced", Font.BOLD, 18));
        FontMetrics fm = g.getFontMetrics();
        for (int i = 0; i < 6; i++) {
            Rectangle r = serviettes[i];
            // Serviette : tissu clair
            g.setColor(new Color(230, 230, 235));
            g.fillRoundRect(r.x, r.y, r.width, r.height, 12, 12);
            // Bandes vertes décoratives
            g.setColor(new Color(120, 180, 140));
            g.fillRect(r.x, r.y + r.height - 18, r.width, 4);
            g.fillRect(r.x, r.y + r.height - 10, r.width, 4);
            // Initiales brodées
            g.setColor(new Color(80, 140, 100));
            String txt = initiales[i];
            g.drawString(txt, r.x + (r.width - fm.stringWidth(txt)) / 2,
                              r.y + r.height / 2 + 6);
        }

        // Cône UV semi-transparent (toujours visible quand la souris est sur le mur)
        g.setColor(new Color(140, 100, 255, 70));
        g.fillOval(mx - 80, my - 80, 160, 160);
        g.setColor(new Color(170, 140, 255, 120));
        g.fillOval(mx - 40, my - 40, 80, 80);

        // Tache UV révélée UNIQUEMENT sur JA. ET sous le cône
        Rectangle servJacques = serviettes[IDX_JACQUES];
        if (servJacques.intersects(coneBounds)) {
            tacheRevelee = true;
            // Tache fluorescente violet/blanc
            int tx = servJacques.x + 30;
            int ty = servJacques.y + 22;
            g.setColor(new Color(200, 170, 255, 220));
            g.fillOval(tx, ty, 36, 24);
            g.setColor(new Color(255, 255, 255, 200));
            g.fillOval(tx + 6, ty + 4, 18, 12);
            g.setColor(new Color(180, 150, 255));
            g.setFont(new Font("SansSerif", Font.BOLD, 11));
            g.drawString("Tache suspecte", tx - 2, ty + 38);
        }

        // Bouton "Continuer"
        g.setColor(tacheRevelee ? new Color(46, 204, 113) : new Color(108, 92, 231));
        g.fillRoundRect(btnContinuer.x, btnContinuer.y,
                        btnContinuer.width, btnContinuer.height, 14, 14);
        g.setStroke(new BasicStroke(2));
        g.setColor(Color.WHITE);
        g.drawRoundRect(btnContinuer.x, btnContinuer.y,
                        btnContinuer.width, btnContinuer.height, 14, 14);
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        String t = tacheRevelee ? "J'ai vu — Continuer" : "Continuer";
        int tw = g.getFontMetrics().stringWidth(t);
        g.drawString(t, btnContinuer.x + (btnContinuer.width - tw) / 2,
                        btnContinuer.y + 20);
    }

    /** @return true si la tache a effectivement été révélée pendant la scène. */
    public boolean isTacheRevelee() { return tacheRevelee; }
}
