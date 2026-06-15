package com.sae.enigmas;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

public class MovingBalls{
    public static final double BALL_RADIUS = 1.0/3.0;
    public int taille;
    public Vec2 coord;
    public double taille_tot;
    public double taille_case;
    public boolean win;
    public Slide[][] slides;
    public Slider[][] balls;
    public int[][] goals;
    public int nb_balls;
    public MovingBallsButton[][] buttons;
    private final Slider[][] debut;

    public boolean isAnimating = false;
    private double animationProgress = 0; // 0.0 à 1.0
    private final double ANIM_SPEED = 0.05; // Ajuste pour la vitesse
    private final List<MovingBallTrack> activeAnimations = new ArrayList<>();
    private final MovingBallsUI parent;

    public MovingBalls(Vec2 coord, double taille, int[][] balls, int[][] goals, Slide[][] slides, MovingBallsUI parent){

        /* Constructeur de l'énigme */

        this.parent = parent;
        this.taille = slides.length;
        this.coord = coord;
        this.taille_tot = taille;
        this.taille_case = this.taille_tot/this.taille;
        this.nb_balls = balls.length;
        this.initBalls(balls, this.taille);
        this.goals = goals;
        this.initSlides(slides);
        this.debut = getCopy(this.balls);
        updBalls();
        this.win = false;
        this.initButtons();
    }

    public void update(Vec2 mouseCoord, boolean leftClickPushed, boolean leftClickPressed){

        if (!win && !isAnimating) { 
            activeAnimations.clear(); // On s'assure que la liste est propre
            
            if (this.updButton(mouseCoord, leftClickPushed, leftClickPressed)){
                // Si un bouton a été cliqué, move() a été appelée et a rempli activeAnimations
                if (!activeAnimations.isEmpty()) {
                    isAnimating = true;
                    animationProgress = 0;
                }
            }
        }
        
        if (isAnimating) {
            animate(); 
        }
        
        if (!isAnimating && verifWin()){
            win = true;
            parent.changeStatus("Tiroir déverrouillé !", new Color(46, 204, 113));
            parent.markSolvedAndClose();
        }
    }

    private void animate() {
        animationProgress += ANIM_SPEED;

        // On anime chaque bille enregistrée de façon totalement sécurisée
        for (MovingBallTrack track : activeAnimations) {
            double startX = track.startPos.x;
            double startY = track.startPos.y;
            double endX = track.endPos.x;
            double endY = track.endPos.y;

            // Calcul de l'interpolation linéaire (LERP)
            double currentX = startX + (endX - startX) * animationProgress;
            double currentY = startY + (endY - startY) * animationProgress;

            track.ball.setCoord(new Vec2(currentX, currentY));
        }

        if (animationProgress >= 1.0) {
            isAnimating = false;
            animationProgress = 0;
            activeAnimations.clear(); // On vide les animations terminées
            updBalls(); // On recale parfaitement toutes les billes sur leurs cases finales
        }
    }

    private boolean verifWin(){

        /* Cette méthode vérifie si l'énigme est résolue ou non. */

        int count = 0;
        for (int j = 0 ; j < balls.length; j++){
            for (int i = 0; i < balls[j].length; i++){
                if (balls[j][i] != null && slides[j][i].isGoal){
                    count += 1;
                }
            }
        }
        return count == nb_balls;
    }

    public boolean updButton(Vec2 mouseCoord, boolean leftClickPushed, boolean leftClickPressed){

        /* Cette méthode met à jour les boutons pour trouver celui qui est cliqué s'il y en a un et effectuer l'action associée. */

        for(int j = 0; j < taille+2; j++){
            for (int i = 0; i < taille+2; i++){
                if (buttons[j][i] != null){
                    buttons[j][i].update(mouseCoord, leftClickPushed, leftClickPressed);
                    if (buttons[j][i].clicked){
                        move(i, j);
                        return true;
                    }
                }
            }
        } 
        return false;
    }

    public Vec2 goodCoord(int i, int j){

        /* Cette méthode renvoie les coordonnées du centre d'une case de la grille. */

        return new Vec2(this.coord.x - this.taille_tot/2 + (i + 0.5) * this.taille_case, 
                        this.coord.y - this.taille_tot/2 + (j + 0.5) * this.taille_case);
    }

