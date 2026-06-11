package com.sae.enigmas;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sae.core.Save;

/**
 * Phase 3.4 — "Portes Logiques" (1/2 Remettre le courant).
 * <p>
 * Port en Java de {@code SAE_Classe_Enigme_GATE_Remake.py}. La grille n°1
 * est une reproduction fidèle du prototype (mêmes positions, mêmes
 * branchements, mêmes lights "sondes") pour permettre au joueur de
 * comprendre les portes. Les grilles suivantes dépendent de la difficulté.
 *
 * <h2>Difficultés</h2>
 * <ul>
 *   <li><b>Easy</b>   : GR1 + 1 grille très simple presque à l'aveugle.
 *       Animations courant + couleurs d'état + noms des portes visibles.</li>
 *   <li><b>Normal</b> : GR1 + GR2 + GR3 (exact prototype Python). Une seule
 *       lampe de validation à la sortie de GR2 et GR3 ; pas d'animation /
 *       de couleurs d'état / de noms de portes.</li>
 *   <li><b>Hard</b>   : GR1 + 3 grilles totalement à l'aveugle. Seule la
 *       toute dernière lampe (objectif) est visible.</li>
 * </ul>
 */
public class LogicGateUI extends EnigmaDialog {

    private static final int W = 1300;
    private static final int H = 780;

    /** Échelle d'un "point" Python en pixels Java (PT). */
    private static final int PT = 8;
    /** Taille d'une "case" (9 PT) en pixels. */
    private static final int CASE_PX = 9 * PT;

    private enum Difficulty { EASY, NORMAL, HARD }
    private enum Sens { RIGHT, LEFT, UP, DOWN }

    private final Difficulty diff;
    private final boolean showAnimations;
    private final boolean showGateColors;
    private final boolean showGateNames;

    private final List<Grid> grids = new ArrayList<>();
    private Light winLight;
    private final Rectangle btnReset = new Rectangle(W - 200, H - 70, 170, 36);

    public LogicGateUI(Window parent, Save save) {
        super(parent, "Tableau électrique - Portes logiques", W, H);
        String d = save != null && save.getDifficulty() != null ? save.getDifficulty() : "Normal";
        this.diff = switch (d) {
            case "Easy"  -> Difficulty.EASY;
            case "Hard"  -> Difficulty.HARD;
            default       -> Difficulty.NORMAL;
        };
        this.showAnimations = (diff == Difficulty.EASY);
        this.showGateColors = (diff == Difficulty.EASY);
        this.showGateNames  = (diff == Difficulty.EASY);

        construire();

        setStatus("Basculez les interrupteurs pour ALLUMER la lampe finale.",
                  new Color(41, 128, 185));
    }

    /* ════════════════════════════════════════════════════════════════════════
     *                          Construction des grilles
     * ════════════════════════════════════════════════════════════════════════ */

    private void construire() {
        // Position de GR1 (toujours) — coin haut-gauche
        Grid g1 = buildGR1(40, 50);
        grids.add(g1);

        switch (diff) {
            case EASY -> {
                Grid g2 = buildEasyG2(40 + 41 * PT + 30, 50, g1);
                grids.add(g2);
                winLight = g2.output;
            }
            case NORMAL -> {
                Grid g2 = buildGR2(40 + 41 * PT + 30, 50, g1);
                grids.add(g2);
                // GR3 sous GR1 (plus large), on la place sur une nouvelle ligne
                int row2Y = 50 + 28 * PT + 30;
                Grid g3 = buildGR3(40, row2Y, g2);
                grids.add(g3);
                winLight = g3.output;
            }
            case HARD -> {
                Grid g2 = buildHardG2(40 + 41 * PT + 30, 50, g1);
                grids.add(g2);
                int row2Y = 50 + 28 * PT + 30;
                Grid g3 = buildHardG3(40, row2Y, g2);
                grids.add(g3);
                Grid g4 = buildHardG4(40 + 56 * PT + 30, row2Y, g3);
                grids.add(g4);
                winLight = g4.output;
            }
        }
    }

    /** GR1 : reproduction fidèle du prototype Python (4 boutons, 5 portes, 9 lampes sondes). */
    private Grid buildGR1(int gx, int gy) {
        // Grille de 40 x 27 points Python
        Grid g = new Grid(gx, gy, 40, 27, "Grille 1", true);

        // Boutons d'entrée
        Button B1_1 = (Button) g.place(new Button("A", Sens.RIGHT), 0, 0,  4, 4);
        Button B1_2 = (Button) g.place(new Button("B", Sens.RIGHT), 0, 1,  7, 1);
        Button B1_3 = (Button) g.place(new Button("C", Sens.RIGHT), 0, 1,  4, 7);
        Button B1_4 = (Button) g.place(new Button("D", Sens.RIGHT), 0, 2,  7, 4);

        // Portes
        Gate G1_1 = (Gate) g.place(new Gate("AND", Sens.RIGHT), 2, 0,  1, 5);
        Gate G1_2 = (Gate) g.place(new Gate("NOT", Sens.RIGHT), 2, 1,  1, 5);
        Gate G1_3 = (Gate) g.place(new Gate("XOR", Sens.RIGHT), 1, 2,  8, 4);
        Gate G1_4 = (Gate) g.place(new Gate("AND", Sens.RIGHT), 3, 0,  4, 8);
        Gate G1_5 = (Gate) g.place(new Gate("AND", Sens.DOWN ), 3, 2,  8, 1);

        // Sondes (lights internes — visibles dans toutes les difficultés sur GR1)
        Light L1_1 = (Light) g.place(new Light("L1", Sens.RIGHT, true), 1, 0,  2, 4);
        Light L1_2 = (Light) g.place(new Light("L2", Sens.RIGHT, true), 1, 0,  5, 9);
        Light L1_3 = (Light) g.place(new Light("L3", Sens.RIGHT, true), 1, 1,  3, 5);
        Light L1_4 = (Light) g.place(new Light("L4", Sens.RIGHT, true), 1, 2,  3, 1);
        Light L1_5 = (Light) g.place(new Light("L5", Sens.RIGHT, true), 1, 2,  3, 8);
        Light L1_6 = (Light) g.place(new Light("L6", Sens.RIGHT, true), 2, 0,  8, 6);
        Light L1_7 = (Light) g.place(new Light("L7", Sens.RIGHT, true), 2, 1,  7, 5);
        Light L1_8 = (Light) g.place(new Light("L8", Sens.RIGHT, true), 2, 2,  7, 4);
        Light L1_9 = (Light) g.place(new Light("OUT", Sens.DOWN, true),  3, 2,  8, 7);

        // Connexions (identiques au Python)
        L1_1.connect(B1_1);
        L1_2.connect(B1_2);
        L1_3.connect(B1_3);
        L1_4.connect(B1_3);
        L1_5.connect(B1_4);
        G1_1.connect(L1_2, L1_1);
        G1_2.connect(L1_3);
        G1_3.connect(L1_5, L1_4);
        L1_6.connect(G1_1);
        L1_7.connect(G1_2);
        L1_8.connect(G1_3);
        G1_4.connect(L1_7, L1_6);
        G1_5.connect(L1_8, G1_4);
        L1_9.connect(G1_5);

        g.output = L1_9;
        g.buildWires();
        return g;
    }

