package com.sae.enigmas;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Window;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sae.core.Save;

/**
 * Phase 3.4 — "Portes Logiques" (1/2 Remettre le courant).
 * <p>
 * Port en Java de {@code SAE_Classe_Enigme_GATE_Remake.py} : modèle
 * Button → Light/Gate (AND, NOT, XOR) → Light de sortie. Le joueur clique sur
 * les interrupteurs pour allumer la lampe de sortie.
 * <p>
 * Niveaux de difficulté :
 * <ul>
 *   <li><b>Easy</b>   : 1 grille (4 interrupteurs, 1 lampe)</li>
 *   <li><b>Normal</b> : 2 grilles chaînées (la sortie de la 1ère alimente la 2nde)</li>
 *   <li><b>Hard</b>   : 3 grilles chaînées</li>
 * </ul>
 */
public class LogicGateUI extends EnigmaDialog {

    private static final int W = 980;
    private static final int H = 640;

    private final List<Grid> grids = new ArrayList<>();
    private Light winComponent;
    private Light statusLight;
    private final Rectangle btnReset = new Rectangle(W - 170, 540, 140, 36);

    public LogicGateUI(Window parent, Save save) {
        super(parent, "Tableau électrique - Portes logiques", W, H);
        construire(save != null ? save.getDifficulty() : "Normal");
        setStatus("Basculez les interrupteurs pour ALLUMER la lampe finale.",
                new Color(41, 128, 185));
    }

    private void construire(String diff) {
        switch (diff == null ? "Normal" : diff) {
            case "Easy" -> {
                Grid g1 = buildGrille1(40, 40);
                grids.add(g1);
                winComponent = g1.output;
                statusLight = g1.output;
            }
            case "Hard" -> {
                Grid g1 = buildGrille1(40, 40);
                Grid g2 = buildGrille2(360, 40, g1);
                Grid g3 = buildGrille3(680, 40, g2);
                grids.add(g1); grids.add(g2); grids.add(g3);
                winComponent = g3.output;
                statusLight = g3.output;
            }
            default -> { // Normal
                Grid g1 = buildGrille1(40, 40);
                Grid g2 = buildGrille2(520, 40, g1);
                grids.add(g1); grids.add(g2);
                winComponent = g2.output;
                statusLight = g2.output;
            }
        }
    }

    /* ────────────────── Construction des grilles (GR1, GR2, GR3) ────────────────── */

    /** Replique simplifié de GR1 : 4 interrupteurs, AND/NOT/XOR/AND combinés. */
    private Grid buildGrille1(int gx, int gy) {
        Grid g = new Grid(gx, gy, 280, 460, "Grille 1");
        Button b1 = g.add(new Button(g.col(1), g.row(0), "A"));
        Button b2 = g.add(new Button(g.col(3), g.row(0), "B"));
        Button b3 = g.add(new Button(g.col(0), g.row(1), "C"));
        Button b4 = g.add(new Button(g.col(2), g.row(1), "D"));

        Gate gAnd1 = g.add(new Gate("AND", g.col(2), g.row(2)));
        Gate gXor  = g.add(new Gate("XOR", g.col(0), g.row(3)));
        Gate gNot  = g.add(new Gate("NOT", g.col(3), g.row(3)));
        Gate gAndF = g.add(new Gate("AND", g.col(1), g.row(5)));

        gAnd1.connect(b1, b2);
        gXor.connect(b3, b4);
        gNot.connect(gAnd1);
        gAndF.connect(gXor, gNot);

        Light out = g.add(new Light(g.col(2), g.row(5), "S1"));
        out.connect(gAndF);
        g.output = out;
        return g;
    }

    /** GR2 : prend l'entrée GR1 et chaine NOT/AND. */
    private Grid buildGrille2(int gx, int gy, Grid input) {
        Grid g = new Grid(gx, gy, 260, 460, "Grille 2");
        Button b1 = g.add(new Button(g.col(0), g.row(0), "E"));
        Button b2 = g.add(new Button(g.col(2), g.row(0), "F"));
        Button b3 = g.add(new Button(g.col(3), g.row(1), "G"));
        g.upstream = input.output;
        b1.connect(input.output); b2.connect(input.output); b3.connect(input.output);

        Gate gAnd1 = g.add(new Gate("AND", g.col(1), g.row(1)));
        Gate gAndUp = g.add(new Gate("AND", g.col(2), g.row(2)));
        Gate gXor   = g.add(new Gate("XOR", g.col(0), g.row(3)));
        Gate gAndF  = g.add(new Gate("AND", g.col(2), g.row(4)));

        gAnd1.connect(b1, b2);
        gAndUp.connect(gAnd1, input.output);
        gXor.connect(b3, gAndUp);
        gAndF.connect(gAndUp, gXor);

        Light out = g.add(new Light(g.col(2), g.row(5), "S2"));
        out.connect(gAndF);
        g.output = out;
        return g;
    }