    public void move(int i_b, int j_b){
        /* 1. On prend une photo de la grille avant tout déplacement */
        Slider[][] grilleDepart = getCopy(balls);
        activeAnimations.clear();

        // Vers la gauche
        if (i_b == 0){
            int j = j_b-1;
            // De gauche à droite
            for (int i = 0; i < taille; i++){
                Slider b = grilleDepart[j][i]; // On lit depuis la photo de départ
                if (b == null) continue;
                
                int decal = 0;
                while (i-decal > 0 && slides[j][i-decal].canGoTo(Slide.LEFT) && balls[j][i-decal-1] == null){
                    decal += 1;
                }
                if (decal != 0){
                    // On récupère le VRAI slider de la grille active
                    Slider vraieBille = balls[j][i];
                    if (vraieBille != null) {
                        activeAnimations.add(new MovingBallTrack(vraieBille, goodCoord(i, j), goodCoord(i - decal, j)));
                        balls[j][i-decal] = vraieBille;
                        balls[j][i] = null;
                    }
                }
            }
        }
        
        // Vers la droite
        if (i_b == taille+1){
            int j = j_b-1;
            // De droite à gauche
            for (int i = taille - 1; i >= 0; i--){
                Slider b = grilleDepart[j][i]; // On lit depuis la photo de départ
                if (b == null) continue;
                
                int decal = 0;
                while (i+decal < taille-1 && slides[j][i+decal].canGoTo(Slide.RIGHT) && balls[j][i+decal+1] == null){
                    decal += 1;
                }
                if (decal != 0){
                    Slider vraieBille = balls[j][i];
                    if (vraieBille != null) {
                        activeAnimations.add(new MovingBallTrack(vraieBille, goodCoord(i, j), goodCoord(i + decal, j)));
                        balls[j][i+decal] = vraieBille;
                        balls[j][i] = null;
                    }
                }
            }
        }
        
        // Vers le haut
        if (j_b == 0){
            int i = i_b-1;
            // De haut en bas
            for (int j = 0; j < taille; j++){
                Slider b = grilleDepart[j][i]; // On lit depuis la photo de départ
                if (b == null) continue;
                
                int decal = 0;
                while (j-decal > 0 && slides[j-decal][i].canGoTo(Slide.UP) && balls[j-decal-1][i] == null){
                    decal += 1;
                }
                if (decal != 0){
                    Slider vraieBille = balls[j][i];
                    if (vraieBille != null) {
                        activeAnimations.add(new MovingBallTrack(vraieBille, goodCoord(i, j), goodCoord(i, j - decal)));
                        balls[j-decal][i] = vraieBille;
                        balls[j][i] = null;
                    }
                }
            }
        }
        
        // Vers le bas
        if (j_b == taille+1){
            int i = i_b-1;
            // De bas en haut
            for (int j = taille - 1; j >= 0; j--){
                Slider b = grilleDepart[j][i]; // On lit depuis la photo de départ
                if (b == null) continue;
                
                int decal = 0;
                while (j+decal < taille-1 && slides[j+decal][i].canGoTo(Slide.DOWN) && balls[j+decal+1][i] == null){
                    decal += 1;
                }
                if (decal != 0){
                    Slider vraieBille = balls[j][i];
                    if (vraieBille != null) {
                        activeAnimations.add(new MovingBallTrack(vraieBille, goodCoord(i, j), goodCoord(i, j + decal)));
                        balls[j+decal][i] = vraieBille;
                        balls[j][i] = null;
                    }
                }
            }
        }
    }

    public void updBalls(){
        /* Cette méthode met à jour les coordonnées d'affichage des billes. */
        for (int j = 0; j < this.balls.length; j++){
            for (int i = 0; i < this.balls[0].length; i++){
                // AJOUT : On ne met à jour la bille que si elle existe sur cette case !
                if (this.balls[j][i] != null) {
                    double rayon = this.balls[j][i].rayon;
                    this.balls[j][i].setDrawingInfo(goodCoord(i, j), this.taille_case * BALL_RADIUS);
                }
            }
        }
    }

    public void reset(){

        /* Cette méthode réinitialise l'énigme. */

        win = false;
        balls = getCopy(debut);
        this.update(new Vec2(0, 0), false, false);
    }