    /** GR2 du prototype Python (5 boutons, 10 portes, à l'aveugle, lampe de sortie). */
    private Grid buildGR2(int gx, int gy, Grid input) {
        Grid g = new Grid(gx, gy, 54, 27, "Grille 2", false);
        g.upstream = input;

        Button B2_1 = (Button) g.place(new Button("E", Sens.RIGHT), 1, 0, 2, 6);
        Button B2_2 = (Button) g.place(new Button("F", Sens.RIGHT), 2, 0, 1, 5);
        Button B2_3 = (Button) g.place(new Button("G", Sens.RIGHT), 2, 0, 9, 6);
        Button B2_4 = (Button) g.place(new Button("H", Sens.RIGHT), 3, 0, 7, 4);
        Button B2_5 = (Button) g.place(new Button("I", Sens.RIGHT), 4, 0, 5, 6);

        Gate G2_1  = (Gate) g.place(new Gate("AND", Sens.LEFT), 1, 1, 2, 5);
        Gate G2_2  = (Gate) g.place(new Gate("NOT", Sens.UP),   2, 1, 5, 7);
        Gate G2_3  = (Gate) g.place(new Gate("NOT", Sens.RIGHT),3, 1, 4, 8);
        Gate G2_4  = (Gate) g.place(new Gate("AND", Sens.LEFT), 3, 1, 8, 4);
        Gate G2_5  = (Gate) g.place(new Gate("NOT", Sens.DOWN), 4, 1, 9, 4);
        Gate G2_6  = (Gate) g.place(new Gate("NOT", Sens.DOWN), 0, 2, 9, 4);
        Gate G2_7  = (Gate) g.place(new Gate("AND", Sens.DOWN), 1, 2, 6, 3);
        Gate G2_8  = (Gate) g.place(new Gate("AND", Sens.RIGHT),2, 2, 4, 5);
        Gate G2_9  = (Gate) g.place(new Gate("AND", Sens.RIGHT),3, 2, 7, 6);
        Gate G2_10 = (Gate) g.place(new Gate("AND", Sens.RIGHT),5, 2, 2, 5);

        // Lampe finale (validation)
        Light L2_out = (Light) g.place(new Light("S2", Sens.RIGHT, true), 5, 2, 7, 5);

        G2_1.connect(B2_1, B2_1);
        G2_3.connect(B2_3);
        G2_4.connect(B2_4, G2_3);
        G2_5.connect(B2_5);
        G2_6.connect(G2_5);
        G2_8.connect(B2_1, B2_2);
        G2_2.connect(G2_8);
        G2_7.connect(G2_1, G2_2);
        G2_9.connect(G2_4, G2_6);
        G2_10.connect(G2_7, G2_9);
        L2_out.connect(G2_10);

        g.output = L2_out;
        g.buildWires();
        return g;
    }

