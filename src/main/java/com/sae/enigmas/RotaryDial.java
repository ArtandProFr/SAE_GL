package com.sae.enigmas;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

public class RotaryDial {

    public static final int[] EMPTY_LIST = new int[] {};
    public static final int[] EASY_LIST = new int[] {1, 2, 3, 1, 2, 1};
    public static final int[] NORMAL_LIST = new int[] {1, 3, 2, 1, 1, 2, 1, 3};
    public static final int[] HARD_LIST = new int[] {2, 4, 1, 3, 2, 1, 3, 2};

    public Vec2 coord;
    public int taille;
    public int taille_possib;
    public int[] liste;
    public boolean win = false;
    public int position = 0;
    public boolean[] pushed;
    public double angle_unite;
    public double angle_jouable;

    public double taille_bouton;
    public int rayon_ext;
    public int rayon_int;
    
    // Angles absolus fixes du cadran
    private double[] angle_cadran;

    public final boolean isValid(){
        return (liste != EMPTY_LIST && (1 < taille_possib && taille_possib < taille));
    }

    public void reset(){
        position = 0;
        pushed = new boolean[taille];
        for (int i = 0; i < taille; i++){
            pushed[i] = false;
        }
        win = false;
    }

    /**
     * Permet de valider instantanément l'énigme (Bouton de triche/validation)
     */
    public void forceWin() {
        for (int i = 0; i < taille; i++) {
            pushed[i] = true;
        }
        win = true;
    }

    public boolean isFinished(){
        if (pushed.length == 0) return false;
        for (boolean b : pushed){
            if (!b) return false;
        }
        return true;
    }

    public boolean canPlay(){
        for (int j = 0; j < taille_possib; j++){
            int idxJouable = (position + j) % taille;
            if (!pushed[idxJouable]) return true;
        }
        return false;
    }

    public boolean isStuck(){
        return !canPlay();
    }

    public int getSelectedPos(double angle) {
        for (int i = 0; i < taille; i++) {
            double a1 = angle_cadran[i];
            double a2 = angle_cadran[(i + 1) % taille];
            
            if (a2 < a1) { // Passage du cap 360° -> 0°
                if (angle >= a1 || angle < a2) return i;
            } else {
                if (angle >= a1 && angle < a2) return i;
            }
        }
        return -1;
    }

    public static RotaryDial create(String difficulty, Vec2 coord, int r_ext, double t_bouton){
        try {
            return new RotaryDial(difficulty, coord, r_ext, t_bouton);
        } catch (Exception e) {
            return null;
        }
    }

    public RotaryDial(String difficulty, Vec2 coord, int r_ext, double t_bouton) throws Exception {
        this.coord = coord;
        this.rayon_ext = r_ext;
        this.taille_bouton = t_bouton;
        this.rayon_int = (int) (rayon_ext * (1.0 - taille_bouton));

        switch(difficulty){
            case "Easy":
                taille_possib = 2;
                liste = EASY_LIST;
                break;
            case "Normal":
                taille_possib = 2;
                liste = NORMAL_LIST;
                break;
            case "Hard":
                taille_possib = 2;
                liste = HARD_LIST;
                break;
            default:
                taille_possib = 0;
                liste = EMPTY_LIST;
        }
        
        this.taille = liste.length;
        if (isValid()){
            this.angle_unite = 360.0 / taille;
            this.angle_jouable = taille_possib * angle_unite;
            
            // Initialisation des angles fixes (0° à 360° en sens horaire)
            this.angle_cadran = new double[taille];
            for (int i = 0; i < taille; i++) {
                this.angle_cadran[i] = (i * angle_unite) % 360;
            }
            
            reset();
        } else {
            throw new Exception("Initialization error : length must be strictly positive.");
        }
    }

