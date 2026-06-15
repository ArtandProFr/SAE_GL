package com.sae.enigmas;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

import com.sae.core.Save;

/**
 * Phase 4.1 — "Téléphone : Correspondances Ondes".
 * <p>
 * Le joueur ajuste un ensemble de potards (paramètres : amplitude, phase,
 * et parfois fréquence d'une sous-onde) pour faire correspondre <b>son</b>
 * signal au signal cible affiché en rouge.
 * <p>
 * Difficultés :
 * <ul>
 * <li><b>Easy</b>   : 1 onde sinusoïdale, 2 potards (amplitude + phase).</li>
 * <li><b>Normal</b> : 3 ondes affichées séparément, chacune composée
 * (sinus + dent-de-scie / triangle / carré). Pour chaque onde, 2-3
 * potards (amplitude des sous-ondes, phase commune). Les fréquences des
 * sous-ondes au sein d'une même onde sont identiques.</li>
 * <li><b>Hard</b>   : 1 onde affichée, mais composée de plusieurs sous-ondes
 * avec des fréquences/phases <i>indépendantes</i>. Tous les potards
 * modifient cette unique onde résultante.</li>
 * </ul>
 */
public class OndesUI extends EnigmaDialog {

    private static final int W = 960;
    private static final int H = 730;      // Dimension réelle de la fenêtre
    private static final int H_JEU = 660;  // Hauteur virtuelle max pour remonter les composants

    /* ─── Modèle des sous-ondes ────────────────────────────────────────────── */

    private enum Shape { SIN, SAW, SQUARE, TRI }

    /** Une sous-onde paramétrable (les paramètres modifiables sont identifiés par un Knob). */
    private static final class Subwave {
        double amp;
        double freq;
        double phase; // en radians
        Shape shape;
        Subwave(double a, double f, double p, Shape s) { amp = a; freq = f; phase = p; shape = s; }
        double sample(double t) {
            double x = freq * t + phase;
            return switch (shape) {
                case SIN    -> amp * Math.sin(x);
                case SAW    -> amp * (2 * ((x / (2 * Math.PI)) - Math.floor(0.5 + (x / (2 * Math.PI)))));
                case SQUARE -> amp * (Math.sin(x) >= 0 ? 1 : -1);
                case TRI    -> amp * (2 / Math.PI) * Math.asin(Math.sin(x));
            };
        }
    }

    /** Une onde = un panneau d'affichage = une liste de sous-ondes sommées. */
    private static final class Wave {
        final List<Subwave> subs = new ArrayList<>();
        double sample(double t) { double s = 0; for (Subwave w : subs) s += w.sample(t); return s; }
    }

    /** Un potard : modifie un paramètre d'une sous-onde précise. */
    private static class Knob {
        final Wave wave;
        final int subIndex;
        final String param;   // "amp" | "phase" | "freq"
        final String label;
        final double step;
        final double min, max;
        Rectangle btnDown = new Rectangle();
        Rectangle btnUp   = new Rectangle();
        Rectangle disp    = new Rectangle();
        Knob(Wave w, int sub, String p, String lbl, double step, double min, double max) {
            this.wave = w; this.subIndex = sub; this.param = p;
            this.label = lbl; this.step = step; this.min = min; this.max = max;
        }
        double get() {
            Subwave s = wave.subs.get(subIndex);
            return switch (param) { case "amp" -> s.amp; case "phase" -> s.phase; case "freq" -> s.freq; default -> 0; };
        }
        void set(double v) {
            v = Math.max(min, Math.min(max, v));
            Subwave s = wave.subs.get(subIndex);
            switch (param) { case "amp" -> s.amp = v; case "phase" -> s.phase = v; case "freq" -> s.freq = v; }
        }
        void inc(int sign) { set(get() + sign * step); }
    }

    /* ─── État du puzzle ───────────────────────────────────────────────────── */

    private final List<Wave>  joueurWaves = new ArrayList<>();
    private final List<Wave>  cibleWaves  = new ArrayList<>();
    private final List<Knob>  knobs       = new ArrayList<>();
    /** Pour chaque potard, la valeur cible que le joueur doit retrouver. */
    private final List<Double> knobsTarget = new ArrayList<>();

    // Remonté à Y = 610 pour être pile au-dessus du bandeau noir d'EnigmaDialog
    private final Rectangle btnTest = new Rectangle(W / 2 - 110, H_JEU - 82, 220, 38);
    private final String difficulte;
    private double timeAnim = 0; // pour effet visuel léger

