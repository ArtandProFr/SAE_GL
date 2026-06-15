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
    private static final int H = 800;
    private Save save = null;

    public MovingBallsUI(Window parent, Save save, int num) {
        super(parent, "Verrou à billes - Tiroir de Jacques", W, H);
        this.enigme = buildEnigme(save != null ? save.getDifficulty() : "Normal", num);
        this.save = save;
        setStatus("Cliquez sur les boutons périphériques pour faire glisser les billes jusqu'aux cibles.",
                new Color(41, 128, 185));
        int fps = 60;
        javax.swing.Timer timer = new javax.swing.Timer(1000 / fps, e -> {
            // On appelle update avec de fausses coordonnées de souris quand elle ne bouge pas
            // pour laisser l'animation tourner de façon fluide
            enigme.update(new Vec2(), false, false); 
            repaint(); 
        });
        timer.start();
    }

    public void changeStatus(String title, Color c){
        setStatus(title, c);
    }

    private MovingBalls buildEnigme(String diff, int num) {
        if ("Easy".equalsIgnoreCase(diff)) {
            if (num == 0){
                int N = 5;
                Slide[][] slides = new Slide[N][N];
                
                // Ligne 0
                slides[0][0] = new Slide("DOWN");
                slides[0][1] = new Slide("DOWN_RIGHT");
                slides[0][2] = new Slide("T_DOWN");
                slides[0][3] = new Slide("T_DOWN");
                slides[0][4] = new Slide("DOWN_LEFT");
                
                // Ligne 1
                slides[1][0] = new Slide("T_RIGHT");
                slides[1][1] = new Slide("T_LEFT");
                slides[1][2] = new Slide("VERT");
                slides[1][3] = new Slide("T_RIGHT");
                slides[1][4] = new Slide("T_LEFT");
                
                // Ligne 2
                slides[2][0] = new Slide("T_RIGHT");
                slides[2][1] = new Slide("UP_LEFT");
                slides[2][2] = new Slide("T_RIGHT");
                slides[2][3] = new Slide("UP_LEFT");
                slides[2][4] = new Slide("VERT");
                
                // Ligne 3
                slides[3][0] = new Slide("UP_RIGHT");
                slides[3][1] = new Slide("T_DOWN");
                slides[3][2] = new Slide("T_LEFT");
                slides[3][3] = new Slide("DOWN_RIGHT");
                slides[3][4] = new Slide("T_LEFT");

                // Ligne 4
                slides[4][0] = new Slide("RIGHT");
                slides[4][1] = new Slide("UP_LEFT");
                slides[4][2] = new Slide("UP_RIGHT");
                slides[4][3] = new Slide("T_UP");
                slides[4][4] = new Slide("UP_LEFT");

                // Positions initiales des 3 boules (Deux en haut dans les coins, une en bas à gauche)
                int[][] balls = new int[][]{ {0, 0}, {1, 3} , {3, 3}};
                
                // Cibles associées
                int[][] goals = new int[][]{ {1, 2}, {2, 1}, {3, 2} };

                return new MovingBalls(new Vec2((W - 10) / 2.0, (H - 90) / 2.0 - 10), 430, balls, goals, slides, this);
            } else if (num == 1){
                int N = 4;
                Slide[][] slides = new Slide[N][N];
                
                // Ligne 0
                slides[0][0] = new Slide("RIGHT");
                slides[0][1] = new Slide("T_DOWN");
                slides[0][2] = new Slide("DOWN_LEFT");
                slides[0][3] = new Slide("DOWN");
                
                // Ligne 1
                slides[1][0] = new Slide("DOWN_RIGHT");
                slides[1][1] = new Slide("UP_LEFT");
                slides[1][2] = new Slide("UP_RIGHT");
                slides[1][3] = new Slide("T_LEFT");
                
                // Ligne 2
                slides[2][0] = new Slide("T_RIGHT");
                slides[2][1] = new Slide("DOWN_LEFT");
                slides[2][2] = new Slide("RIGHT");
                slides[2][3] = new Slide("T_LEFT");
                
                // Ligne 3
                slides[3][0] = new Slide("UP");
                slides[3][1] = new Slide("UP_RIGHT");
                slides[3][2] = new Slide("HORI");
                slides[3][3] = new Slide("UP_LEFT");

                // Positions initiales des 3 boules (Deux en haut dans les coins, une en bas à gauche)
                int[][] balls = new int[][]{ {0, 0}, {0, 3} };
                
                // Cibles associées
                int[][] goals = new int[][]{ {3, 0}, {2, 2} };

                return new MovingBalls(new Vec2((W - 10) / 2.0, (H - 90) / 2.0 - 10), 430, balls, goals, slides, this);
            } else if (num == 2){
                int N = 4;
                Slide[][] slides = new Slide[N][N];
                
                // Ligne 0
                slides[0][0] = new Slide("RIGHT");
                slides[0][1] = new Slide("DOWN_LEFT");
                slides[0][2] = new Slide("DOWN_RIGHT");
                slides[0][3] = new Slide("DOWN_LEFT");
                
                // Ligne 1
                slides[1][0] = new Slide("DOWN_RIGHT");
                slides[1][1] = new Slide("CROSS");
                slides[1][2] = new Slide("CROSS");
                slides[1][3] = new Slide("T_LEFT");
                
                // Ligne 2
                slides[2][0] = new Slide("VERT");
                slides[2][1] = new Slide("UP_RIGHT");
                slides[2][2] = new Slide("T_LEFT");
                slides[2][3] = new Slide("UP");
                
                // Ligne 3
                slides[3][0] = new Slide("UP_RIGHT");
                slides[3][1] = new Slide("HORI");
                slides[3][2] = new Slide("T_UP");
                slides[3][3] = new Slide("LEFT");

                // Positions initiales des 3 boules (Deux en haut dans les coins, une en bas à gauche)
                int[][] balls = new int[][]{ {0, 3}, {2, 1} };
                
                // Cibles associées
                int[][] goals = new int[][]{ {1, 0}, {3, 2} };

                return new MovingBalls(new Vec2((W - 10) / 2.0, (H - 90) / 2.0 - 10), 430, balls, goals, slides, this);
            } else {
                return null;
            }
        } else if ("Hard".equalsIgnoreCase(diff)) {
            if (num == 0){
                // Mode NORMAL : Maquette Python
                int N = 5;
                Slide[][] slides = new Slide[N][N];
                
                // Ligne 0
                slides[0][0] = new Slide("DOWN_RIGHT");
                slides[0][1] = new Slide("HORI");
                slides[0][2] = new Slide("DOWN_LEFT");
                slides[0][3] = new Slide("DOWN_RIGHT");
                slides[0][4] = new Slide("DOWN_LEFT");
                // Ligne 1
                slides[1][0] = new Slide("T_RIGHT");
                slides[1][1] = new Slide("T_DOWN");
                slides[1][2] = new Slide("CROSS");
                slides[1][3] = new Slide("CROSS");
                slides[1][4] = new Slide("UP_LEFT");
                // Ligne 2
                slides[2][0] = new Slide("T_RIGHT");
                slides[2][1] = new Slide("CROSS");
                slides[2][2] = new Slide("T_LEFT");
                slides[2][3] = new Slide("T_RIGHT");
                slides[2][4] = new Slide("DOWN_LEFT");
                // Ligne 3
                slides[3][0] = new Slide("T_RIGHT");
                slides[3][1] = new Slide("CROSS");
                slides[3][2] = new Slide("T_LEFT");
                slides[3][3] = new Slide("T_RIGHT");
                slides[3][4] = new Slide("UP_LEFT");
                // Ligne 4
                slides[4][0] = new Slide("UP_RIGHT");
                slides[4][1] = new Slide("T_UP");
                slides[4][2] = new Slide("T_UP");
                slides[4][3] = new Slide("T_UP");
                slides[4][4] = new Slide("LEFT");

                // Billes d'origine de la maquette
                int[][] balls = new int[][]{ {0, 0}, {2, 2}, {0, 4}, {4, 4} };
                // Objectifs d'origine de la maquette
                int[][] goals = new int[][]{ {2, 0}, {1, 2}, {2, 3}, {4, 3} };

                return new MovingBalls(new Vec2((W - 10) / 2.0, (H - 90) / 2.0 - 10), 430, balls, goals, slides, this);
            } else if (num == 1){
                int N = 5;
                Slide[][] slides = new Slide[N][N];
                
                // Ligne 0
                slides[0][0] = new Slide("DOWN_RIGHT");
                slides[0][1] = new Slide("DOWN_LEFT");
                slides[0][2] = new Slide("DOWN_RIGHT");
                slides[0][3] = new Slide("HORI");
                slides[0][4] = new Slide("DOWN_LEFT");
                
                // Ligne 1
                slides[1][0] = new Slide("VERT");
                slides[1][1] = new Slide("UP_RIGHT");
                slides[1][2] = new Slide("CROSS");
                slides[1][3] = new Slide("DOWN_LEFT");
                slides[1][4] = new Slide("UP");
                
                // Ligne 2
                slides[2][0] = new Slide("T_RIGHT");
                slides[2][1] = new Slide("T_DOWN");
                slides[2][2] = new Slide("CROSS");
                slides[2][3] = new Slide("T_UP");
                slides[2][4] = new Slide("DOWN_LEFT");
                
                // Ligne 3
                slides[3][0] = new Slide("T_RIGHT");
                slides[3][1] = new Slide("CROSS");
                slides[3][2] = new Slide("CROSS");
                slides[3][3] = new Slide("HORI");
                slides[3][4] = new Slide("T_LEFT");
                
                // Ligne 4
                slides[4][0] = new Slide("UP");
                slides[4][1] = new Slide("UP_RIGHT");
                slides[4][2] = new Slide("UP_LEFT");
                slides[4][3] = new Slide("RIGHT");
                slides[4][4] = new Slide("UP_LEFT");

                // Positions initiales des 3 boules (Deux en haut dans les coins, une en bas à gauche)
                int[][] balls = new int[][]{ {0, 3}, {3, 0}, {4, 3} };
                
                // Cibles associées
                int[][] goals = new int[][]{ {2, 1}, {1, 4}, {3, 4} };

                return new MovingBalls(new Vec2((W - 10) / 2.0, (H - 90) / 2.0 - 10), 430, balls, goals, slides, this);
            } else {
                int N = 5;
                Slide[][] slides = new Slide[N][N];
                // Ligne 0
                slides[0][0] = new Slide("DOWN_RIGHT");
                slides[0][1] = new Slide("HORI");
                slides[0][2] = new Slide("T_DOWN");
                slides[0][3] = new Slide("T_DOWN");
                slides[0][4] = new Slide("DOWN_LEFT");

                // Ligne 1
                slides[1][0] = new Slide("UP_RIGHT");
                slides[1][1] = new Slide("HORI");
                slides[1][2] = new Slide("T_LEFT");
                slides[1][3] = new Slide("T_RIGHT");
                slides[1][4] = new Slide("UP_LEFT");

                // Ligne 2
                slides[2][0] = new Slide("DOWN_RIGHT");
                slides[2][1] = new Slide("DOWN_LEFT");
                slides[2][2] = new Slide("VERT"); // Cible : accessible uniquement par le bas
                slides[2][3] = new Slide("T_RIGHT");
                slides[2][4] = new Slide("DOWN_LEFT");

                // Ligne 3
                slides[3][0] = new Slide("VERT");
                slides[3][1] = new Slide("T_RIGHT");
                slides[3][2] = new Slide("T_UP"); // Aiguillage central vers la cible
                slides[3][3] = new Slide("T_UP");
                slides[3][4] = new Slide("T_LEFT");

                // Ligne 4
                slides[4][0] = new Slide("UP_RIGHT");
                slides[4][1] = new Slide("T_UP");
                slides[4][2] = new Slide("HORI");
                slides[4][3] = new Slide("HORI");
                slides[4][4] = new Slide("UP_LEFT");

                // Positions initiales des 3 boules
                int[][] balls = new int[][]{ {0, 0}, {3, 0}, {0, 4} };

                // Cibles associées
                int[][] goals = new int[][]{ {1, 1}, {2, 2}, {4, 3} };
                return new MovingBalls(new Vec2((W - 10) / 2.0, (H - 90) / 2.0 - 10), 430, balls, goals, slides, this);
            }

        } else {
            switch (num){
                case 0 -> {
                    // Mode NORMAL : Maquette Python
                    int N = 5;
                    Slide[][] slides = new Slide[N][N];
                    
                    // Ligne 0
                    slides[0][0] = new Slide("DOWN_RIGHT");
                    slides[0][1] = new Slide("HORI");
                    slides[0][2] = new Slide("DOWN_LEFT");
                    slides[0][3] = new Slide("DOWN_RIGHT");
                    slides[0][4] = new Slide("DOWN_LEFT");
                    // Ligne 1
                    slides[1][0] = new Slide("T_RIGHT");
                    slides[1][1] = new Slide("T_DOWN");
                    slides[1][2] = new Slide("CROSS");
                    slides[1][3] = new Slide("CROSS");
                    slides[1][4] = new Slide("UP_LEFT");
                    // Ligne 2
                    slides[2][0] = new Slide("T_RIGHT");
                    slides[2][1] = new Slide("CROSS");
                    slides[2][2] = new Slide("T_LEFT");
                    slides[2][3] = new Slide("T_RIGHT");
                    slides[2][4] = new Slide("DOWN_LEFT");
                    // Ligne 3
                    slides[3][0] = new Slide("T_RIGHT");
                    slides[3][1] = new Slide("CROSS");
                    slides[3][2] = new Slide("T_LEFT");
                    slides[3][3] = new Slide("T_RIGHT");
                    slides[3][4] = new Slide("UP_LEFT");
                    // Ligne 4
                    slides[4][0] = new Slide("UP_RIGHT");
                    slides[4][1] = new Slide("T_UP");
                    slides[4][2] = new Slide("T_UP");
                    slides[4][3] = new Slide("T_UP");
                    slides[4][4] = new Slide("LEFT");

                    // Billes d'origine de la maquette
                    int[][] balls = new int[][]{ {0, 0}, {2, 2}, {0, 4}, {4, 4} };
                    // Objectifs d'origine de la maquette
                    int[][] goals = new int[][]{ {2, 0}, {1, 2}, {2, 3}, {4, 3} };

                    return new MovingBalls(new Vec2((W - 10) / 2.0, (H - 90) / 2.0 - 10), 430, balls, goals, slides, this);
                }
                case 1 -> {
                    int N = 5;
                    Slide[][] slides = new Slide[N][N];
                    
                    // Ligne 0
                    slides[0][0] = new Slide("DOWN_RIGHT");
                    slides[0][1] = new Slide("DOWN_LEFT");
                    slides[0][2] = new Slide("DOWN_RIGHT");
                    slides[0][3] = new Slide("HORI");
                    slides[0][4] = new Slide("DOWN_LEFT");
                    
                    // Ligne 1
                    slides[1][0] = new Slide("VERT");
                    slides[1][1] = new Slide("UP_RIGHT");
                    slides[1][2] = new Slide("CROSS");
                    slides[1][3] = new Slide("DOWN_LEFT");
                    slides[1][4] = new Slide("UP");
                    
                    // Ligne 2
                    slides[2][0] = new Slide("T_RIGHT");
                    slides[2][1] = new Slide("T_DOWN");
                    slides[2][2] = new Slide("CROSS");
                    slides[2][3] = new Slide("T_UP");
                    slides[2][4] = new Slide("DOWN_LEFT");
                    
                    // Ligne 3
                    slides[3][0] = new Slide("T_RIGHT");
                    slides[3][1] = new Slide("CROSS");
                    slides[3][2] = new Slide("CROSS");
                    slides[3][3] = new Slide("HORI");
                    slides[3][4] = new Slide("T_LEFT");
                    
                    // Ligne 4
                    slides[4][0] = new Slide("UP");
                    slides[4][1] = new Slide("UP_RIGHT");
                    slides[4][2] = new Slide("UP_LEFT");
                    slides[4][3] = new Slide("RIGHT");
                    slides[4][4] = new Slide("UP_LEFT");

                    // Positions initiales des 3 boules (Deux en haut dans les coins, une en bas à gauche)
                    int[][] balls = new int[][]{ {0, 3}, {3, 0}, {4, 3} };
                    
                    // Cibles associées
                    int[][] goals = new int[][]{ {2, 1}, {1, 4}, {3, 4} };

                    return new MovingBalls(new Vec2((W - 10) / 2.0, (H - 90) / 2.0 - 10), 430, balls, goals, slides, this);
                }
                default -> {
                    int N = 5;
                    Slide[][] slides = new Slide[N][N];
                    // Ligne 0
                    slides[0][0] = new Slide("DOWN_RIGHT");
                    slides[0][1] = new Slide("HORI");
                    slides[0][2] = new Slide("T_DOWN");
                    slides[0][3] = new Slide("T_DOWN");
                    slides[0][4] = new Slide("DOWN_LEFT");

                    // Ligne 1
                    slides[1][0] = new Slide("UP_RIGHT");
                    slides[1][1] = new Slide("HORI");
                    slides[1][2] = new Slide("T_LEFT");
                    slides[1][3] = new Slide("T_RIGHT");
                    slides[1][4] = new Slide("UP_LEFT");

                    // Ligne 2
                    slides[2][0] = new Slide("DOWN_RIGHT");
                    slides[2][1] = new Slide("DOWN_LEFT");
                    slides[2][2] = new Slide("VERT"); // Cible : accessible uniquement par le bas
                    slides[2][3] = new Slide("T_RIGHT");
                    slides[2][4] = new Slide("DOWN_LEFT");

                    // Ligne 3
                    slides[3][0] = new Slide("VERT");
                    slides[3][1] = new Slide("T_RIGHT");
                    slides[3][2] = new Slide("T_UP"); // Aiguillage central vers la cible
                    slides[3][3] = new Slide("T_UP");
                    slides[3][4] = new Slide("T_LEFT");

                    // Ligne 4
                    slides[4][0] = new Slide("UP_RIGHT");
                    slides[4][1] = new Slide("T_UP");
                    slides[4][2] = new Slide("HORI");
                    slides[4][3] = new Slide("HORI");
                    slides[4][4] = new Slide("UP_LEFT");

                    // Positions initiales des 3 boules
                    int[][] balls = new int[][]{ {0, 0}, {3, 0}, {0, 4} };

                    // Cibles associées
                    int[][] goals = new int[][]{ {1, 1}, {2, 2}, {4, 3} };
                    return new MovingBalls(new Vec2((W - 10) / 2.0, (H - 90) / 2.0 - 10), 430, balls, goals, slides, this);
                }
            }
        }
    }

    @Override
    protected void onMousePressed(Vec2 p) {
        if (enigme.win) return;
        enigme.update(p, true, true);
    }

    @Override
    protected void onMouseReleased(Vec2 p) {
        enigme.update(p, false, false);
    }

    @Override
    protected void paintEnigme(Graphics2D g, int w, int h) {
        enigme.draw(g);
        if (save.getDifficulty().equals("Easy")){
            g.setColor(new Color(180, 185, 195));
            g.setFont(new Font("SansSerif", Font.ITALIC, 12));
            g.drawString("Toutes les billes doivent reposer sur une case 'objectif'.",
                    20, h - 13);
        }
    }
}
