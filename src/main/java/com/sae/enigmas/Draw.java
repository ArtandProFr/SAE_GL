package com.sae.enigmas;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;

/**
 * Classe utilitaire de dessin partagée par toutes les énigmes.
 * Traduction des helpers Python (Module_Pygame) vers Java2D.
 * Toutes les méthodes acceptent un Graphics2D déjà configuré.
 */
public final class Draw {

    private Draw() {}

    /** Active l'antialiasing sur g2d. À appeler une fois en début de rendu. */
    public static void setupQuality(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,        RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,           RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,       RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,   RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    /** Parse une couleur hex "#RRGGBB" ou "#AARRGGBB". */
    public static Color color(String hex) {
        if (hex == null) return Color.BLACK;
        String h = hex.startsWith("#") ? hex.substring(1) : hex;
        try {
            if (h.length() == 6) return new Color(Integer.parseInt(h, 16));
            if (h.length() == 8) {
                int a = Integer.parseInt(h.substring(0, 2), 16);
                int r = Integer.parseInt(h.substring(2, 4), 16);
                int g = Integer.parseInt(h.substring(4, 6), 16);
                int b = Integer.parseInt(h.substring(6, 8), 16);
                return new Color(r, g, b, a);
            }
        } catch (NumberFormatException ignore) {}
        return Color.BLACK;
    }

    /**
     * Interpole vers le noir/blanc. Reproduit mod.transition(col, "#000000", pct)
     * pct = 0   -> col d'origine
     * pct = 100 -> couleur cible
     */
    public static Color transition(String fromHex, String toHex, int pct) {
        return transition(color(fromHex), color(toHex), pct);
    }

    public static Color transition(Color from, Color to, int pct) {
        double t = Math.max(0, Math.min(100, pct)) / 100.0;
        int r = (int) Math.round(from.getRed()   + (to.getRed()   - from.getRed())   * t);
        int g = (int) Math.round(from.getGreen() + (to.getGreen() - from.getGreen()) * t);
        int b = (int) Math.round(from.getBlue()  + (to.getBlue()  - from.getBlue())  * t);
        return new Color(clamp(r), clamp(g), clamp(b));
    }

    private static int clamp(int v) { return Math.max(0, Math.min(255, v)); }

    /** Disque plein. */
    public static void circle(Graphics2D g, double cx, double cy, double r, Color col) {
        g.setColor(col);
        int d = (int) Math.round(2 * r);
        g.fillOval((int) Math.round(cx - r), (int) Math.round(cy - r), d, d);
    }

    /** Cercle évidé (uniquement contour). */
    public static void circle(Graphics2D g, double cx, double cy, double r, Color col, double lineWidth) {
        g.setColor(col);
        g.setStroke(new BasicStroke((float) lineWidth));
        int d = (int) Math.round(2 * r);
        g.drawOval((int) Math.round(cx - r), (int) Math.round(cy - r), d, d);
    }

    public static void rect(Graphics2D g, double x, double y, double w, double h, Color col) {
        g.setColor(col);
        g.fillRect((int) Math.round(x), (int) Math.round(y),
                   (int) Math.round(w), (int) Math.round(h));
    }

    public static void rectOutline(Graphics2D g, double x, double y, double w, double h, Color col, double lineW) {
        g.setColor(col);
        g.setStroke(new BasicStroke((float) lineW));
        g.drawRect((int) Math.round(x), (int) Math.round(y),
                   (int) Math.round(w), (int) Math.round(h));
    }

    public static void line(Graphics2D g, double x1, double y1, double x2, double y2, Color col, double w) {
        g.setColor(col);
        g.setStroke(new BasicStroke((float) w, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine((int) Math.round(x1), (int) Math.round(y1),
                   (int) Math.round(x2), (int) Math.round(y2));
    }

    /**
     * Portion de couronne (cercle plein entre rayon_int et rayon_ext, angle a1..a2 en degrés).
     * Conventions identiques à pygame (0° = est, anti-horaire).
     */
    public static void portionCouronne(Graphics2D g, double cx, double cy,
                                       double rayonInt, double rayonExt,
                                       double a1, double a2, Color col) {
        double extent = a2 - a1;
        Path2D.Double path = new Path2D.Double();
        Arc2D.Double outer = new Arc2D.Double(cx - rayonExt, cy - rayonExt, 2 * rayonExt, 2 * rayonExt,
                                              a1, extent, Arc2D.OPEN);
        Arc2D.Double inner = new Arc2D.Double(cx - rayonInt, cy - rayonInt, 2 * rayonInt, 2 * rayonInt,
                                              a2, -extent, Arc2D.OPEN);
        path.append(outer, false);
        path.append(inner, true);
        path.closePath();
        g.setColor(col);
        g.fill(path);
    }

    /** Arc (contour). */
    public static void portionCercle(Graphics2D g, double cx, double cy, double rayon,
                                     double a1, double a2, Color col, double width) {
        g.setColor(col);
        g.setStroke(new BasicStroke((float) width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Arc2D.Double(cx - rayon, cy - rayon, 2 * rayon, 2 * rayon, a1, a2 - a1, Arc2D.OPEN));
    }

    /** Texte centré sur (x,y). */
    public static void textCentered(Graphics2D g, double x, double y, String txt, Color col, int size) {
        g.setColor(col);
        g.setFont(g.getFont().deriveFont((float) size));
        java.awt.FontMetrics fm = g.getFontMetrics();
        int tw = fm.stringWidth(txt);
        int th = fm.getAscent();
        g.drawString(txt, (int) Math.round(x - tw / 2.0), (int) Math.round(y + th / 2.0));
    }
}