    public OndesUI(Window parent, Save save) {
        super(parent, "Décrocher - Analyse du signal", W, H);
        difficulte = (save != null && save.getDifficulty() != null) ? save.getDifficulty() : "Normal";
        construire(difficulte);
        layoutKnobs();
        setStatus("Caler le signal du joueur (bleu) sur la cible (rouge). Difficulté : " + difficulte,
                new Color(41, 128, 185));
    }

    /* ─── Construction selon difficulté ────────────────────────────────────── */

    private void construire(String diff) {
        switch (diff) {
            case "Easy" -> construireEasy();
            case "Hard" -> construireHard();
            default     -> construireNormal();
        }
    }

    private void construireEasy() {
        Wave target = new Wave();
        double tAmp   = round1(1 + Math.random() * 3);
        double tPhase = round1((Math.random() * 2 - 1) * Math.PI);
        target.subs.add(new Subwave(tAmp, 1.0, tPhase, Shape.SIN));
        cibleWaves.add(target);

        Wave player = new Wave();
        player.subs.add(new Subwave(2.0, 1.0, 0.0, Shape.SIN));
        joueurWaves.add(player);

        knobs.add(new Knob(player, 0, "amp",   "Ampl.",   0.1, 0.1, 5));
        knobsTarget.add(tAmp);
        knobs.add(new Knob(player, 0, "phase", "Phase",   0.1, -Math.PI, Math.PI));
        knobsTarget.add(tPhase);
    }

    private void construireNormal() {
        Shape[] formes  = { Shape.SAW, Shape.TRI, Shape.SQUARE };
        int[]   harmons = { 2,         3,         2            };
        for (int i = 0; i < 1; i++) {
            Shape sub2 = formes[i];
            int harm   = harmons[i];
            double f1  = 1.0 + i * 0.5;
            double f2  = f1 * harm;

            Wave tgt = new Wave();
            double tA1 = round1(0.5 + Math.random() * 2.5);
            double tA2 = round1(0.5 + Math.random() * 2.0);
            double tPh = round1((Math.random() * 2 - 1) * Math.PI);
            tgt.subs.add(new Subwave(tA1, f1, tPh, Shape.SIN));
            tgt.subs.add(new Subwave(tA2, f2, tPh, sub2));
            cibleWaves.add(tgt);

            Wave pl = new Wave();
            pl.subs.add(new Subwave(1.0, f1, 0.0, Shape.SIN));
            pl.subs.add(new Subwave(1.0, f2, 0.0, sub2));
            joueurWaves.add(pl);

            knobs.add(new Knob(pl, 0, "amp",   "A" + (i+1) + ".1 (sin)",     0.1, 0.1, 4));
            knobsTarget.add(tA1);
            knobs.add(new Knob(pl, 1, "amp",   "A" + (i+1) + ".2 (" + nameOf(sub2) + ")", 0.1, 0.1, 4));
            knobsTarget.add(tA2);
            knobs.add(new Knob(pl, 0, "phase", "φ" + (i+1),            0.1, -Math.PI, Math.PI) {
                @Override void set(double v) {
                    super.set(v);
                    wave.subs.get(1).phase = v;
                }
            });
            knobsTarget.add(tPh);
        }
    }

    private static String nameOf(Shape s) {
        return switch (s) { case SIN -> "sin"; case SAW -> "saw"; case TRI -> "tri"; case SQUARE -> "sqr"; };
    }

    private void construireHard() {
        Wave target = new Wave();
        double f1 = round1(0.7 + Math.random() * 0.6);
        double f2 = round1(1.5 + Math.random() * 0.6);
        double f3 = round1(2.5 + Math.random() * 0.6);
        double tA1 = round1(0.5 + Math.random() * 1.5);
        double tA2 = round1(0.3 + Math.random() * 1.2);
        double tA3 = round1(0.3 + Math.random() * 1.0);
        double tP1 = round1((Math.random() * 2 - 1) * Math.PI);
        double tP2 = round1((Math.random() * 2 - 1) * Math.PI);
        double tP3 = round1((Math.random() * 2 - 1) * Math.PI);
        target.subs.add(new Subwave(tA1, f1, tP1, Shape.SIN));
        target.subs.add(new Subwave(tA2, f2, tP2, Shape.SAW));
        target.subs.add(new Subwave(tA3, f3, tP3, Shape.TRI));
        cibleWaves.add(target);

        Wave pl = new Wave();
        pl.subs.add(new Subwave(1.0, f1, 0.0, Shape.SIN));
        pl.subs.add(new Subwave(1.0, f2, 0.0, Shape.SAW));
        pl.subs.add(new Subwave(1.0, f3, 0.0, Shape.TRI));
        joueurWaves.add(pl);

        Object[][] cfg = {
                {0, "amp",   "A sin",  tA1},
                {0, "phase", "φ sin",  tP1},
                {1, "amp",   "A saw",  tA2},
                {1, "phase", "φ saw",  tP2},
                {2, "amp",   "A tri",  tA3},
                {2, "phase", "φ tri",  tP3}
        };
        for (Object[] c : cfg) {
            int sub = (int) c[0];
            String param = (String) c[1];
            String lbl = (String) c[2];
            double tgt = (double) c[3];
            double step = "phase".equals(param) ? 0.1 : 0.1;
            double mn   = "phase".equals(param) ? -Math.PI : 0.1;
            double mx   = "phase".equals(param) ?  Math.PI : 3;
            knobs.add(new Knob(pl, sub, param, lbl, step, mn, mx));
            knobsTarget.add(tgt);
        }
    }

