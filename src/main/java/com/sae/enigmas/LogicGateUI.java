package com.sae.enigmas;
import com.roxane.app.Translations;

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
 * Phase 3.4 — "Portes Logiques".
 * <p>
 * Port en Java de {@code SAE_Classe_Enigme_GATE_Remake.py} pour GR1/GR2/GR3,
 * étendu avec deux niveaux de difficulté supplémentaires :
 *
 * <ul>
 *   <li><b>Easy</b>   : GR1 + une grille à 5 boutons implémentant
 *       <em>(A∧B) ∧ ¬C ∧ (D⊕A) ∧ (E⊕¬B)</em> → solution unique
 *       A=1, B=1, C=0, D=0, E=1.</li>
 *   <li><b>Normal</b> : GR1 + GR2 + GR3 (prototype Python).</li>
 *   <li><b>Hard</b>   : GR1 + UNE grosse grille à l'aveugle implémentant les
 *       10 sous-expressions S1..S10 ANDées ensemble. 15 boutons A..O.</li>
 * </ul>
 */
public class LogicGateUI extends EnigmaDialog {

    private enum Difficulty { EASY, NORMAL, HARD }
    private enum Sens { RIGHT, LEFT, UP, DOWN }

    private final int PT;
    private final Difficulty diff;
    private final boolean showAnimations;
    private final boolean showGateColors;
    private final boolean showGateNames;

    private final List<Grid> grids = new ArrayList<>();
    private Light winLight;
    private final Rectangle btnReset;

    public LogicGateUI(Window parent, Save save) {
        super(parent, Translations.t("LOGIC_TITLE"),
              widthFor(diffFrom(save)), heightFor(diffFrom(save)));
        this.diff = diffFrom(save);
        this.PT  = ptFor(diff);

        this.showAnimations = (diff == Difficulty.EASY);
        this.showGateColors = (diff == Difficulty.EASY);
        this.showGateNames  = (diff == Difficulty.EASY);

        this.btnReset = new Rectangle(widthFor(diff) - 200, heightFor(diff) - 100, 170, 36);

        construire();
        // Init : on évalue une première fois (pour les NOT à 1 dès le départ)
        for (Grid g : grids) g.evaluate();

        setStatus(Translations.t("LOGIC_STATUS"),
                  new Color(41, 128, 185));
    }

    /* ════════════════════════════════════════════════════════════════════════ */

    private static Difficulty diffFrom(Save save) {
        String d = (save != null && save.getDifficulty() != null) ? save.getDifficulty() : "Normal";
        return switch (d) {
            case "Easy" -> Difficulty.EASY;
            case "Hard" -> Difficulty.HARD;
            default      -> Difficulty.NORMAL;
        };
    }

    private static int widthFor(Difficulty d) {
        return switch (d) {
            case EASY   -> 1000;
            case NORMAL -> 1320;
            case HARD   -> 1320;
        };
    }
    private static int heightFor(Difficulty d) {
        return switch (d) {
            case EASY   -> 620;
            case NORMAL -> 820;
            case HARD   -> 820;
        };
    }
    private static int ptFor(Difficulty d) {
        return switch (d) {
            case EASY   -> 11;
            case NORMAL -> 8;
            case HARD   -> 7;
        };
    }

    /* ════════════════════════════════════════════════════════════════════════ */

    private void construire() {
        Grid g1 = buildGR1(40, 50);
        grids.add(g1);

        switch (diff) {
            case EASY -> {
                int g2x = 40 + 40 * PT + 30;
                Grid g2 = buildEasyG2(g2x, 50, g1);
                grids.add(g2);
                winLight = g2.output;
            }
            case NORMAL -> {
                int g2x = 40 + 40 * PT + 30;
                Grid g2 = buildGR2(g2x, 50, g1);
                grids.add(g2);
                int row2Y = 50 + 28 * PT + 30;
                Grid g3 = buildGR3(40, row2Y, g2);
                grids.add(g3);
                winLight = g3.output;
            }
            case HARD -> {
                int g2y = 50 + 28 * PT + 30;
                Grid g2 = buildHardBig(40, g2y, g1);
                grids.add(g2);
                winLight = g2.output;
            }
        }
    }

    /* ════════════════════════════════════════════════════════════════════════
     *                      GR1 — Reproduction du prototype
     * ════════════════════════════════════════════════════════════════════════ */

