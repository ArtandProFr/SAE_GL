package com.sae.enigmas;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

public class RotaryDial {

    public static final int[] EMPTY_LIST = new int[] {};
    public static final int[] EASY_LIST = new int[] {1, 2, 3, 1, 2, 1};
    public static final int[] NORMAL_LIST = new int[] {1, 3, 2, 1, 1, 2, 1, 3};
    public static final int[] HARD_LIST = new int[] {2, 4, 1, 3, 2, 1, 3, 2}; // Liste Hard personnalisée

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

    public boolean isFinished(){
        if (pushed.length == 0) return false;
        for (boolean b : pushed){
            if (!b) return false;
        }
        return true;
    }

    public boolean canPlay(){
        for (int i = 0; i < taille_possib; i++){
            if (!pushed[(position + i) % taille]) return true;
        }
        return false;
    }

    public boolean isStuck(){
        return !canPlay();
    }

    // Renvoie les angles absolus mis à jour selon la rotation (position globale)
    public double[] getAngleCadran() {
        double[] angles = new double[taille];
        for (int i = 0; i < taille; i++) {
            angles[i] = ((position + i) * angle_unite) % 360;
        }
        return angles;
    }

    public int getSelectedPos(double angle) {
        double[] angleCadranActuel = getAngleCadran();
        for (int i = 0; i < taille; i++) {
            double a1 = angleCadranActuel[i];
            double a2 = angleCadranActuel[(i + 1) % taille];
            
            if (a2 < a1) { // Gestion du passage par 0° / 360°
                if (angle > a1 || angle < a2) return i;
            } else {
                if (a1 < angle && angle < a2) return i;
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
                taille_possib = 2; // Tu peux passer à 3 si tu veux corser le jeu
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
            this.pushed = new boolean[taille];
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
                
                // Formule mathématique calquée sur Python : 0° en haut, sens horaire
                double angle = Math.toDegrees(Math.atan2(dx, -dy)) % 360;
                if (angle < 0) angle += 360;

                double[] angleCadranActuel = getAngleCadran();
                double a1 = angleCadranActuel[0]; // Début de la zone jouable (index 0 de la rotation)
                double a2 = (a1 + angle_jouable) % 360;

                boolean angleDansZoneJouable = false;
                if (a1 < a2) {
                    angleDansZoneJouable = (angle > a1 && angle < a2);
                } else { // Passage par le point zéro
                    angleDansZoneJouable = (angle > a1 || angle < a2);
                }

                if (angleDansZoneJouable){
                    int pos = getSelectedPos(angle);
                    if (pos >= 0 && !pushed[pos]){
                        pushed[pos] = true;
                        position += liste[pos];
                        position %= taille;
                        
                        if (isFinished()){
                            win = true;
                        } else if (isStuck()) {
                            // Si le joueur est bloqué (plus de cases libres dans la zone active), reset
                            reset();
                        }
                    }
                } else {
                    // Clic en dehors de la zone active -> Reset de la progression
                    if (getSelectedPos(angle) != -1) {
                        reset();
                    }
                }
            }
        }
    }

    /* ====== AFFICHAGE ====== */

    public void draw(Graphics2D g){
        Draw.setupQuality(g);

        // Fond de la couronne
        Draw.circle(g, coord.x, coord.y, rayon_ext, new Color(38, 42, 50));
        Draw.circle(g, coord.x, coord.y, rayon_int, new Color(18, 20, 24));

        double[] angleCadranActuel = getAngleCadran();

        // Secteurs : chaque case affiche son nombre et son état
        for (int i = 0; i < taille; i++){
            double a1 = angleCadranActuel[i];
            double a2 = angleCadranActuel[(i + 1) % taille];
            if (a2 < a1) a2 += 360;
            
            boolean estJouable = isCaseJouable(i);
            Color base;
            
            if (pushed[i]) base = new Color(46, 204, 113);          // vert : validé
            else if (estJouable) base = new Color(241, 196, 15);    // jaune : actif
            else base = new Color(52, 58, 68);                      // sombre : non jouable

            // Ajustement graphique Swing : On retire 90° pour aligner le haut (0°) avec le repère Java (gauche/haut)
            Draw.portionCouronne(g, coord.x, coord.y, rayon_int + 4, rayon_ext - 4, a1 - 90, a2 - 90, base);

            // Séparateurs
            double aRad = Math.toRadians(a1 - 90);
            double xs1 = coord.x + Math.cos(aRad) * rayon_int;
            double ys1 = coord.y + Math.sin(aRad) * rayon_int;
            double xs2 = coord.x + Math.cos(aRad) * rayon_ext;
            double ys2 = coord.y + Math.sin(aRad) * rayon_ext;
            Draw.line(g, xs1, ys1, xs2, ys2, new Color(20, 22, 28), 2);

            // Texte du chiffre (valeur du saut)
            double aMid = Math.toRadians(((a1 + a2) / 2.0) - 90);
            double rTxt = (rayon_int + rayon_ext) / 2.0;
            double tx = coord.x + Math.cos(aMid) * rTxt;
            double ty = coord.y + Math.sin(aMid) * rTxt;
            
            g.setFont(new Font("SansSerif", Font.BOLD, Math.max(12, rayon_ext / 12)));
            Draw.textCentered(g, tx, ty, String.valueOf(liste[i]),
                    pushed[i] ? Color.WHITE : new Color(230, 230, 230), Math.max(12, rayon_ext / 12));
        }

        // Centre + libellé
        Draw.circle(g, coord.x, coord.y, rayon_int * 0.55, new Color(28, 30, 36));
        Draw.circle(g, coord.x, coord.y, rayon_int * 0.55, new Color(120, 130, 145), 2);
        Draw.textCentered(g, coord.x, coord.y,
                win ? "OUVERT" : (pushed.length == 0 ? "" : countPushed() + "/" + taille),
                win ? new Color(46, 204, 113) : Color.WHITE,
                Math.max(13, rayon_ext / 14));
    }

    private boolean isCaseJouable(int i){
        // Les cases jouables sont toujours les 'taille_possib' premières cases à partir de l'index 0 de la rotation
        return i < taille_possib;
    }

    private int countPushed(){
        int n = 0;
        for (boolean b : pushed) if (b) n++;
        return n;
    }
}