    /** GR3 : 3 interrupteurs supplémentaires + un dernier AND validateur. */
    private Grid buildGrille3(int gx, int gy, Grid input) {
        Grid g = new Grid(gx, gy, 260, 460, "Grille 3");
        Button b1 = g.add(new Button(g.col(0), g.row(0), "H"));
        Button b2 = g.add(new Button(g.col(2), g.row(0), "I"));
        Button b3 = g.add(new Button(g.col(3), g.row(1), "J"));
        g.upstream = input.output;
        b1.connect(input.output); b2.connect(input.output); b3.connect(input.output);

        Gate gXor   = g.add(new Gate("XOR", g.col(1), g.row(1)));
        Gate gNot   = g.add(new Gate("NOT", g.col(3), g.row(2)));
        Gate gAndUp = g.add(new Gate("AND", g.col(2), g.row(3)));
        Gate gAndF  = g.add(new Gate("AND", g.col(2), g.row(4)));

        gXor.connect(b1, b2);
        gNot.connect(b3);
        gAndUp.connect(input.output, gNot);
        gAndF.connect(gXor, gAndUp);

        Light out = g.add(new Light(g.col(2), g.row(5), "FINALE"));
        out.connect(gAndF);
        g.output = out;
        return g;
    }

    /* ────────────────── Boucle d'évaluation ────────────────── */

    private void evaluate() {
        // Évaluation en cascade (topologie déjà ordonnée par construction).
        for (Grid g : grids) g.evaluate();
        if (winComponent != null && winComponent.state) {
            setStatus("Lampe FINALE allumée : sortie ON, courant rétabli !",
                    new Color(46, 204, 113));
            if (!reussite) markSolvedAndClose();
        }
    }

    @Override
    protected void onMousePressed(Vec2 p) {
        if (reussite) return;
        if (btnReset.contains((int) p.x, (int) p.y)) {
            for (Grid g : grids) g.resetButtons();
            evaluate();
            return;
        }
        for (Grid g : grids) {
            for (Button b : g.buttons) {
                if (b.contains(p)) { b.pushed = !b.pushed; evaluate(); return; }
            }
        }
    }

    @Override
    protected boolean isCursorInteractive(Vec2 p) {
        if (btnReset.contains((int) p.x, (int) p.y)) return true;
        for (Grid g : grids) for (Button b : g.buttons) if (b.contains(p)) return true;
        return false;
    }

    @Override
    protected void paintEnigme(Graphics2D g, int w, int h) {
        // Cadres
        for (Grid gr : grids) gr.drawFrame(g);
        // Wires en premier (sous les composants)
        for (Grid gr : grids) gr.drawWires(g);
        // Composants
        for (Grid gr : grids) gr.drawComponents(g);

        // Voyants d'état par grille
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        for (int i = 0; i < grids.size(); i++) {
            Grid gr = grids.get(i);
            Color c = (gr.output != null && gr.output.state) ? new Color(46, 204, 113) : new Color(120, 50, 50);
            Draw.circle(g, 30, 30 + 24 * i, 9, c);
            g.setColor(Color.WHITE);
            g.drawString(gr.name, 50, 34 + 24 * i);
        }

        // Bouton reset
        g.setColor(new Color(192, 57, 43));
        g.fillRoundRect(btnReset.x, btnReset.y, btnReset.width, btnReset.height, 14, 14);
        g.setStroke(new BasicStroke(2));
        g.setColor(Color.WHITE);
        g.drawRoundRect(btnReset.x, btnReset.y, btnReset.width, btnReset.height, 14, 14);
        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        String t = "Tout remettre à OFF";
        int tw = g.getFontMetrics().stringWidth(t);
        g.drawString(t, btnReset.x + (btnReset.width - tw) / 2, btnReset.y + 24);

        // Statut final
        if (statusLight != null) {
            g.setFont(new Font("SansSerif", Font.BOLD, 13));
            g.setColor(statusLight.state ? new Color(46, 204, 113) : new Color(220, 220, 220));
            g.drawString("Lampe finale : " + (statusLight.state ? "ON" : "OFF"), 30, h - 30);
        }
    }

    /* ════════════════════════════════════════════════════════════════════════
     *                        Modèle interne (Component etc.)
     * ════════════════════════════════════════════════════════════════════════ */