    private Grid buildGR1(int gx, int gy) {
        Grid g = new Grid(gx, gy, 40, 27, "Grille 1", PT);

        Button B1 = (Button) g.place(new Button(Sens.RIGHT), 0, 0,  4, 4);
        Button B2 = (Button) g.place(new Button(Sens.RIGHT), 0, 1,  7, 1);
        Button B3 = (Button) g.place(new Button(Sens.RIGHT), 0, 1,  4, 7);
        Button B4 = (Button) g.place(new Button(Sens.RIGHT), 0, 2,  7, 4);

        Gate G1 = (Gate) g.place(new Gate("AND", Sens.RIGHT), 2, 0,  1, 5);
        Gate G2 = (Gate) g.place(new Gate("NOT", Sens.RIGHT), 2, 1,  1, 5);
        Gate G3 = (Gate) g.place(new Gate("XOR", Sens.RIGHT), 1, 2,  8, 4);
        Gate G4 = (Gate) g.place(new Gate("AND", Sens.RIGHT), 3, 0,  4, 8);
        Gate G5 = (Gate) g.place(new Gate("AND", Sens.DOWN ), 3, 2,  8, 1);

        Light L1 = (Light) g.place(new Light(Sens.RIGHT, true), 1, 0,  2, 4);
        Light L2 = (Light) g.place(new Light(Sens.RIGHT, true), 1, 0,  5, 9);
        Light L3 = (Light) g.place(new Light(Sens.RIGHT, true), 1, 1,  3, 5);
        Light L4 = (Light) g.place(new Light(Sens.RIGHT, true), 1, 2,  3, 1);
        Light L5 = (Light) g.place(new Light(Sens.RIGHT, true), 1, 2,  3, 8);
        Light L6 = (Light) g.place(new Light(Sens.RIGHT, true), 2, 0,  8, 6);
        Light L7 = (Light) g.place(new Light(Sens.RIGHT, true), 2, 1,  7, 5);
        Light L8 = (Light) g.place(new Light(Sens.RIGHT, true), 2, 2,  7, 4);
        Light L9 = (Light) g.place(new Light(Sens.DOWN,  true), 3, 2,  8, 7);

        L1.connect(B1); L2.connect(B2); L3.connect(B3);
        L4.connect(B3); L5.connect(B4);
        G1.connect(L2, L1);
        G2.connect(L3);
        G3.connect(L5, L4);
        L6.connect(G1); L7.connect(G2); L8.connect(G3);
        G4.connect(L7, L6);
        G5.connect(L8, G4);
        L9.connect(G5);

        g.output = L9;
        g.buildWires();
        return g;
    }

    /* ════════════════════════════════════════════════════════════════════════
     *                                  GR2 / GR3
     * ════════════════════════════════════════════════════════════════════════ */

    private Grid buildGR2(int gx, int gy, Grid input) {
        Grid g = new Grid(gx, gy, 54, 27, "Grille 2", PT);
        g.upstream = input;

        Button B1 = (Button) g.place(new Button(Sens.RIGHT), 1, 0, 2, 6);
        Button B2 = (Button) g.place(new Button(Sens.RIGHT), 2, 0, 1, 5);
        Button B3 = (Button) g.place(new Button(Sens.RIGHT), 2, 0, 9, 6);
        Button B4 = (Button) g.place(new Button(Sens.RIGHT), 3, 0, 7, 4);
        Button B5 = (Button) g.place(new Button(Sens.RIGHT), 4, 0, 5, 6);

        Gate G1  = (Gate) g.place(new Gate("AND", Sens.LEFT),  1, 1, 2, 5);
        Gate G2  = (Gate) g.place(new Gate("NOT", Sens.UP),    2, 1, 5, 7);
        Gate G3  = (Gate) g.place(new Gate("NOT", Sens.RIGHT), 3, 1, 4, 8);
        Gate G4  = (Gate) g.place(new Gate("AND", Sens.LEFT),  3, 1, 8, 4);
        Gate G5  = (Gate) g.place(new Gate("NOT", Sens.DOWN),  4, 1, 9, 4);
        Gate G6  = (Gate) g.place(new Gate("NOT", Sens.DOWN),  0, 2, 9, 4);
        Gate G7  = (Gate) g.place(new Gate("AND", Sens.DOWN),  1, 2, 6, 3);
        Gate G8  = (Gate) g.place(new Gate("AND", Sens.RIGHT), 2, 2, 4, 5);
        Gate G9  = (Gate) g.place(new Gate("AND", Sens.RIGHT), 3, 2, 7, 6);
        Gate G10 = (Gate) g.place(new Gate("AND", Sens.RIGHT), 5, 2, 2, 5);

        Light Lout = (Light) g.place(new Light(Sens.RIGHT, true), 5, 2, 7, 5);

        G1.connect(B1, B1); G3.connect(B3); G4.connect(B4, G3);
        G5.connect(B5); G6.connect(G5); G8.connect(B1, B2);
        G2.connect(G8); G7.connect(G1, G2); G9.connect(G4, G6);
        G10.connect(G7, G9); Lout.connect(G10);

        g.output = Lout;
        g.buildWires();
        return g;
    }