    public void update(Vec2 mouseCoord, boolean leftClick){
        if (leftClick && !win){
            double dist = mouseCoord.distanceTo(coord);
            if (rayon_int < dist && dist < rayon_ext){
                double dx = mouseCoord.x - coord.x;
                double dy = mouseCoord.y - coord.y;
                
                // Calcul de l'angle de la souris : 0° en haut, tourne dans le sens des aiguilles d'une montre
                double angle = Math.toDegrees(Math.atan2(dx, -dy)) % 360;
                if (angle < 0) angle += 360;

                int pos = getSelectedPos(angle);
                if (pos >= 0) {
                    if (isCaseJouable(pos)) {
                        if (!pushed[pos]) {
                            pushed[pos] = true;
                            position += liste[pos];
                            position %= taille;
                            
                            if (isFinished()) {
                                win = true;
                            } else if (isStuck()) {
                                //reset(); 
                            }
                        }
                    } else {
                        reset(); // Clic sur une case sombre -> Reset
                    }
                }
            }
        }
    }

    /* ====== AFFICHAGE ====== */

    public void draw(Graphics2D g){
        Draw.setupQuality(g);

        // Fond du cadran
        Draw.circle(g, coord.x, coord.y, rayon_ext, new Color(38, 42, 50));
        Draw.circle(g, coord.x, coord.y, rayon_int, new Color(18, 20, 24));

        // =========================================================================
        // PASSE 1 : DESSIN DES FONDS ET DES CONTOURS DE BASE DE TOUTES LES CASES
        // =========================================================================
        for (int i = 0; i < taille; i++) {
            double a1 = angle_cadran[i];
            double a2 = angle_cadran[(i + 1) % taille];
            if (a2 < a1) a2 += 360;
            
            // Couleurs de fond (Marron clair cannelle par défaut, plus foncé si pressé)
            Color base = pushed[i] ? new Color(135, 100, 75) : new Color(210, 180, 145);
            
            double startAngle = 90 - a1;
            double extent = -(a2 - a1);

            // 1) Remplissage de la forme de la case
            Draw.portionCouronne(g, coord.x, coord.y, rayon_int + 4, rayon_ext - 4, startAngle, startAngle + extent, base);

            // 2) Contour sombre standard de la case (Passe 1)
            g.setStroke(new BasicStroke(3.5f));
            g.setColor(new Color(25, 22, 20)); // Grosse bordure de fond sombre

            double rad1 = Math.toRadians(90 - a1);
            double rad2 = Math.toRadians(90 - a2);

            // Tracé des 4 lignes de contour par défaut
            g.drawArc((int)(coord.x - rayon_ext + 4), (int)(coord.y - rayon_ext + 4), (int)(rayon_ext * 2 - 8), (int)(rayon_ext * 2 - 8), (int)startAngle, (int)extent);
            g.drawArc((int)(coord.x - rayon_int - 4), (int)(coord.y - rayon_int - 4), (int)(rayon_int * 2 + 8), (int)(rayon_int * 2 + 8), (int)startAngle, (int)extent);
            g.drawLine((int)(coord.x + Math.cos(rad1) * (rayon_int + 4)), (int)(coord.y - Math.sin(rad1) * (rayon_int + 4)),
                    (int)(coord.x + Math.cos(rad1) * (rayon_ext - 4)), (int)(coord.y - Math.sin(rad1) * (rayon_ext - 4)));
            g.drawLine((int)(coord.x + Math.cos(rad2) * (rayon_int + 4)), (int)(coord.y - Math.sin(rad2) * (rayon_int + 4)),
                    (int)(coord.x + Math.cos(rad2) * (rayon_ext - 4)), (int)(coord.y - Math.sin(rad2) * (rayon_ext - 4)));
        }

        // =========================================================================
        // PASSE 2 : DESSIN DES GROS CONTOURS DE LA PARTIE ROTATIVE ACTIVE (PAR-DESSUS)
        // =========================================================================
        for (int i = 0; i < taille; i++) {
            if (isCaseJouable(i)) {
                double a1 = angle_cadran[i];
                double a2 = angle_cadran[(i + 1) % taille];
                if (a2 < a1) a2 += 360;

                double startAngle = 90 - a1;
                double extent = -(a2 - a1);

                // Épaisseur de 6px avec une couleur gris métallique clair
                g.setStroke(new BasicStroke(6.0f));
                g.setColor(new Color(200, 205, 215)); // Gris acier métallique clair

                double rad1 = Math.toRadians(90 - a1);
                double rad2 = Math.toRadians(90 - a2);

                // Tracé de l'encadré rotatif épais recalé pile sur les rayons de base (4)
                // Arc extérieur
                g.drawArc((int)(coord.x - rayon_ext + 4), (int)(coord.y - rayon_ext + 4), 
                        (int)(rayon_ext * 2 - 8), (int)(rayon_ext * 2 - 8), (int)startAngle, (int)extent);

                // Arc intérieur
                g.drawArc((int)(coord.x - rayon_int - 4), (int)(coord.y - rayon_int - 4), 
                        (int)(rayon_int * 2 + 8), (int)(rayon_int * 2 + 8), (int)startAngle, (int)extent);

                // Bord latéral gauche
                g.drawLine((int)(coord.x + Math.cos(rad1) * (rayon_int + 4)), (int)(coord.y - Math.sin(rad1) * (rayon_int + 4)),
                        (int)(coord.x + Math.cos(rad1) * (rayon_ext - 4)), (int)(coord.y - Math.sin(rad1) * (rayon_ext - 4)));

                // Bord latéral droit
                g.drawLine((int)(coord.x + Math.cos(rad2) * (rayon_int + 4)), (int)(coord.y - Math.sin(rad2) * (rayon_int + 4)),
                        (int)(coord.x + Math.cos(rad2) * (rayon_ext - 4)), (int)(coord.y - Math.sin(rad2) * (rayon_ext - 4)));
            }
        }

        // =========================================================================
        // PASSE 3 : AFFICHAGE DU TEXTE AU PREMIER PLAN FINAL
        // =========================================================================
        for (int i = 0; i < taille; i++) {
            double a1 = angle_cadran[i];
            double a2 = angle_cadran[(i + 1) % taille];
            if (a2 < a1) a2 += 360;

            Color texteColor = pushed[i] ? new Color(245, 235, 220) : new Color(55, 40, 25);

            double midAngle = a1 + ((a2 - a1) / 2.0);
            double aMidRad = Math.toRadians(90 - midAngle);
            double rTxt = (rayon_int + rayon_ext) / 2.0;
            
            double tx = coord.x + Math.cos(aMidRad) * rTxt;
            double ty = coord.y - Math.sin(aMidRad) * rTxt; 
            
            g.setFont(new Font("SansSerif", Font.BOLD, Math.max(12, rayon_ext / 12)));
            Draw.textCentered(g, tx, ty, String.valueOf(liste[i]), texteColor, Math.max(12, rayon_ext / 12));
        }

        // Centre de l'interface
        Draw.circle(g, coord.x, coord.y, rayon_int * 0.55, new Color(28, 30, 36));
        Draw.circle(g, coord.x, coord.y, rayon_int * 0.55, new Color(120, 130, 145), 2);
        Draw.textCentered(g, coord.x, coord.y,
                win ? "OUVERT" : (pushed.length == 0 ? "" : countPushed() + "/" + taille),
                win ? new Color(46, 204, 113) : Color.WHITE,
                Math.max(13, rayon_ext / 14));
    }

    private boolean isCaseJouable(int i){
        for (int j = 0; j < taille_possib; j++) {
            if (((position + j) % taille) == i) {
                return true;
            }
        }
        return false;
    }

    private int countPushed(){
        int n = 0;
        for (boolean b : pushed) if (b) n++;
        return n;
    }
}