    static abstract class Component {
        int x, y;
        boolean state;
        abstract void evaluate();
        boolean contains(Vec2 p) { return false; }
        abstract void draw(Graphics2D g);
        Component anchorTarget() { return this; }
        int anchorX() { return x; }
        int anchorY() { return y; }
    }

    static class Button extends Component {
        boolean pushed;
        final String label;
        Component upstream;
        Button(int x, int y, String label) { this.x = x; this.y = y; this.label = label; }
        public void connect(Component up) { this.upstream = up; }
        @Override void evaluate() {
            // Un bouton est ON s'il est pressé ET son amont est ON (ou s'il n'a pas d'amont)
            state = pushed && (upstream == null || upstream.state);
        }
        @Override boolean contains(Vec2 p) {
            return Math.hypot(p.x - x, p.y - y) < 18;
        }
        @Override void draw(Graphics2D g) {
            Color col = state ? new Color(219, 106, 0) : (pushed ? new Color(100, 60, 30) : new Color(110, 53, 0));
            Draw.circle(g, x, y, 22, new Color(40, 40, 40));
            Draw.circle(g, x, y, 19, new Color(170, 170, 170));
            Draw.circle(g, x, y, 15, col);
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 11));
            g.drawString(label, x - g.getFontMetrics().stringWidth(label) / 2, y - 26);
        }
    }

    static class Light extends Component {
        final String label;
        Component upstream;
        Light(int x, int y, String label) { this.x = x; this.y = y; this.label = label; }
        public void connect(Component up) { this.upstream = up; }
        @Override void evaluate() { state = (upstream != null) && upstream.state; }
        @Override void draw(Graphics2D g) {
            Color col = state ? new Color(219, 179, 0) : new Color(110, 98, 0);
            Draw.circle(g, x, y, 14, new Color(40, 40, 40));
            Draw.circle(g, x, y, 12, col);
            if (state) {
                g.setColor(new Color(255, 255, 200, 90));
                g.fillOval(x - 22, y - 22, 44, 44);
            }
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 10));
            g.drawString(label, x - g.getFontMetrics().stringWidth(label) / 2, y + 28);
        }
    }

    static class Gate extends Component {
        final String type; // "AND" "NOT" "XOR"
        final List<Component> inputs = new ArrayList<>();
        Gate(String type, int x, int y) { this.type = type; this.x = x; this.y = y; }
        public void connect(Component... ins) { inputs.addAll(Arrays.asList(ins)); }
        @Override void evaluate() {
            if (inputs.isEmpty()) { state = false; return; }
            switch (type) {
                case "AND" -> {
                    boolean a = true;
                    for (Component c : inputs) if (!c.state) { a = false; break; }
                    state = a;
                }
                case "NOT" -> state = !inputs.get(0).state;
                case "XOR" -> {
                    boolean a = false;
                    for (Component c : inputs) a ^= c.state;
                    state = a;
                }
                default -> state = false;
            }
        }
        @Override void draw(Graphics2D g) {
            Color bg = state ? colorByType(type) : new Color(60, 65, 75);
            g.setColor(bg);
            switch (type) {
                case "AND" -> {
                    g.fillRoundRect(x - 22, y - 14, 44, 28, 24, 24);
                    g.setColor(Color.WHITE); g.setStroke(new BasicStroke(2));
                    g.drawRoundRect(x - 22, y - 14, 44, 28, 24, 24);
                }
                case "NOT" -> {
                    int[] xs = { x - 18, x + 14, x - 18 };
                    int[] ys = { y - 14, y,      y + 14 };
                    g.fillPolygon(xs, ys, 3);
                    g.setColor(Color.WHITE); g.setStroke(new BasicStroke(2));
                    g.drawPolygon(xs, ys, 3);
                    Draw.circle(g, x + 18, y, 4, state ? colorByType(type) : new Color(60, 65, 75));
                    Draw.circle(g, x + 18, y, 4, Color.WHITE, 2);
                }
                case "XOR" -> {
                    int[] xs = { x - 18, x + 18, x - 18 };
                    int[] ys = { y - 16, y,      y + 16 };
                    g.fillPolygon(xs, ys, 3);
                    g.setColor(Color.WHITE); g.setStroke(new BasicStroke(2));
                    g.drawPolygon(xs, ys, 3);
                }
            }
            g.setFont(new Font("SansSerif", Font.BOLD, 9));
            g.setColor(Color.WHITE);
            int tw = g.getFontMetrics().stringWidth(type);
            g.drawString(type, x - tw / 2, y + 3);
        }
        private Color colorByType(String t) {
            return switch (t) {
                case "AND" -> new Color(0, 200, 0);
                case "NOT" -> new Color(231, 76, 60);
                case "XOR" -> new Color(52, 152, 219);
                default     -> Color.GRAY;
            };
        }
    }

    static class Grid {
        final int x, y, w, h;
        final String name;
        final List<Button> buttons = new ArrayList<>();
        final List<Component> all = new ArrayList<>();
        Component upstream;
        Light output;
        Grid(int x, int y, int w, int h, String name) { this.x = x; this.y = y; this.w = w; this.h = h; this.name = name; }
        int col(int c) { return x + 50 + c * ((w - 100) / 3); }
        int row(int r) { return y + 60 + r * ((h - 120) / 5); }
        <T extends Component> T add(T c) { all.add(c); if (c instanceof Button b) buttons.add(b); return c; }
        void resetButtons() { for (Button b : buttons) b.pushed = false; }

        void evaluate() {
            // upstream propagation déjà faite à la grille précédente
            // Ordre : buttons → gates → light de sortie (mais en générique : itérer 4x suffit pour fixer point fixe)
            for (int i = 0; i < 6; i++) for (Component c : all) c.evaluate();
        }

        void drawFrame(Graphics2D g) {
            g.setColor(new Color(39, 27, 13, 220));
            g.fillRoundRect(x, y, w, h, 18, 18);
            g.setColor(new Color(150, 130, 80));
            g.setStroke(new BasicStroke(2));
            g.drawRoundRect(x, y, w, h, 18, 18);
            g.setColor(new Color(220, 200, 160));
            g.setFont(new Font("SansSerif", Font.BOLD, 13));
            g.drawString(name, x + 14, y + 22);
            // Grille de points
            g.setColor(new Color(150, 130, 80, 120));
            for (int i = 0; i < w; i += 20) for (int j = 0; j < h; j += 20) g.fillOval(x + i, y + j, 2, 2);
        }
        void drawComponents(Graphics2D g) { for (Component c : all) c.draw(g); }

        void drawWires(Graphics2D g) {
            for (Component c : all) {
                if (c instanceof Button b && b.upstream != null) drawWire(g, b.upstream, b);
                if (c instanceof Light l && l.upstream != null)  drawWire(g, l.upstream, l);
                if (c instanceof Gate gt) for (Component in : gt.inputs) drawWire(g, in, gt);
            }
            // Wire montrant le lien entre grilles
            if (upstream != null) {
                g.setColor(upstream.state ? new Color(255, 255, 255) : new Color(60, 70, 90));
                g.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.drawLine(upstream.x + 16, upstream.y, x + 6, y + h / 2);
                g.drawLine(x + 6, y + h / 2, x, y + h / 2);
                g.setColor(new Color(220, 200, 160));
                g.setFont(new Font("Monospaced", Font.BOLD, 11));
                g.drawString("⇢ entrée", x + 10, y + h / 2 - 6);
            }
        }
        private void drawWire(Graphics2D g, Component a, Component b) {
            boolean active = a.state;
            g.setColor(active ? new Color(255, 255, 255) : new Color(50, 60, 75));
            g.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            // Câble en L (orthogonal)
            int x1 = a.x, y1 = a.y, x2 = b.x, y2 = b.y;
            int xm = (x1 + x2) / 2;
            g.drawLine(x1, y1, xm, y1);
            g.drawLine(xm, y1, xm, y2);
            g.drawLine(xm, y2, x2, y2);
            if (active) {
                // Petite "boule" qui circule (effet courant)
                long now = System.currentTimeMillis();
                double t = (now % 1200) / 1200.0;
                double[] pos = wirePoint(x1, y1, xm, y1, xm, y2, x2, y2, t);
                g.setColor(Color.WHITE);
                g.fillOval((int) pos[0] - 4, (int) pos[1] - 4, 8, 8);
            }
        }
        private double[] wirePoint(int x1,int y1,int x2,int y2,int x3,int y3,int x4,int y4,double t){
            double l1 = Math.hypot(x2-x1,y2-y1), l2 = Math.hypot(x3-x2,y3-y2), l3 = Math.hypot(x4-x3,y4-y3);
            double tot = l1 + l2 + l3 + 0.0001;
            double d = t * tot;
            if (d < l1) { double u = d / l1; return new double[]{ x1 + (x2-x1)*u, y1 + (y2-y1)*u }; }
            d -= l1;
            if (d < l2) { double u = d / l2; return new double[]{ x2 + (x3-x2)*u, y2 + (y3-y2)*u }; }
            d -= l2;
            double u = d / l3; return new double[]{ x3 + (x4-x3)*u, y3 + (y4-y3)*u };
        }
    }
}