    /** GR3 du prototype Python (10 boutons, 21 portes, 1 lampe finale). Très large. */
    private Grid buildGR3(int gx, int gy, Grid input) {
        Grid g = new Grid(gx, gy, 99, 27, "Grille 3", false);
        g.upstream = input;

        Button B3_1  = (Button) g.place(new Button("J", Sens.RIGHT), 1, 0, 2, 4);
        Button B3_2  = (Button) g.place(new Button("K", Sens.RIGHT), 0, 1, 7, 1);
        Button B3_3  = (Button) g.place(new Button("L", Sens.RIGHT), 1, 1, 1, 8);
        Button B3_4  = (Button) g.place(new Button("M", Sens.RIGHT), 0, 2, 7, 5);
        Button B3_5  = (Button) g.place(new Button("N", Sens.RIGHT), 2, 0, 2, 4);
        Button B3_6  = (Button) g.place(new Button("O", Sens.RIGHT), 4, 0, 9, 4);
        Button B3_7  = (Button) g.place(new Button("P", Sens.RIGHT), 5, 0, 7, 6);
        Button B3_8  = (Button) g.place(new Button("Q", Sens.RIGHT), 6, 0, 4, 4);
        Button B3_9  = (Button) g.place(new Button("R", Sens.RIGHT), 7, 0, 2, 6);
        Button B3_10 = (Button) g.place(new Button("S", Sens.RIGHT), 7, 0, 9, 4);

        Gate G3_1  = (Gate) g.place(new Gate("AND", Sens.DOWN),  3, 0, 2, 6);
        Gate G3_2  = (Gate) g.place(new Gate("NOT", Sens.RIGHT), 1, 1, 7, 4);
        Gate G3_3  = (Gate) g.place(new Gate("AND", Sens.RIGHT), 2, 1, 6, 6);
        Gate G3_4  = (Gate) g.place(new Gate("XOR", Sens.DOWN),  3, 1, 5, 4);
        Gate G3_5  = (Gate) g.place(new Gate("AND", Sens.DOWN),  3, 1, 3, 9);
        Gate G3_6  = (Gate) g.place(new Gate("NOT", Sens.RIGHT), 1, 2, 7, 6);
        Gate G3_7  = (Gate) g.place(new Gate("NOT", Sens.DOWN),  2, 2, 3, 3);
        Gate G3_8  = (Gate) g.place(new Gate("AND", Sens.RIGHT), 2, 2, 8, 6);
        Gate G3_9  = (Gate) g.place(new Gate("AND", Sens.RIGHT), 3, 2, 9, 7);
        Gate G3_10 = (Gate) g.place(new Gate("NOT", Sens.DOWN),  5, 1, 8, 3);
        Gate G3_11 = (Gate) g.place(new Gate("XOR", Sens.DOWN),  5, 1, 4, 8);
        Gate G3_12 = (Gate) g.place(new Gate("NOT", Sens.RIGHT), 5, 2, 7, 3);
        Gate G3_13 = (Gate) g.place(new Gate("XOR", Sens.DOWN),  6, 1, 3, 8);
        Gate G3_14 = (Gate) g.place(new Gate("AND", Sens.RIGHT), 6, 2, 9, 1);
        Gate G3_15 = (Gate) g.place(new Gate("NOT", Sens.RIGHT), 7, 1, 5, 5);
        Gate G3_16 = (Gate) g.place(new Gate("AND", Sens.RIGHT), 8, 1, 2, 4);
        Gate G3_17 = (Gate) g.place(new Gate("AND", Sens.UP),    8, 1, 7, 1);
        Gate G3_18 = (Gate) g.place(new Gate("AND", Sens.RIGHT), 9, 0, 5, 6);
        Gate G3_19 = (Gate) g.place(new Gate("AND", Sens.DOWN), 10, 0, 2, 9);
        Gate G3_20 = (Gate) g.place(new Gate("AND", Sens.RIGHT),10, 2, 3, 5);

        Light L3_1 = (Light) g.place(new Light("FINALE", Sens.RIGHT, true), 10, 2, 8, 8);

        G3_1.connect(B3_5, B3_6);
        G3_2.connect(B3_1);
        G3_3.connect(B3_2, G3_2);
        G3_4.connect(B3_1, G3_1);
        G3_5.connect(G3_3, G3_4);
        G3_6.connect(B3_4);
        G3_7.connect(B3_3);
        G3_8.connect(G3_6, G3_7);
        G3_9.connect(G3_8, G3_5);
        G3_10.connect(B3_7);
        G3_11.connect(B3_6, G3_10);
        G3_12.connect(G3_11);
        G3_13.connect(B3_7, B3_8);
        G3_14.connect(G3_13, B3_8);
        G3_15.connect(B3_9);
        G3_16.connect(G3_15, B3_10);
        G3_17.connect(G3_14, G3_16);
        G3_18.connect(G3_12, G3_17);
        G3_19.connect(G3_18, null); // null = upstream (résolu plus bas)
        G3_20.connect(G3_9, G3_19);
        L3_1.connect(G3_20);

        g.output = L3_1;
        g.buildWires();
        return g;
    }

    /** Easy : une 2e grille très simple, presque à l'aveugle (2 boutons + 1 porte AND). */
    private Grid buildEasyG2(int gx, int gy, Grid input) {
        Grid g = new Grid(gx, gy, 30, 27, "Grille 2", false);
        g.upstream = input;

        Button E1 = (Button) g.place(new Button("E", Sens.RIGHT), 0, 0, 3, 5);
        Button E2 = (Button) g.place(new Button("F", Sens.RIGHT), 0, 2, 3, 5);

        // Une AND entre l'amont (signal GR1) et un bouton, puis AND finale avec l'autre bouton
        Gate gA = (Gate) g.place(new Gate("AND", Sens.RIGHT), 1, 1, 5, 5);
        Gate gB = (Gate) g.place(new Gate("AND", Sens.RIGHT), 2, 1, 5, 5);

        Light Lout = (Light) g.place(new Light("S2", Sens.RIGHT, true), 3, 1, 1, 5);

        // E1 et E2 dépendent du signal amont (comme dans le proto)
        gA.connect(E1, null); // null = upstream
        gB.connect(gA, E2);
        Lout.connect(gB);

        g.output = Lout;
        g.buildWires();
        return g;
    }

