package com.sae.enigmas;

import java.awt.Graphics2D;


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

    public MovingBalls(Vec2 coord, double taille, int[][] balls, int[][] goals, Slide[][] slides){

        /* Constructeur de l'énigme */

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

        /* Cette méthode met à jour l'énigme en fonction des actions de l'utilisateur. */

        if (!win){
            if (this.updButton(mouseCoord, leftClickPushed, leftClickPressed)){
                this.updBalls();
            }
            if (verifWin()){
                win = true;
            }
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

    public void move(int i_b, int j_b){

        /* Cette méthode déplace les boules selon le bouton qui a été pressé. */

        // Attention ici i_b et j_b sont les coordonnées du bouton cliqué, ainsi la colonne 0 de balls est la colonne 1 de bouton
        if (i_b == 0){
            // Vers la gauche
            int j = j_b-1;
            for (int i = 1; i < taille; i++){
                Slider b = balls[j][i];
                int decal = 0;
                while (i-decal > 0 && slides[j][i-decal].canGoTo(Slide.LEFT) && balls[j][i-decal-1] == null){
                    decal += 1;
                }
                if (decal != 0){
                    balls[j][i-decal] = b;
                    balls[j][i] = null;
                }
            }
        }
        if (i_b == taille+1){
            // Vers la droite
            int j = j_b-1;
            for (int i2 = 0; i2 < taille-1; i2++){
                int i = taille-2-i2;
                Slider b = balls[j][i];
                int decal = 0;
                while (i+decal < taille-1 && slides[j][i+decal].canGoTo(Slide.RIGHT) && balls[j][i+decal+1] == null){
                    decal += 1;
                }
                if (decal != 0){
                    balls[j][i+decal] = b;
                    balls[j][i] = null;
                }
            }
        }
        if (j_b == 0){
            // Vers le haut
            int i = i_b-1;
            for (int j = 1; j < taille; j++){
                Slider b = balls[j][i];
                int decal = 0;
                while (j-decal > 0 && slides[j-decal][i].canGoTo(Slide.UP) && balls[j-decal-1][i] == null){
                    decal += 1;
                }
                if (decal != 0){
                    balls[j-decal][i] = b;
                    balls[j][i] = null;
                }
            }
        }
        if (j_b == taille+1){
            // Vers le bas
            int i = i_b-1;
            for (int j2 = 0; j2 < taille-1; j2++){
                int j = taille-2-j2;
                Slider b = balls[j][i];
                int decal = 0;
                while (j+decal < taille-1 && slides[j+decal][i].canGoTo(Slide.DOWN) && balls[j+decal+1][i] == null){
                    decal += 1;
                }
                if (decal != 0){
                    balls[j+decal][i] = b;
                    balls[j][i] = null;
                }
            }
        }
    }

    public final void updBalls(){

        /* Cette méthode met à jour les coordonnées des boules. */

        for (int j = 0; j < balls.length; j++){
            for (int i = 0; i < balls[j].length; i++){
                Vec2 c = new Vec2(coord.x-taille_tot/2 + (i+1/2) * taille_case, coord.y-taille_tot/2 + (j+1/2) * taille_case);
                balls[j][i].setCoord(c);
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
                    Vec2 c = new Vec2(coord.x-(taille_tot+2*taille_case)/2 + (i+1/2) * taille_case, coord.y-(taille_tot + 2*taille_case)/2 + (j+1/2) * taille_case);
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
                if (j == this.slides.length && this.slides[j][i].canGoTo(Slide.DOWN)){
                    this.slides[j][i].directions.remove(Slide.DOWN);
                    this.slides[j][i].provenances.remove(Slide.UP);
                }
                if (i == this.slides[0].length && this.slides[j][i].canGoTo(Slide.RIGHT)){
                    this.slides[j][i].directions.remove(Slide.RIGHT);
                    this.slides[j][i].provenances.remove(Slide.LEFT);
                }
                Vec2 c = new Vec2(this.coord.x-this.taille_tot/2 + (i+1/2) * this.taille_case, this.coord.y-this.taille_tot/2 + (j+1/2) * this.taille_case);
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
                if (s != null) s.draw(g);
            }
        }
        // Boules
        for (Slider[] line : balls){
            for (Slider b : line){
                if (b != null) b.drawBall(g);
            }
        }
        // Boutons
        for (MovingBallsButton[] line : buttons){
            for (MovingBallsButton btn : line){
                if (btn != null) btn.draw(g);
            }
        }
    }
}