    private Grid buildGR3(int gx, int gy, Grid input) {
        Grid g = new Grid(gx, gy, 99, 27, "Grille 3", PT);
        g.upstream = input;

        Button B1  = (Button) g.place(new Button(Sens.RIGHT), 1, 0, 2, 4);
        Button B2  = (Button) g.place(new Button(Sens.RIGHT), 0, 1, 7, 1);
        Button B3  = (Button) g.place(new Button(Sens.RIGHT), 1, 1, 1, 8);
        Button B4  = (Button) g.place(new Button(Sens.RIGHT), 0, 2, 7, 5);
        Button B5  = (Button) g.place(new Button(Sens.RIGHT), 2, 0, 2, 4);
        Button B6  = (Button) g.place(new Button(Sens.RIGHT), 4, 0, 9, 4);
        Button B7  = (Button) g.place(new Button(Sens.RIGHT), 5, 0, 7, 6);
        Button B8  = (Button) g.place(new Button(Sens.RIGHT), 6, 0, 4, 4);
        Button B9  = (Button) g.place(new Button(Sens.RIGHT), 7, 0, 2, 6);
        Button B10 = (Button) g.place(new Button(Sens.RIGHT), 7, 0, 9, 4);

        Gate G1  = (Gate) g.place(new Gate("AND", Sens.DOWN),  3, 0, 2, 6);
        Gate G2  = (Gate) g.place(new Gate("NOT", Sens.RIGHT), 1, 1, 7, 4);
        Gate G3  = (Gate) g.place(new Gate("AND", Sens.RIGHT), 2, 1, 6, 6);
        Gate G4  = (Gate) g.place(new Gate("XOR", Sens.DOWN),  3, 1, 5, 4);
        Gate G5  = (Gate) g.place(new Gate("AND", Sens.DOWN),  3, 1, 3, 9);
        Gate G6  = (Gate) g.place(new Gate("NOT", Sens.RIGHT), 1, 2, 7, 6);
        Gate G7  = (Gate) g.place(new Gate("NOT", Sens.DOWN),  2, 2, 3, 3);
        Gate G8  = (Gate) g.place(new Gate("AND", Sens.RIGHT), 2, 2, 8, 6);
        Gate G9  = (Gate) g.place(new Gate("AND", Sens.RIGHT), 3, 2, 9, 7);
        Gate G10 = (Gate) g.place(new Gate("NOT", Sens.DOWN),  5, 1, 8, 3);
        Gate G11 = (Gate) g.place(new Gate("XOR", Sens.DOWN),  5, 1, 4, 8);
        Gate G12 = (Gate) g.place(new Gate("NOT", Sens.RIGHT), 5, 2, 7, 3);
        Gate G13 = (Gate) g.place(new Gate("XOR", Sens.DOWN),  6, 1, 3, 8);
        Gate G14 = (Gate) g.place(new Gate("AND", Sens.RIGHT), 6, 2, 9, 1);
        Gate G15 = (Gate) g.place(new Gate("NOT", Sens.RIGHT), 7, 1, 5, 5);
        Gate G16 = (Gate) g.place(new Gate("AND", Sens.RIGHT), 8, 1, 2, 4);
        Gate G17 = (Gate) g.place(new Gate("AND", Sens.UP),    8, 1, 7, 1);
        Gate G18 = (Gate) g.place(new Gate("AND", Sens.RIGHT), 9, 0, 5, 6);
        Gate G19 = (Gate) g.place(new Gate("AND", Sens.DOWN), 10, 0, 2, 9);
        Gate G20 = (Gate) g.place(new Gate("AND", Sens.RIGHT),10, 2, 3, 5);

        Light L1 = (Light) g.place(new Light(Sens.RIGHT, true), 10, 2, 8, 8);

        G1.connect(B5, B6); G2.connect(B1); G3.connect(B2, G2);
        G4.connect(B1, G1); G5.connect(G3, G4); G6.connect(B4);
        G7.connect(B3); G8.connect(G6, G7); G9.connect(G8, G5);
        G10.connect(B7); G11.connect(B6, G10); G12.connect(G11);
        G13.connect(B7, B8); G14.connect(G13, B8); G15.connect(B9);
        G16.connect(G15, B10); G17.connect(G14, G16);
        G18.connect(G12, G17); G19.connect(G18, null);
        G20.connect(G9, G19); L1.connect(G20);

        g.output = L1;
        g.buildWires();
        return g;
    }

    /* ════════════════════════════════════════════════════════════════════════
     *  EASY G2 — (A∧B) ∧ ¬C ∧ (D⊕A) ∧ (E⊕¬B)   [solution unique : 1 1 0 0 1]
     * ════════════════════════════════════════════════════════════════════════ */

    private Grid buildEasyG2(int gx, int gy, Grid input) {
        Grid g = new Grid(gx, gy, 36, 27, "Grille 2 - Easy", PT);
        g.upstream = input;

        // Boutons en colonne gauche
        Button A = (Button) g.placePT(new Button(Sens.RIGHT), 4,  3);
        Button B = (Button) g.placePT(new Button(Sens.RIGHT), 4,  8);
        Button C = (Button) g.placePT(new Button(Sens.RIGHT), 4, 13);
        Button D = (Button) g.placePT(new Button(Sens.RIGHT), 4, 19);
        Button E = (Button) g.placePT(new Button(Sens.RIGHT), 4, 24);

        // Niveau 1 : portes unaires & XOR
        Gate t1 = (Gate) g.placePT(new Gate("AND", Sens.RIGHT), 13,  5); // A∧B
        Gate t4 = (Gate) g.placePT(new Gate("NOT", Sens.DOWN),  10, 10); // ¬B (route vers le bas)
        Gate t2 = (Gate) g.placePT(new Gate("NOT", Sens.RIGHT), 13, 13); // ¬C
        Gate t3 = (Gate) g.placePT(new Gate("XOR", Sens.RIGHT), 13, 21); // D⊕A
        Gate t5 = (Gate) g.placePT(new Gate("XOR", Sens.RIGHT), 16, 26); // E⊕¬B  (¬B = t4)

        // Niveau 2 : combinaisons par paires
        Gate top = (Gate) g.placePT(new Gate("AND", Sens.RIGHT), 22,  9); // t1∧t2
        Gate bot = (Gate) g.placePT(new Gate("AND", Sens.RIGHT), 22, 23); // t3∧t5

        // Niveau 3 : fusion
        Gate expr  = (Gate) g.placePT(new Gate("AND", Sens.RIGHT), 27, 16); // top∧bot
        Gate withX = (Gate) g.placePT(new Gate("AND", Sens.RIGHT), 31, 16); // expr ∧ upstream

        Light Lout = (Light) g.placePT(new Light(Sens.RIGHT, true), 34, 16);

        t1.connect(A, B);
        t4.connect(B);
        t2.connect(C);
        t3.connect(D, A);
        t5.connect(E, t4);
        top.connect(t1, t2);
        bot.connect(t3, t5);
        expr.connect(top, bot);
        withX.connect(expr, null); // upstream bus
        Lout.connect(withX);

        g.output = Lout;
        g.buildWires();
        return g;
    }