    /** Hard G2 : grille à l'aveugle, pas de lampe intermédiaire. */
    private Grid buildHardG2(int gx, int gy, Grid input) {
        Grid g = new Grid(gx, gy, 41, 27, "Grille 2", false);
        g.upstream = input;

        Button b1 = (Button) g.place(new Button("E", Sens.RIGHT), 0, 0, 3, 4);
        Button b2 = (Button) g.place(new Button("F", Sens.RIGHT), 1, 0, 1, 6);
        Button b3 = (Button) g.place(new Button("G", Sens.RIGHT), 2, 0, 6, 4);
        Button b4 = (Button) g.place(new Button("H", Sens.RIGHT), 3, 0, 4, 6);

        Gate g1 = (Gate) g.place(new Gate("XOR", Sens.RIGHT), 0, 1, 7, 4);
        Gate g2 = (Gate) g.place(new Gate("NOT", Sens.RIGHT), 1, 1, 7, 4);
        Gate g3 = (Gate) g.place(new Gate("AND", Sens.RIGHT), 2, 1, 7, 4);
        Gate g4 = (Gate) g.place(new Gate("AND", Sens.RIGHT), 3, 1, 7, 4);
        Gate g5 = (Gate) g.place(new Gate("XOR", Sens.RIGHT), 3, 2, 7, 4);

        // Pas de Light intermédiaire (fully blind) — la sortie est invisible
        Light hiddenOut = (Light) g.place(new Light("·", Sens.RIGHT, false), 4, 1, 1, 4);

        g1.connect(b1, b2);
        g2.connect(b3);
        g3.connect(g2, b4);
        g4.connect(g1, g3);
        g5.connect(g4, null); // upstream
        hiddenOut.connect(g5);

        g.output = hiddenOut;
        g.buildWires();
        return g;
    }

    private Grid buildHardG3(int gx, int gy, Grid input) {
        Grid g = new Grid(gx, gy, 56, 27, "Grille 3", false);
        g.upstream = input;

        Button b1 = (Button) g.place(new Button("I", Sens.RIGHT), 0, 0, 3, 4);
        Button b2 = (Button) g.place(new Button("J", Sens.RIGHT), 1, 0, 1, 6);
        Button b3 = (Button) g.place(new Button("K", Sens.RIGHT), 2, 0, 6, 4);
        Button b4 = (Button) g.place(new Button("L", Sens.RIGHT), 3, 0, 3, 4);
        Button b5 = (Button) g.place(new Button("M", Sens.RIGHT), 4, 0, 5, 6);

        Gate g1 = (Gate) g.place(new Gate("AND", Sens.RIGHT), 0, 1, 7, 4);
        Gate g2 = (Gate) g.place(new Gate("XOR", Sens.RIGHT), 1, 1, 7, 4);
        Gate g3 = (Gate) g.place(new Gate("NOT", Sens.RIGHT), 2, 1, 7, 4);
        Gate g4 = (Gate) g.place(new Gate("AND", Sens.RIGHT), 3, 1, 7, 4);
        Gate g5 = (Gate) g.place(new Gate("XOR", Sens.RIGHT), 4, 1, 7, 4);
        Gate g6 = (Gate) g.place(new Gate("AND", Sens.RIGHT), 5, 1, 3, 4);

        Light hiddenOut = (Light) g.place(new Light("·", Sens.RIGHT, false), 5, 1, 7, 4);

        g1.connect(b1, b2);
        g2.connect(b3, b4);
        g3.connect(b5);
        g4.connect(g1, g2);
        g5.connect(g3, null); // upstream
        g6.connect(g4, g5);
        hiddenOut.connect(g6);

        g.output = hiddenOut;
        g.buildWires();
        return g;
    }

    private Grid buildHardG4(int gx, int gy, Grid input) {
        Grid g = new Grid(gx, gy, 41, 27, "Grille 4", false);
        g.upstream = input;

        Button b1 = (Button) g.place(new Button("T", Sens.RIGHT), 0, 0, 3, 4);
        Button b2 = (Button) g.place(new Button("U", Sens.RIGHT), 1, 0, 1, 6);
        Button b3 = (Button) g.place(new Button("V", Sens.RIGHT), 2, 0, 6, 4);

        Gate g1 = (Gate) g.place(new Gate("AND", Sens.RIGHT), 0, 1, 7, 4);
        Gate g2 = (Gate) g.place(new Gate("XOR", Sens.RIGHT), 1, 1, 7, 4);
        Gate g3 = (Gate) g.place(new Gate("AND", Sens.RIGHT), 2, 1, 7, 4);
        Gate g4 = (Gate) g.place(new Gate("AND", Sens.RIGHT), 3, 1, 7, 4);

        // Lampe finale visible (lampe de victoire)
        Light finalLight = (Light) g.place(new Light("FINALE", Sens.RIGHT, true), 4, 1, 1, 4);

        g1.connect(b1, b2);
        g2.connect(b3, null); // upstream
        g3.connect(g1, g2);
        g4.connect(g3, null); // upstream
        finalLight.connect(g4);

        g.output = finalLight;
        g.buildWires();
        return g;
    }

    /* ════════════════════════════════════════════════════════════════════════
     *                          Boucle / Évaluation
     * ════════════════════════════════════════════════════════════════════════ */

    private void evaluate() {
        for (Grid gr : grids) gr.evaluate();
        if (winLight != null && winLight.state) {
            setStatus("Lampe FINALE allumée : courant rétabli !",
                      new Color(46, 204, 113));
            if (!reussite) markSolvedAndClose();
        }
    }

    @Override
    protected void onMousePressed(Vec2 p) {
        if (reussite) return;
        if (btnReset.contains((int) p.x, (int) p.y)) {
            for (Grid gr : grids) gr.resetButtons();
            evaluate();
            return;
        }
        for (Grid gr : grids) {
            for (Button b : gr.buttons) {
                if (b.contains(p)) { b.pushed = !b.pushed; evaluate(); return; }
            }
        }
    }

    @Override
    protected boolean isCursorInteractive(Vec2 p) {
        if (btnReset.contains((int) p.x, (int) p.y)) return true;
        for (Grid gr : grids) for (Button b : gr.buttons) if (b.contains(p)) return true;
        return false;
    }