    /* ─── Mise en page des potards ─────────────────────────────────────────── */

    private void layoutKnobs() {
        int n = knobs.size();
        int totalW = W - 80;
        int slot = totalW / n;
        // Aligné par rapport à la hauteur virtuelle de 660 au lieu de 730
        int y = H_JEU - 170; 
        for (int i = 0; i < n; i++) {
            Knob k = knobs.get(i);
            int cx = 40 + i * slot + slot / 2;
            k.btnDown = new Rectangle(cx - 50, y, 40, 32);
            k.btnUp   = new Rectangle(cx + 10, y, 40, 32);
            k.disp    = new Rectangle(cx - 50, y + 40, 100, 26);
        }
    }

    /* ─── Logique du test/validation ───────────────────────────────────────── */

    @Override
    protected void onMousePressed(Vec2 p) {
        int mx = (int) p.x, my = (int) p.y;
        for (Knob k : knobs) {
            if (k.btnDown.contains(mx, my)) { k.inc(-1); return; }
            if (k.btnUp.contains(mx, my))   { k.inc(+1); return; }
        }
        if (btnTest.contains(mx, my)) tenter();
    }

    private void tenter() {
        boolean ok = true;
        for (int i = 0; i < knobs.size(); i++) {
            Knob k = knobs.get(i);
            double tgt = knobsTarget.get(i);
            double tol = 0.2;
            double err = (k.param.equals("phase"))
                    ? Math.abs(angleDelta(k.get(), tgt))
                    : Math.abs(k.get() - tgt);
            if (err > tol) { ok = false; break; }
        }
        if (ok) {
            setStatus("Signal synchronisé. Vous décrochez : 'Allô ?'", new Color(46, 204, 113));
            markSolvedAndClose();
        } else {
            setStatus("Le signal ne correspond pas encore.", new Color(192, 57, 43));
        }
    }

    private static double angleDelta(double a, double b) {
        double d = (a - b) % (2 * Math.PI);
        if (d > Math.PI)  d -= 2 * Math.PI;
        if (d < -Math.PI) d += 2 * Math.PI;
        return d;
    }
    private static double round1(double v) { return Math.round(v * 10) / 10.0; }

    @Override
    protected boolean isCursorInteractive(Vec2 p) {
        int mx = (int) p.x, my = (int) p.y;
        if (btnTest.contains(mx, my)) return true;
        for (Knob k : knobs) {
            if (k.btnDown.contains(mx, my) || k.btnUp.contains(mx, my)) return true;
        }
        return false;
    }

    @Override
    protected void tick() { timeAnim += 0.03; }

    /* ─── Rendu ────────────────────────────────────────────────────────────── */

    @Override
    protected void paintEnigme(Graphics2D g, int w, int h) {
        // 1) Oscilloscopes calés sur la hauteur virtuelle H_JEU (660)
        int scopeTop = 30, scopeBot = H_JEU - 200;
        if (cibleWaves.size() == 1) {
            renderScope(g, 30, scopeTop, w - 60, scopeBot - scopeTop, cibleWaves.get(0), joueurWaves.get(0), "Signal");
        } else {
            int nW = cibleWaves.size();
            int slot = (w - 30 - 30 * (nW + 1)) / nW;
            for (int i = 0; i < nW; i++) {
                int x = 30 + i * (slot + 30);
                renderScope(g, x, scopeTop, slot, scopeBot - scopeTop,
                        cibleWaves.get(i), joueurWaves.get(i), "Onde " + (i + 1));
            }
        }

        // 2) Bandeau des potards
        for (Knob k : knobs) drawKnob(g, k);

        // 3) Bouton tester (Placé à Y = 610, juste au-dessus du bandeau noir)
        g.setColor(new Color(46, 204, 113));
        g.fillRoundRect(btnTest.x, btnTest.y, btnTest.width, btnTest.height, 14, 14);
        g.setStroke(new BasicStroke(2));
        g.setColor(Color.WHITE);
        g.drawRoundRect(btnTest.x, btnTest.y, btnTest.width, btnTest.height, 14, 14);
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        String t = "Décrocher (tester le signal)";
        int tw = g.getFontMetrics().stringWidth(t);
        g.drawString(t, btnTest.x + (btnTest.width - tw) / 2, btnTest.y + 24);

        // Légende remontée légèrement pour laisser la place au bouton vert
        g.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g.setColor(new Color(231, 76, 60));
        g.drawString("● Cible", 40, H_JEU - 95);
        g.setColor(new Color(52, 152, 219));
        g.drawString("● Vous", 110, H_JEU - 95);
        g.setColor(new Color(180, 185, 195));
        g.drawString("Difficulté : " + difficulte + "  —  " + knobs.size() + " potard(s)", 200, H_JEU - 95);
    }