    /* ════════════════════════════════════════════════════════════════════════
     *  HARD — 1 grosse grille à l'aveugle implémentant S1..S10 (AND de tout).
     *
     *  Signaux :
     *    S1  = (A⊕B) ∧ (C⊕D)
     *    S2  = E ⊕ (A∧C)
     *    S3  = F ⊕ (B∧D)
     *    S4  = G ⊕ (E⊕F)
     *    S5  = H ⊕ (¬A ∧ G)
     *    S6  = I ⊕ (C ∧ ¬F)
     *    S7  = J ⊕ (H⊕I)
     *    S8  = (K ⊕ (A∧D∧J)) ∧ (L ⊕ (B∧C∧¬J))
     *    S9  = (M ⊕ (K⊕L)) ∧ (N ⊕ (G ∧ ¬M))
     *    S10 = O ⊕ (M ⊕ (N ∧ ¬E))
     *
     *  Sortie finale = S1 ∧ … ∧ S10 ∧ X(upstream).
     *  Solutions : 4 (les 4 combinaisons (A,B,C,D) satisfaisant S1 imposent
     *  E..O par les autres équations).
     * ════════════════════════════════════════════════════════════════════════ */

    private Grid buildHardBig(int gx, int gy, Grid input) {
        Grid g = new Grid(gx, gy, 165, 50, "Grille 2 - Hard", PT);
        g.upstream = input;

        // Boutons A..O en haut, espacés de 11 PT (pour bien voir)
        Button[] btn = new Button[15];
        for (int i = 0; i < 15; i++) {
            btn[i] = (Button) g.placePT(new Button(Sens.RIGHT), 6 + i * 11, 4);
        }
        Button A=btn[0],B=btn[1],C=btn[2],D=btn[3],E=btn[4],F=btn[5],
               G_=btn[6],H=btn[7],I=btn[8],J=btn[9],K=btn[10],L=btn[11],
               M=btn[12],N=btn[13],O=btn[14];

        // === S1 = (A⊕B) ∧ (C⊕D) ==========================================
        Gate xorAB = (Gate) g.placePT(new Gate("XOR", Sens.DOWN), 11, 12);
        Gate xorCD = (Gate) g.placePT(new Gate("XOR", Sens.DOWN), 33, 12);
        Gate S1    = (Gate) g.placePT(new Gate("AND", Sens.RIGHT), 22, 19);
        xorAB.connect(A, B); xorCD.connect(C, D); S1.connect(xorAB, xorCD);

        // === S2 = E ⊕ (A∧C) ===============================================
        Gate andAC = (Gate) g.placePT(new Gate("AND", Sens.DOWN), 17, 12);
        Gate S2    = (Gate) g.placePT(new Gate("XOR", Sens.DOWN), 50, 12);
        andAC.connect(A, C); S2.connect(E, andAC);

        // === S3 = F ⊕ (B∧D) ===============================================
        Gate andBD = (Gate) g.placePT(new Gate("AND", Sens.DOWN), 28, 12);
        Gate S3    = (Gate) g.placePT(new Gate("XOR", Sens.DOWN), 61, 12);
        andBD.connect(B, D); S3.connect(F, andBD);

        // === S4 = G ⊕ (E⊕F) ===============================================
        Gate xorEF = (Gate) g.placePT(new Gate("XOR", Sens.DOWN), 55, 19);
        Gate S4    = (Gate) g.placePT(new Gate("XOR", Sens.DOWN), 72, 12);
        xorEF.connect(E, F); S4.connect(G_, xorEF);

        // === S5 = H ⊕ (¬A ∧ G) ============================================
        Gate notA1   = (Gate) g.placePT(new Gate("NOT", Sens.DOWN),  6, 11);
        Gate andNAG  = (Gate) g.placePT(new Gate("AND", Sens.DOWN), 70, 19);
        Gate S5      = (Gate) g.placePT(new Gate("XOR", Sens.DOWN), 83, 12);
        notA1.connect(A); andNAG.connect(notA1, G_); S5.connect(H, andNAG);

        // === S6 = I ⊕ (C ∧ ¬F) ============================================
        Gate notF1   = (Gate) g.placePT(new Gate("NOT", Sens.DOWN), 61, 19);
        Gate andCNF  = (Gate) g.placePT(new Gate("AND", Sens.DOWN), 80, 26);
        Gate S6      = (Gate) g.placePT(new Gate("XOR", Sens.DOWN), 94, 12);
        notF1.connect(F); andCNF.connect(C, notF1); S6.connect(I, andCNF);

        // === S7 = J ⊕ (H⊕I) ===============================================
        Gate xorHI = (Gate) g.placePT(new Gate("XOR", Sens.DOWN),  88, 19);
        Gate S7    = (Gate) g.placePT(new Gate("XOR", Sens.DOWN), 105, 12);
        xorHI.connect(H, I); S7.connect(J, xorHI);

        // === S8 = (K ⊕ (A∧D∧J)) ∧ (L ⊕ (B∧C∧¬J)) ==========================
        Gate andAD  = (Gate) g.placePT(new Gate("AND", Sens.DOWN), 115, 12);
        Gate andADJ = (Gate) g.placePT(new Gate("AND", Sens.DOWN), 117, 19);
        Gate xorK   = (Gate) g.placePT(new Gate("XOR", Sens.DOWN), 118, 26);
        Gate notJ1  = (Gate) g.placePT(new Gate("NOT", Sens.DOWN), 105, 19);
        Gate andBC  = (Gate) g.placePT(new Gate("AND", Sens.DOWN), 125, 12);
        Gate andBCJ = (Gate) g.placePT(new Gate("AND", Sens.DOWN), 127, 19);
        Gate xorL   = (Gate) g.placePT(new Gate("XOR", Sens.DOWN), 128, 26);
        Gate S8     = (Gate) g.placePT(new Gate("AND", Sens.RIGHT), 123, 33);
        andAD.connect(A, D); andADJ.connect(andAD, J);
        xorK.connect(K, andADJ);
        notJ1.connect(J);
        andBC.connect(B, C); andBCJ.connect(andBC, notJ1);
        xorL.connect(L, andBCJ);
        S8.connect(xorK, xorL);

        // === S9 = (M ⊕ (K⊕L)) ∧ (N ⊕ (G ∧ ¬M)) ============================
        Gate xorKL  = (Gate) g.placePT(new Gate("XOR", Sens.DOWN), 134, 12);
        Gate xorM   = (Gate) g.placePT(new Gate("XOR", Sens.DOWN), 138, 19);
        Gate notM1  = (Gate) g.placePT(new Gate("NOT", Sens.DOWN), 134, 26);
        Gate andGNM = (Gate) g.placePT(new Gate("AND", Sens.DOWN), 145, 19);
        Gate xorN   = (Gate) g.placePT(new Gate("XOR", Sens.DOWN), 148, 26);
        Gate S9     = (Gate) g.placePT(new Gate("AND", Sens.RIGHT), 143, 33);
        xorKL.connect(K, L); xorM.connect(M, xorKL);
        notM1.connect(M); andGNM.connect(G_, notM1);
        xorN.connect(N, andGNM);
        S9.connect(xorM, xorN);

        // === S10 = O ⊕ (M ⊕ (N ∧ ¬E)) =====================================
        Gate notE1   = (Gate) g.placePT(new Gate("NOT", Sens.DOWN), 50, 19);
        Gate andNNE  = (Gate) g.placePT(new Gate("AND", Sens.DOWN),155, 19);
        Gate xorMNE  = (Gate) g.placePT(new Gate("XOR", Sens.DOWN),158, 26);
        Gate S10     = (Gate) g.placePT(new Gate("XOR", Sens.RIGHT),162, 33);
        notE1.connect(E); andNNE.connect(N, notE1);
        xorMNE.connect(M, andNNE);
        S10.connect(O, xorMNE);

        // === Cascade finale : AND de tous les S_i ∧ upstream ==============
        // On combine en arbre binaire à droite (caseY=4)
        Gate a12   = (Gate) g.placePT(new Gate("AND", Sens.RIGHT),  40, 40); // S1∧S2
        Gate a34   = (Gate) g.placePT(new Gate("AND", Sens.RIGHT),  60, 40); // S3∧S4
        Gate a1234 = (Gate) g.placePT(new Gate("AND", Sens.RIGHT),  75, 40); // (S1S2)∧(S3S4)
        Gate a56   = (Gate) g.placePT(new Gate("AND", Sens.RIGHT),  90, 40); // S5∧S6
        Gate a78   = (Gate) g.placePT(new Gate("AND", Sens.RIGHT), 110, 40); // S7∧S8
        Gate a5678 = (Gate) g.placePT(new Gate("AND", Sens.RIGHT), 125, 40); // (S5S6)∧(S7S8)
        Gate a9_10 = (Gate) g.placePT(new Gate("AND", Sens.RIGHT), 145, 40); // S9∧S10
        Gate all1  = (Gate) g.placePT(new Gate("AND", Sens.RIGHT), 100, 45); // (S1..4)∧(S5..8)
        Gate all2  = (Gate) g.placePT(new Gate("AND", Sens.RIGHT), 140, 45); // ((..)∧(..))∧(S9S10)
        Gate withX = (Gate) g.placePT(new Gate("AND", Sens.RIGHT), 155, 47); // ∧ upstream
        a12.connect(S1, S2); a34.connect(S3, S4); a1234.connect(a12, a34);
        a56.connect(S5, S6); a78.connect(S7, S8); a5678.connect(a56, a78);
        a9_10.connect(S9, S10);
        all1.connect(a1234, a5678);
        all2.connect(all1, a9_10);
        withX.connect(all2, null); // upstream

        Light finalLight = (Light) g.placePT(new Light(Sens.RIGHT, true), 162, 47);
        finalLight.connect(withX);

        g.output = finalLight;
        g.buildWires();
        return g;
    }