    @Override
    protected void paintEnigme(Graphics2D g, int w, int h) {
        // Fond
        for (Grid gr : grids) gr.drawBackground(g);
        // Câbles "bus" entre grilles (gros câble inter-grilles)
        for (Grid gr : grids) gr.drawBusCable(g, showAnimations);
        // Wires internes (sous les composants)
        for (Grid gr : grids) gr.drawWires(g, showAnimations);
        // Composants
        for (Grid gr : grids) gr.drawComponents(g, showGateColors, showGateNames);

        // Voyants d'état des grilles (en haut à gauche) en mode Easy
        if (this.diff.equals(Difficulty.EASY)){
            for (int i = 0; i < grids.size(); i++) {
                Grid gr = grids.get(i);
                Color c = (gr.output != null && gr.output.state)
                        ? new Color(46, 204, 113) : new Color(120, 50, 50);
                Draw.circle(g, 18, 16 + 16 * i, 6, c);
            }
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
        if (winLight != null) {
            g.setFont(new Font("SansSerif", Font.BOLD, 13));
            g.setColor(winLight.state ? new Color(46, 204, 113) : new Color(220, 220, 220));
            g.drawString("Lampe finale : " + (winLight.state ? "ON" : "OFF"),
                         30, h - 50);
        }
    }

    /* ════════════════════════════════════════════════════════════════════════
     *                          Modèle composants
     * ════════════════════════════════════════════════════════════════════════ */

    static abstract class Component {
        int x, y;          // centre en pixels
        Sens sens = Sens.RIGHT;
        boolean state;

        // Ancrages : in_anchors[i] = {préAncre, ancre} ; out_anchor = {ancre, sortie}
        // Stockés sous forme de points (xi, yi).
        int[][] inA  = new int[0][2];  // ancre principale (l'extrémité côté entrée du composant)
        int[][] inAp = new int[0][2];  // pré-ancre (un peu en retrait, pour le routing)
        int   outX, outY;              // ancre principale (sortie)
        int   outXp, outYp;            // pré-ancre (un peu plus loin)

        abstract void evaluate();
        boolean contains(Vec2 p) { return false; }
        abstract void draw(Graphics2D g, boolean colored, boolean showName);
        abstract void computeAnchors();
    }

    static class Button extends Component {
        boolean pushed;
        final String label;
        Component upstream;       // null = pas d'amont (boutons "libres")
        boolean linkedToGrid;     // si true => l'amont est le bus de la grille

        Button(String label, Sens sens) { this.label = label; this.sens = sens; }
        void connect(Component up) { this.upstream = up; }

        @Override void evaluate() {
            boolean up = (upstream == null) || upstream.state;
            state = pushed && up;
        }
        @Override boolean contains(Vec2 p) { return Math.hypot(p.x - x, p.y - y) < 18; }

        @Override void computeAnchors() {
            int r = 18;
            outX = x; outY = y;
            int[] o = rotate(x - r, y, x, y, sensAngle(sens));
            outX = o[0]; outY = o[1];
            int[] op = rotate(x - 2 * r, y, x, y, sensAngle(sens));
            outXp = op[0]; outYp = op[1];
            // pas d'entrées physiques (les boutons n'ont pas d'inputs visibles)
            inA = new int[0][2];
            inAp = new int[0][2];
        }

        @Override void draw(Graphics2D g, boolean colored, boolean showName) {
            Color base = new Color(170, 170, 170);
            Color ringDark = new Color(40, 40, 40);
            Draw.circle(g, x, y, 22, ringDark);
            Draw.circle(g, x, y, 19, base);
            Color col;
            if (state) col = new Color(219, 106, 0);
            else if (pushed) col = new Color(80, 50, 25);
            else col = new Color(110, 53, 0);
            Draw.circle(g, x, y, 14, col);
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 11));
            int tw = g.getFontMetrics().stringWidth(label);
            g.drawString(label, x - tw / 2, y - 26);
        }
    }

    /**
     * Light = lampe / sonde. Peut être visible (lumière) ou invisible (juste un
     * point de propagation pour les grilles à l'aveugle).
     */
    static class Light extends Component {
        final String label;
        final boolean visible;
        Component upstream;
        Light(String label, Sens sens, boolean visible) {
            this.label = label; this.sens = sens; this.visible = visible;
        }
        void connect(Component up) { this.upstream = up; }

        @Override void evaluate() { state = (upstream != null) && upstream.state; }

        @Override void computeAnchors() {
            int r = 12;
            // entrée principale : côté opposé au sens
            int[] in0 = rotate(x - 2 * r, y, x, y, sensAngle(sens));
            int[] in1 = new int[]{ x, y };
            inA  = new int[][]{ in1 };       // ancre = centre
            inAp = new int[][]{ in0 };       // pré-ancre = en arrière
            outX = x; outY = y;
            outXp = x; outYp = y;
        }

        @Override void draw(Graphics2D g, boolean colored, boolean showName) {
            if (!visible) return;
            Color col = state ? new Color(219, 179, 0) : new Color(110, 98, 0);
            Draw.circle(g, x, y, 13, new Color(40, 40, 40));
            Draw.circle(g, x, y, 11, col);
            if (state) {
                g.setColor(new Color(255, 255, 200, 80));
                g.fillOval(x - 20, y - 20, 40, 40);
            }
            g.setColor(new Color(220, 220, 220));
            g.setFont(new Font("SansSerif", Font.BOLD, 9));
            int tw = g.getFontMetrics().stringWidth(label);
            g.drawString(label, x - tw / 2, y + 26);
        }
    }

    static class Gate extends Component {
        final String type; // AND, NOT, XOR
        final List<Component> inputs = new ArrayList<>();
        Grid parentGrid; // pour résoudre les inputs null = bus amont
        Gate(String type, Sens sens) { this.type = type; this.sens = sens; }
        void connect(Component... ins) { inputs.addAll(Arrays.asList(ins)); }

        private boolean inputState(Component c) {
            if (c != null) return c.state;
            // null = bus amont de la grille
            return parentGrid != null && parentGrid.busState();
        }

        @Override void evaluate() {
            if (inputs.isEmpty()) { state = false; return; }
            switch (type) {
                case "AND" -> {
                    boolean a = true;
                    for (Component c : inputs) if (!inputState(c)) { a = false; break; }
                    state = a;
                }
                case "NOT" -> state = !inputState(inputs.get(0));
                case "XOR" -> {
                    boolean a = false;
                    for (Component c : inputs) a ^= inputState(c);
                    state = a;
                }
                default -> state = false;
            }
        }

        @Override void computeAnchors() {
            int size = 16;   // demi-taille
            if (type.equals("NOT")) {
                // Une entrée, une sortie
                int[] in = rotate(x - size, y, x, y, sensAngle(sens));
                int[] inP = rotate(x - 2 * size, y, x, y, sensAngle(sens));
                inA  = new int[][]{ in };
                inAp = new int[][]{ inP };
                int[] out = rotate(x + size + 6, y, x, y, sensAngle(sens));
                int[] outP = rotate(x + 2 * size + 6, y, x, y, sensAngle(sens));
                outX = out[0]; outY = out[1];
                outXp = outP[0]; outYp = outP[1];
            } else {
                // AND/XOR : deux entrées, une sortie. Anchors décalées en haut et en bas
                int dy = 7; // espacement
                int[] in1 = rotate(x - size, y - dy, x, y, sensAngle(sens));
                int[] in1P = rotate(x - 2 * size, y - dy, x, y, sensAngle(sens));
                int[] in2 = rotate(x - size, y + dy, x, y, sensAngle(sens));
                int[] in2P = rotate(x - 2 * size, y + dy, x, y, sensAngle(sens));
                inA  = new int[][]{ in1, in2 };
                inAp = new int[][]{ in1P, in2P };
                int[] out = rotate(x + size, y, x, y, sensAngle(sens));
                int[] outP = rotate(x + 2 * size, y, x, y, sensAngle(sens));
                outX = out[0]; outY = out[1];
                outXp = outP[0]; outYp = outP[1];
            }
        }

        @Override void draw(Graphics2D g, boolean colored, boolean showName) {
            Color stateCol = colored ? colorByType(type) : new Color(80, 90, 100);
            Color bodyCol  = state && colored ? stateCol : new Color(60, 65, 75);

            int t = 16;
            int angle = sensAngle(sens);
            switch (type) {
                case "NOT" -> drawNot(g, bodyCol, t, angle);
                case "AND" -> drawAnd(g, bodyCol, t, angle);
                case "XOR" -> drawXor(g, bodyCol, t, angle);
            }
            if (showName) {
                g.setFont(new Font("SansSerif", Font.BOLD, 9));
                g.setColor(Color.WHITE);
                int tw = g.getFontMetrics().stringWidth(type);
                g.drawString(type, x - tw / 2, y + 3);
            }
        }

        private void drawNot(Graphics2D g, Color col, int t, int angle) {
            // Triangle dont la pointe est dans la direction du sens.
            int[] p1 = rotate(x - t, y - t, x, y, angle);
            int[] p2 = rotate(x - t, y + t, x, y, angle);
            int[] p3 = rotate(x + t, y,     x, y, angle);
            Polygon p = new Polygon(
                new int[]{p1[0], p2[0], p3[0]},
                new int[]{p1[1], p2[1], p3[1]}, 3);
            g.setColor(col); g.fillPolygon(p);
            g.setStroke(new BasicStroke(2)); g.setColor(Color.WHITE); g.drawPolygon(p);
            // Petit cercle d'inversion à la pointe
            int[] dot = rotate(x + t + 4, y, x, y, angle);
            Draw.circle(g, dot[0], dot[1], 4, col);
            Draw.circle(g, dot[0], dot[1], 4, Color.WHITE, 2);
        }

        private void drawAnd(Graphics2D g, Color col, int t, int angle) {
            // Forme "D" : un rectangle + un demi-cercle, orienté.
            // On dessine d'abord en RIGHT puis on rote via une rotation graphique.
            java.awt.geom.AffineTransform old = g.getTransform();
            g.translate(x, y);
            g.rotate(Math.toRadians(-angle));
            Area a = new Area(new java.awt.Rectangle(-t, -t, t, 2 * t));
            a.add(new Area(new Ellipse2D.Double(-t/2.0, -t, 2 * t, 2 * t)));
            g.setColor(col); g.fill(a);
            g.setStroke(new BasicStroke(2)); g.setColor(Color.WHITE); g.draw(a);
            g.setTransform(old);
        }

        private void drawXor(Graphics2D g, Color col, int t, int angle) {
            // Triangle pointu (XOR) avec une petite encoche arrière (juste un trait courbé).
            java.awt.geom.AffineTransform old = g.getTransform();
            g.translate(x, y);
            g.rotate(Math.toRadians(-angle));
            int[] xs = { -t, -t, t + 2 };
            int[] ys = { -t,  t, 0 };
            g.setColor(col); g.fillPolygon(xs, ys, 3);
            g.setStroke(new BasicStroke(2)); g.setColor(Color.WHITE); g.drawPolygon(xs, ys, 3);
            // Petit trait arrière
            g.drawArc(-t - 6, -t, 6, 2 * t, -90, 180);
            g.setTransform(old);
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

    /* ════════════════════════════════════════════════════════════════════════
     *                                Grid
     * ════════════════════════════════════════════════════════════════════════ */

    static class Grid {
        final int dx, dy;     // origine pixels
        final int tx, ty;     // taille logique (en PT Python)
        final String name;
        final boolean hasIntermediateProbes; // true pour GR1 (lights internes)

        final List<Component> all = new ArrayList<>();
        final List<Button> buttons = new ArrayList<>();
        final List<Wire> wires = new ArrayList<>();

        Grid upstream;        // grille amont
        Light output;         // lampe finale de la grille

        Grid(int dx, int dy, int tx, int ty, String name, boolean hasIntermediateProbes) {
            this.dx = dx; this.dy = dy;
            this.tx = tx; this.ty = ty;
            this.name = name;
            this.hasIntermediateProbes = hasIntermediateProbes;
        }

        /** Place un composant (coord Python : caseX*9 + localX, caseY*9 + localY). */
        Component place(Component c, int caseX, int caseY, int localX, int localY) {
            int px = dx + ((caseX * 9 + localX - 1) * PT);
            int py = dy + ((caseY * 9 + localY - 1) * PT);
            c.x = px; c.y = py;
            c.computeAnchors();
            all.add(c);
            if (c instanceof Button b) buttons.add(b);
            if (c instanceof Gate g) g.parentGrid = this;
            return c;
        }

        void resetButtons() { for (Button b : buttons) b.pushed = false; }

        /** Construit la liste des wires basée sur les connexions logiques. */
        void buildWires() {
            // Représente l'upstream comme une "source virtuelle" placée à gauche
            // de la grille. On utilise un Component fictif pour la position du bus.
            for (Component c : all) {
                if (c instanceof Gate gt) {
                    for (int i = 0; i < gt.inputs.size(); i++) {
                        Component in = gt.inputs.get(i);
                        if (in == null) {
                            // upstream = bus de grille
                            wires.add(Wire.busInput(this, gt, i));
                        } else {
                            wires.add(new Wire(in, gt, i));
                        }
                    }
                } else if (c instanceof Light l) {
                    if (l.upstream != null) wires.add(new Wire(l.upstream, l, 0));
                } else if (c instanceof Button b) {
                    if (b.upstream != null) wires.add(new Wire(b.upstream, b, 0));
                }
            }
        }

        /** Évalue tous les composants (point fixe sur quelques itérations). */
        void evaluate() {
            // Les boutons reçoivent le signal amont (bus) si la grille a un upstream
            for (Button b : buttons) {
                if (b.upstream == null && upstream != null) {
                    // bouton sans amont explicite : ses pushes ne sont actifs que si le bus est ON
                    b.linkedToGrid = true;
                }
            }
            for (int i = 0; i < 8; i++) {
                for (Component c : all) {
                    if (c instanceof Button b && b.linkedToGrid && upstream != null) {
                        b.state = b.pushed && upstream.output != null && upstream.output.state;
                    } else {
                        c.evaluate();
                    }
                }
            }
        }

        /** Renvoie l'état effectif du bus de cette grille (= sortie de l'amont). */
        boolean busState() {
            return upstream != null && upstream.output != null && upstream.output.state;
        }

        /* ─────────────── Dessin ─────────────── */

        void drawBackground(Graphics2D g) {
            int w = tx * PT, h = ty * PT;
            // fond
            Color clair  = Draw.transition("#271B0D", "#FFFFFF", 22);
            Color sombre = Draw.transition(clair,    new Color(0, 0, 0), 20);
            g.setColor(clair);
            g.fillRoundRect(dx, dy, w, h, 14, 14);
            g.setColor(new Color(150, 130, 80));
            g.setStroke(new BasicStroke(2));
            g.drawRoundRect(dx, dy, w, h, 14, 14);
            // Quadrillage : lignes/colonnes par PT_CASE=9
            g.setColor(sombre);
            g.setStroke(new BasicStroke(1));
            for (int l = 0; l <= ty / 9; l++) g.drawLine(dx, dy + l * 9 * PT, dx + w, dy + l * 9 * PT);
            for (int c = 0; c <= tx / 9; c++) g.drawLine(dx + c * 9 * PT, dy, dx + c * 9 * PT, dy + h);
            // petits points
            g.setColor(new Color(150, 130, 80, 120));
            for (int ix = 0; ix < tx; ix++)
                for (int iy = 0; iy < ty; iy++)
                    g.fillOval(dx + ix * PT + PT/2 - 1, dy + iy * PT + PT/2 - 1, 2, 2);
        }

        /** Câble "bus" épais qui entre dans la grille (depuis la grille amont). */
        void drawBusCable(Graphics2D g, boolean withAnim) {
            if (upstream == null || upstream.output == null) return;
            int srcX = upstream.output.x;
            int srcY = upstream.output.y;
            boolean on = busState();
            // Entrée du bus côté gauche de la grille (milieu vertical)
            int tgtX = dx;
            int tgtY = dy + (ty * PT) / 2;

            int midX = (srcX + tgtX) / 2;
            // Câble épais (3 segments en L)
            g.setStroke(new BasicStroke(8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(new Color(20, 25, 35));
            g.drawLine(srcX, srcY, midX, srcY);
            g.drawLine(midX, srcY, midX, tgtY);
            g.drawLine(midX, tgtY, tgtX, tgtY);
            // Cœur du câble
            g.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(on ? new Color(255, 220, 120) : new Color(60, 70, 85));
            g.drawLine(srcX, srcY, midX, srcY);
            g.drawLine(midX, srcY, midX, tgtY);
            g.drawLine(midX, tgtY, tgtX, tgtY);

            if (on && withAnim) {
                long now = System.currentTimeMillis();
                double t = (now % 1400) / 1400.0;
                double[] pos = orthPath(srcX, srcY, midX, srcY, midX, tgtY, tgtX, tgtY, t);
                g.setColor(Color.WHITE);
                g.fillOval((int) pos[0] - 5, (int) pos[1] - 5, 10, 10);
            }
        }

        void drawWires(Graphics2D g, boolean withAnim) {
            for (Wire w : wires) w.draw(g, withAnim);
        }

        void drawComponents(Graphics2D g, boolean colored, boolean showNames) {
            for (Component c : all) {
                // Lights "non visibles" (utilisées comme points de propagation) ne dessinent rien
                c.draw(g, colored, showNames);
            }
        }
    }

    /* ════════════════════════════════════════════════════════════════════════
     *                                 Wire
     * ════════════════════════════════════════════════════════════════════════ */

    static class Wire {
        Component src;
        Component dst;
        int inputIdx;
        Grid busGrid;        // si non null, source = bus de cette grille
        Wire(Component src, Component dst, int inputIdx) {
            this.src = src; this.dst = dst; this.inputIdx = inputIdx;
        }
        static Wire busInput(Grid g, Component dst, int inputIdx) {
            Wire w = new Wire(null, dst, inputIdx);
            w.busGrid = g;
            return w;
        }

        boolean activeState() {
            if (busGrid != null) return busGrid.busState();
            return src != null && src.state;
        }

        void draw(Graphics2D g, boolean withAnim) {
            int x1, y1, x1p, y1p;
            if (busGrid != null) {
                // source = milieu gauche de la grille
                x1 = busGrid.dx;
                y1 = busGrid.dy + (busGrid.ty * PT) / 2;
                x1p = x1 + 16; y1p = y1;
            } else {
                x1 = src.outX;  y1 = src.outY;
                x1p = src.outXp; y1p = src.outYp;
            }
            int x2 = dst.inA[inputIdx][0],  y2 = dst.inA[inputIdx][1];
            int x2p = dst.inAp[inputIdx][0], y2p = dst.inAp[inputIdx][1];

            // Routage orthogonal en utilisant la pré-ancre source et la pré-ancre cible :
            // src.out → src.outP → (coude) → dst.inP → dst.in
            // Avec un coude orthogonal entre outP et inP : on choisit le point (x2p, y1p)
            // ou (x1p, y2p) selon la direction dominante.
            int kneeX, kneeY;
            if (Math.abs(x2p - x1p) >= Math.abs(y2p - y1p)) {
                kneeX = (x1p + x2p) / 2; kneeY = y1p;
                drawSegments(g, withAnim,
                    new int[][]{ {x1, y1}, {x1p, y1p}, {kneeX, y1p}, {kneeX, y2p}, {x2p, y2p}, {x2, y2} });
            } else {
                kneeX = x1p; kneeY = (y1p + y2p) / 2;
                drawSegments(g, withAnim,
                    new int[][]{ {x1, y1}, {x1p, y1p}, {x1p, kneeY}, {x2p, kneeY}, {x2p, y2p}, {x2, y2} });
            }
        }

        private void drawSegments(Graphics2D g, boolean withAnim, int[][] pts) {
            boolean on = activeState();
            g.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(on ? new Color(255, 220, 120) : new Color(50, 60, 75));
            for (int i = 0; i < pts.length - 1; i++) {
                g.drawLine(pts[i][0], pts[i][1], pts[i+1][0], pts[i+1][1]);
            }
            if (on && withAnim) {
                // Calculer la longueur totale et placer un point de lumière
                double total = 0;
                double[] segLen = new double[pts.length - 1];
                for (int i = 0; i < pts.length - 1; i++) {
                    segLen[i] = Math.hypot(pts[i+1][0] - pts[i][0], pts[i+1][1] - pts[i][1]);
                    total += segLen[i];
                }
                long now = System.currentTimeMillis();
                double t = (now % 1200) / 1200.0;
                double d = t * total;
                for (int i = 0; i < segLen.length; i++) {
                    if (d <= segLen[i]) {
                        double u = segLen[i] == 0 ? 0 : d / segLen[i];
                        double xx = pts[i][0] + (pts[i+1][0] - pts[i][0]) * u;
                        double yy = pts[i][1] + (pts[i+1][1] - pts[i][1]) * u;
                        g.setColor(Color.WHITE);
                        g.fillOval((int) xx - 3, (int) yy - 3, 6, 6);
                        return;
                    }
                    d -= segLen[i];
                }
            }
        }
    }

    /* ════════════════════════════════════════════════════════════════════════
     *                              Utilitaires
     * ════════════════════════════════════════════════════════════════════════ */

    /** Rotation entière d'un point autour d'un pivot d'un angle en degrés. */
    static int[] rotate(int px, int py, int cx, int cy, int angleDeg) {
        double a = Math.toRadians(angleDeg);
        double dx = px - cx, dy = py - cy;
        double xr = dx * Math.cos(a) - dy * Math.sin(a);
        double yr = dx * Math.sin(a) + dy * Math.cos(a);
        return new int[]{ (int) Math.round(cx + xr), (int) Math.round(cy + yr) };
    }

    /** Angle de rotation associé à un sens (en degrés, sens trigonométrique screen). */
    static int sensAngle(Sens s) {
        return switch (s) {
            case RIGHT -> 0;
            case LEFT  -> 180;
            case UP    -> -90;
            case DOWN  ->  90;
        };
    }

    /** Position le long d'un trajet 4-points pour les animations bus. */
    static double[] orthPath(int x1, int y1, int x2, int y2,
                             int x3, int y3, int x4, int y4, double t) {
        double l1 = Math.hypot(x2-x1, y2-y1),
               l2 = Math.hypot(x3-x2, y3-y2),
               l3 = Math.hypot(x4-x3, y4-y3),
               tot = l1 + l2 + l3 + 1e-6;
        double d = t * tot;
        if (d < l1) { double u = d / l1; return new double[]{ x1 + (x2-x1)*u, y1 + (y2-y1)*u }; }
        d -= l1;
        if (d < l2) { double u = d / l2; return new double[]{ x2 + (x3-x2)*u, y2 + (y3-y2)*u }; }
        d -= l2;
        double u = d / l3; return new double[]{ x3 + (x4-x3)*u, y3 + (y4-y3)*u };
    }
}