    private void renderScope(Graphics2D g, int x, int y, int wB, int hB, Wave tgt, Wave plr, String titre) {
        g.setColor(new Color(22, 24, 30));
        g.fillRoundRect(x, y, wB, hB, 12, 12);
        g.setColor(new Color(60, 70, 85));
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(x, y, wB, hB, 12, 12);
        g.setColor(new Color(50, 55, 65));
        g.setStroke(new BasicStroke(1));
        for (int i = 1; i < 8; i++) g.drawLine(x + i * wB / 8, y, x + i * wB / 8, y + hB);
        g.drawLine(x, y + hB / 2, x + wB, y + hB / 2);

        double tStart = 0, tEnd = 4 * Math.PI;
        drawWave(g, plr, x, y, wB, hB, tStart, tEnd, new Color(52, 152, 219));
        drawWave(g, tgt, x, y, wB, hB, tStart, tEnd, new Color(231, 76, 60, 220));

        g.setColor(new Color(220, 220, 220));
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.drawString(titre, x + 8, y + 16);
    }

    private void drawWave(Graphics2D g, Wave w, int x, int y, int wB, int hB,
                           double tStart, double tEnd, Color col) {
        Path2D.Double path = new Path2D.Double();
        double maxAmp = (hB / 2.0) - 8;
        for (int i = 0; i <= wB; i++) {
            double t = tStart + (tEnd - tStart) * i / wB;
            double v = w.sample(t);
            double norm = Math.max(-3.5, Math.min(3.5, v)) / 3.5;
            double yy = y + hB / 2.0 - norm * maxAmp;
            if (i == 0) path.moveTo(x + i, yy);
            else        path.lineTo(x + i, yy);
        }
        g.setColor(col);
        g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(path);
    }

    private void drawKnob(Graphics2D g, Knob k) {
        g.setColor(new Color(58, 65, 80));
        g.fillRoundRect(k.btnDown.x, k.btnDown.y, k.btnDown.width, k.btnDown.height, 10, 10);
        g.setColor(new Color(110, 120, 140));
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(k.btnDown.x, k.btnDown.y, k.btnDown.width, k.btnDown.height, 10, 10);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 22));
        g.drawString("-", k.btnDown.x + 15, k.btnDown.y + 25);

        g.setColor(new Color(58, 65, 80));
        g.fillRoundRect(k.btnUp.x, k.btnUp.y, k.btnUp.width, k.btnUp.height, 10, 10);
        g.setColor(new Color(110, 120, 140));
        g.drawRoundRect(k.btnUp.x, k.btnUp.y, k.btnUp.width, k.btnUp.height, 10, 10);
        g.setColor(Color.WHITE);
        g.drawString("+", k.btnUp.x + 13, k.btnUp.y + 25);

        g.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g.setColor(new Color(190, 195, 205));
        int tw = g.getFontMetrics().stringWidth(k.label);
        g.drawString(k.label, k.btnDown.x + (k.disp.width - tw) / 2, k.btnDown.y - 6);

        g.setFont(new Font("Monospaced", Font.BOLD, 13));
        g.setColor(new Color(52, 152, 219));
        String val = formatVal(k);
        int tw2 = g.getFontMetrics().stringWidth(val);
        g.drawString(val, k.disp.x + (k.disp.width - tw2) / 2, k.disp.y + 18);
    }

    private String formatVal(Knob k) {
        double v = k.get();
        if ("phase".equals(k.param)) return String.format("%+.2f rad", v);
        if ("freq".equals(k.param))  return String.format("%.2f Hz", v);
        return String.format("%.2f", v);
    }
}