    /* ════════════════════════════════════════════════════════════════════════
     *                          Évaluation / Évents
     * ════════════════════════════════════════════════════════════════════════ */

    private void evaluate() {
        for (Grid gr : grids) gr.evaluate();
        if (winLight != null && winLight.state) {
            setStatus(Translations.t("LOGIC_OK"),
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
        for (Grid gr : grids) gr.drawBackground(g);
        for (Grid gr : grids) gr.drawBusCable(g, showAnimations);
        for (Grid gr : grids) gr.drawWires(g, showAnimations);
        for (Grid gr : grids) gr.drawComponents(g, showGateColors, showGateNames);

        // Voyants d'état des grilles (en haut à gauche) en mode Easy
        if (this.diff == Difficulty.EASY) {
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
        String t = Translations.t("LOGIC_RESET");
        int tw = g.getFontMetrics().stringWidth(t);
        g.drawString(t, btnReset.x + (btnReset.width - tw) / 2, btnReset.y + 24);

        if (winLight != null) {
            g.setFont(new Font("SansSerif", Font.BOLD, 13));
            g.setColor(winLight.state ? new Color(46, 204, 113) : new Color(220, 220, 220));
            g.drawString(String.format(Translations.t("LOGIC_FINAL_FMT"), (winLight.state ? "ON" : "OFF")), 30, h - 60);
        }
    }

    /* ════════════════════════════════════════════════════════════════════════
     *                              Composants
     * ════════════════════════════════════════════════════════════════════════ */

    static abstract class Component {
        int x, y;
        Sens sens = Sens.RIGHT;
        boolean state;

        int[][] inA  = new int[0][2];
        int[][] inAp = new int[0][2];
        int outX, outY;
        int outXp, outYp;

        abstract void evaluate();
        boolean contains(Vec2 p) { return false; }
        abstract void draw(Graphics2D g, boolean colored, boolean showName);
        abstract void computeAnchors();
    }

    static class Button extends Component {
        boolean pushed;
        Component upstream;
        boolean linkedToGrid;

        Button(Sens sens) { this.sens = sens; }
        void connect(Component up) { this.upstream = up; }

        @Override void evaluate() {
            boolean up = (upstream == null) || upstream.state;
            state = pushed && up;
        }
        @Override boolean contains(Vec2 p) { return Math.hypot(p.x - x, p.y - y) < 20; }

        @Override void computeAnchors() {
            int r = 22;
            int[] o  = rotate(x + r, y, x, y, sensAngle(sens));
            int[] op = rotate(x + 2 * r, y, x, y, sensAngle(sens));
            outX  = o[0];  outY  = o[1];
            outXp = op[0]; outYp = op[1];
            inA = new int[0][2]; inAp = new int[0][2];
        }

        @Override void draw(Graphics2D g, boolean colored, boolean showName) {
            Color ringDark = new Color(40, 40, 40);
            Color baseRing = new Color(170, 170, 170);
            Draw.circle(g, x, y, 22, ringDark);
            Draw.circle(g, x, y, 19, baseRing);
            Color col;
            if (state) col = new Color(219, 106, 0);
            else if (pushed) col = new Color(80, 50, 25);
            else col = new Color(110, 53, 0);
            Draw.circle(g, x, y, 14, col);
        }
    }

    static class Light extends Component {
        final boolean visible;
        Component upstream;
        Light(Sens sens, boolean visible) { this.sens = sens; this.visible = visible; }
        void connect(Component up) { this.upstream = up; }

        @Override void evaluate() { state = (upstream != null) && upstream.state; }

        @Override void computeAnchors() {
            int r = 12;
            int[] in0 = rotate(x - 2 * r, y, x, y, sensAngle(sens));
            inA  = new int[][]{ { x, y } };
            inAp = new int[][]{ in0 };
            int[] o  = rotate(x + r, y, x, y, sensAngle(sens));
            int[] op = rotate(x + 2 * r, y, x, y, sensAngle(sens));
            outX = o[0]; outY = o[1];
            outXp = op[0]; outYp = op[1];
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
        }
    }

    static class Gate extends Component {
        final String type;
        final List<Component> inputs = new ArrayList<>();
        Grid parentGrid;
        Gate(String type, Sens sens) { this.type = type; this.sens = sens; }
        void connect(Component... ins) { inputs.addAll(Arrays.asList(ins)); }

        private boolean inputState(Component c) {
            if (c != null) return c.state;
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
            int size = 18;
            if (type.equals("NOT")) {
                int[] in  = rotate(x - size, y, x, y, sensAngle(sens));
                int[] inP = rotate(x - 2 * size, y, x, y, sensAngle(sens));
                inA  = new int[][]{ in };
                inAp = new int[][]{ inP };
                int[] out  = rotate(x + size + 6, y, x, y, sensAngle(sens));
                int[] outP = rotate(x + 2 * size + 6, y, x, y, sensAngle(sens));
                outX = out[0]; outY = out[1];
                outXp = outP[0]; outYp = outP[1];
            } else {
                int dy = 8;
                int[] in1  = rotate(x - size, y - dy, x, y, sensAngle(sens));
                int[] in1P = rotate(x - 2 * size, y - dy, x, y, sensAngle(sens));
                int[] in2  = rotate(x - size, y + dy, x, y, sensAngle(sens));
                int[] in2P = rotate(x - 2 * size, y + dy, x, y, sensAngle(sens));
                inA  = new int[][]{ in1, in2 };
                inAp = new int[][]{ in1P, in2P };
                int[] out  = rotate(x + size, y, x, y, sensAngle(sens));
                int[] outP = rotate(x + 2 * size, y, x, y, sensAngle(sens));
                outX = out[0]; outY = out[1];
                outXp = outP[0]; outYp = outP[1];
            }
        }

        @Override void draw(Graphics2D g, boolean colored, boolean showName) {
            Color stateCol = colored ? colorByType(type) : new Color(80, 90, 100);
            Color bodyCol  = state && colored ? stateCol : new Color(60, 65, 75);

            int t = 18;
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
            int[] p1 = rotate(x - t, y - t, x, y, angle);
            int[] p2 = rotate(x - t, y + t, x, y, angle);
            int[] p3 = rotate(x + t, y,     x, y, angle);
            Polygon p = new Polygon(
                new int[]{p1[0], p2[0], p3[0]},
                new int[]{p1[1], p2[1], p3[1]}, 3);
            g.setColor(col); g.fillPolygon(p);
            g.setStroke(new BasicStroke(2)); g.setColor(Color.WHITE); g.drawPolygon(p);
            int[] dot = rotate(x + t + 4, y, x, y, angle);
            Draw.circle(g, dot[0], dot[1], 4, col);
            Draw.circle(g, dot[0], dot[1], 4, Color.WHITE, 2);
        }

        private void drawAnd(Graphics2D g, Color col, int t, int angle) {
            java.awt.geom.AffineTransform old = g.getTransform();
            g.translate(x, y);
            g.rotate(Math.toRadians(-angle));
            Area a = new Area(new java.awt.Rectangle(-t, -t, t, 2 * t));
            a.add(new Area(new Ellipse2D.Double(-t / 2.0, -t, 2 * t, 2 * t)));
            g.setColor(col); g.fill(a);
            g.setStroke(new BasicStroke(2)); g.setColor(Color.WHITE); g.draw(a);
            g.setTransform(old);
        }

        private void drawXor(Graphics2D g, Color col, int t, int angle) {
            java.awt.geom.AffineTransform old = g.getTransform();
            g.translate(x, y);
            g.rotate(Math.toRadians(-angle));
            int[] xs = { -t, -t, t + 2 };
            int[] ys = { -t,  t, 0 };
            g.setColor(col); g.fillPolygon(xs, ys, 3);
            g.setStroke(new BasicStroke(2)); g.setColor(Color.WHITE); g.drawPolygon(xs, ys, 3);
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
        final int dx, dy, tx, ty;
        final int PT;
        final String name;

        final List<Component> all = new ArrayList<>();
        final List<Button> buttons = new ArrayList<>();
        final List<Wire> wires = new ArrayList<>();

        Grid upstream;
        Light output;

        Grid(int dx, int dy, int tx, int ty, String name, int PT) {
            this.dx = dx; this.dy = dy; this.tx = tx; this.ty = ty;
            this.name = name; this.PT = PT;
        }

        /** Place par coord Python (case + local). */
        Component place(Component c, int caseX, int caseY, int localX, int localY) {
            int px = dx + ((caseX * 9 + localX - 1) * PT);
            int py = dy + ((caseY * 9 + localY - 1) * PT);
            return placeAtPixels(c, px, py);
        }

        /** Place par coord PT directe (1-based). */
        Component placePT(Component c, int ptX, int ptY) {
            int px = dx + (ptX - 1) * PT;
            int py = dy + (ptY - 1) * PT;
            return placeAtPixels(c, px, py);
        }

        private Component placeAtPixels(Component c, int px, int py) {
            c.x = px; c.y = py;
            c.computeAnchors();
            all.add(c);
            if (c instanceof Button b) buttons.add(b);
            if (c instanceof Gate gt) gt.parentGrid = this;
            return c;
        }

        void resetButtons() { for (Button b : buttons) b.pushed = false; }

        void buildWires() {
            int idx = 0;
            for (Component c : all) {
                if (c instanceof Gate gt) {
                    for (int i = 0; i < gt.inputs.size(); i++) {
                        Component in = gt.inputs.get(i);
                        Wire w = (in == null) ? Wire.busInput(this, gt, i) : new Wire(in, gt, i);
                        w.orderIdx = idx++;
                        wires.add(w);
                    }
                } else if (c instanceof Light l && l.upstream != null) {
                    Wire w = new Wire(l.upstream, l, 0); w.orderIdx = idx++;
                    wires.add(w);
                } else if (c instanceof Button b && b.upstream != null) {
                    Wire w = new Wire(b.upstream, b, 0); w.orderIdx = idx++;
                    wires.add(w);
                }
            }
        }

        void evaluate() {
            for (Button b : buttons) {
                if (b.upstream == null && upstream != null) b.linkedToGrid = true;
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

        boolean busState() {
            return upstream != null && upstream.output != null && upstream.output.state;
        }

        /* ─────────────── Dessin ─────────────── */

        void drawBackground(Graphics2D g) {
            int w = tx * PT, h = ty * PT;
            Color clair  = Draw.transition("#271B0D", "#FFFFFF", 22);
            Color sombre = Draw.transition(clair, new Color(0, 0, 0), 20);
            g.setColor(clair);
            g.fillRoundRect(dx, dy, w, h, 14, 14);
            g.setColor(new Color(150, 130, 80));
            g.setStroke(new BasicStroke(2));
            g.drawRoundRect(dx, dy, w, h, 14, 14);
            g.setColor(sombre);
            g.setStroke(new BasicStroke(1));
            for (int l = 0; l <= ty / 9; l++)
                g.drawLine(dx, dy + l * 9 * PT, dx + w, dy + l * 9 * PT);
            for (int c = 0; c <= tx / 9; c++)
                g.drawLine(dx + c * 9 * PT, dy, dx + c * 9 * PT, dy + h);
            g.setColor(new Color(150, 130, 80, 120));
            for (int ix = 0; ix < tx; ix++)
                for (int iy = 0; iy < ty; iy++)
                    g.fillOval(dx + ix * PT + PT/2 - 1, dy + iy * PT + PT/2 - 1, 2, 2);
        }

        void drawBusCable(Graphics2D g, boolean withAnim) {
            if (upstream == null || upstream.output == null) return;
            int srcX = upstream.output.outX;
            int srcY = upstream.output.outY;
            boolean on = busState();
            int tgtX = dx;
            int tgtY = dy + (ty * PT) / 2;
            int midX = (srcX + tgtX) / 2;

            g.setStroke(new BasicStroke(8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(new Color(20, 25, 35));
            g.drawLine(srcX, srcY, midX, srcY);
            g.drawLine(midX, srcY, midX, tgtY);
            g.drawLine(midX, tgtY, tgtX, tgtY);
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
            for (Component c : all) c.draw(g, colored, showNames);
        }
    }

    /* ════════════════════════════════════════════════════════════════════════
     *                                 Wire
     * ════════════════════════════════════════════════════════════════════════ */

    static class Wire {
        Component src;
        Component dst;
        int inputIdx;
        Grid busGrid;
        int orderIdx;

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
                x1 = busGrid.dx; y1 = busGrid.dy + (busGrid.ty * busGrid.PT) / 2;
                x1p = x1 + 16; y1p = y1;
            } else {
                x1 = src.outX;  y1 = src.outY;
                x1p = src.outXp; y1p = src.outYp;
            }
            int x2  = dst.inA[inputIdx][0],  y2  = dst.inA[inputIdx][1];
            int x2p = dst.inAp[inputIdx][0], y2p = dst.inAp[inputIdx][1];

            int stagger = (orderIdx * 3) % 9 - 4;

            int[][] pts;
            if (Math.abs(x2p - x1p) >= Math.abs(y2p - y1p)) {
                int kneeX = (x1p + x2p) / 2 + stagger;
                pts = new int[][]{
                    {x1, y1}, {x1p, y1p},
                    {kneeX, y1p}, {kneeX, y2p},
                    {x2p, y2p}, {x2, y2}
                };
            } else {
                int kneeY = (y1p + y2p) / 2 + stagger;
                pts = new int[][]{
                    {x1, y1}, {x1p, y1p},
                    {x1p, kneeY}, {x2p, kneeY},
                    {x2p, y2p}, {x2, y2}
                };
            }
            drawSegments(g, withAnim, pts);
        }

        private void drawSegments(Graphics2D g, boolean withAnim, int[][] pts) {
            boolean on = activeState();
            g.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(new Color(20, 25, 35));
            for (int i = 0; i < pts.length - 1; i++)
                g.drawLine(pts[i][0], pts[i][1], pts[i+1][0], pts[i+1][1]);
            g.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(on ? new Color(255, 220, 120) : new Color(120, 130, 145));
            for (int i = 0; i < pts.length - 1; i++)
                g.drawLine(pts[i][0], pts[i][1], pts[i+1][0], pts[i+1][1]);
            if (on && withAnim) {
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

    /* ════════════════════════════════════════════════════════════════════════ */

    static int[] rotate(int px, int py, int cx, int cy, int angleDeg) {
        double a = Math.toRadians(angleDeg);
        double dx = px - cx, dy = py - cy;
        double xr = dx * Math.cos(a) - dy * Math.sin(a);
        double yr = dx * Math.sin(a) + dy * Math.cos(a);
        return new int[]{ (int) Math.round(cx + xr), (int) Math.round(cy + yr) };
    }

    static int sensAngle(Sens s) {
        return switch (s) {
            case RIGHT -> 0;
            case LEFT  -> 180;
            case UP    -> -90;
            case DOWN  ->  90;
        };
    }

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