    private void initButtons(){

        /* Cette méthode initialise les boutons. */

        buttons = new MovingBallsButton[taille+2][taille+2];
        for (int j = 0; j < buttons.length; j++){
            for (int i = 0; i < buttons[j].length; i++){
                String sens = null;
                if (i == 0 && (j != 0) && (j != taille+1)){
                    sens = Slide.LEFT;
                }
                if (j == 0 && (i != 0) && (i != taille+1)){
                    sens = Slide.UP;
                }
                if (i == taille+1 && (j != 0) && (j != taille+1)){
                    sens = Slide.RIGHT;
                }
                if (j == taille+1 && (i != 0) && (i != taille+1)){
                    sens = Slide.DOWN;
                }
                if (sens != null){
                    Vec2 c = new Vec2(coord.x-(taille_tot+2*taille_case)/2 + (i+0.5) * taille_case, coord.y-(taille_tot + 2*taille_case)/2 + (j+0.5) * taille_case);
                    buttons[j][i] = new MovingBallsButton(c, taille_case, sens);
                }
            }
        }
    }

    private void initBalls(int[][] balls, int taille){

        /* Cette méthode initialise le rayon des boules. */

        this.balls = new Slider[taille][taille];
        for (int[] line : balls){
            this.balls[line[1]][line[0]] = new Slider();
            this.balls[line[1]][line[0]].rayon = taille_case*BALL_RADIUS;
        }
    }

    private void initSlides(Slide[][] slides){

        /* Cette méthode initialise les Slides avec un tableau de modèles. */

        this.slides = new Slide[slides.length][slides[0].length];
        for (int j = 0; j < this.slides.length; j++){
            for (int i = 0; i < this.slides.length; i++){
                this.slides[j][i] = new Slide(slides[j][i]);
                this.slides[j][i].isGoal = false;
                for (int[] l : goals){
                    if (l[0] == i && l[1] == j){
                        this.slides[j][i].isGoal = true;
                    }
                }
                if (j == 0 && this.slides[j][i].canGoTo(Slide.UP)){
                    this.slides[j][i].directions.remove(Slide.UP);
                    this.slides[j][i].provenances.remove(Slide.DOWN);
                }
                if (i == 0 && this.slides[j][i].canGoTo(Slide.LEFT)){
                    this.slides[j][i].directions.remove(Slide.LEFT);
                    this.slides[j][i].provenances.remove(Slide.RIGHT);
                }
                if (j == this.slides.length-1 && this.slides[j][i].canGoTo(Slide.DOWN)){
                    this.slides[j][i].directions.remove(Slide.DOWN);
                    this.slides[j][i].provenances.remove(Slide.UP);
                }
                if (i == this.slides[0].length-1 && this.slides[j][i].canGoTo(Slide.RIGHT)){
                    this.slides[j][i].directions.remove(Slide.RIGHT);
                    this.slides[j][i].provenances.remove(Slide.LEFT);
                }
                Vec2 c = new Vec2(this.coord.x-this.taille_tot/2 + (i+0.5) * this.taille_case, this.coord.y-this.taille_tot/2 + (j+0.5) * this.taille_case);
                this.slides[j][i].setDrawingInfo(c, this.taille_case);
            }
        }
    }
    private Slider[][] getCopy(Slider[][] sliders){

        /* Cette méthode renvoie la copie du tableau de Slider. */

        Slider[][] nv = new Slider[sliders.length][sliders[0].length];
        for (int j = 0; j < nv.length; j++){
            for (int i = 0; i < nv[0].length; i++){
                nv[j][i] = (sliders[j][i] == null)?null:new Slider(sliders[j][i]);
            }
        }
        return nv;
    }

    /* ====== AFFICHAGE ====== */

    public void draw(Graphics2D g){
        Draw.setupQuality(g);
        // Grille
        for (Slide[] line : slides){
            for (Slide s : line){
                if (s != null) s.draw(g, "BALLS");
            }
        }

        // Goals
        for (Slide[] line : slides){
            for (Slide s : line){
                if (s != null) s.drawGoal(g);
            }
        }

        // Billes
        for (Slider[] line : balls){
            for (Slider b : line){
                // AJOUT EN SÉCURITÉ : Ne dessine la bille que si elle existe
                if (b != null) b.drawBall(g);
            }
        }

        // Boutons
        for (MovingBallsButton[] line : buttons){
            for (MovingBallsButton b : line){
                if (b != null) b.draw(g);
            }
        }
    }

    private static class MovingBallTrack {
        Slider ball;
        Vec2 startPos;
        Vec2 endPos;

        public MovingBallTrack(Slider ball, Vec2 startPos, Vec2 endPos) {
            this.ball = ball;
            this.startPos = startPos;
            this.endPos = endPos;
        }
    }
}