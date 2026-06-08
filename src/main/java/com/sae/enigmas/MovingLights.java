package com.sae.enigmas;

import java.awt.Graphics2D;

public class MovingLights {
    public static final double LIGHT_RADIUS = 1.0/3.0;
    public int taille;
    public Vec2 coord;
    public double taille_tot;
    public double taille_case;
    public boolean win;
    public int[] selection;
    public Slider[][] sliders;
    public Slide[][] slides;
    private final Slider[][] debut;

    public MovingLights(Vec2 coord, double taille, Object[][] sliders, Slide[][] slides){

        /* Constructeur de l'énigme */

        this.taille = slides.length;
        this.coord = coord;
        this.taille_tot = taille;
        this.taille_case = this.taille_tot/this.taille;
        this.initSliders(sliders);
        this.initSlides(slides);
        this.debut = getCopy(this.sliders);
        this.win = false;
        this.selection = null;
        updAllSliders();
    }

    public void update(Vec2 mouseCoord, boolean leftClick, boolean leftRelease){

        /* Cette méthode actualise l'énigme en fonction des actions du joueur. */

        if (!this.win){
            if (selection == null && leftClick){
                for (int j = 0; j < sliders.length; j++){
                    for (int i = 0; i < sliders[j].length; i++){
                        Slider s = sliders[j][i];
                        if (s != null && Vec2.dist(mouseCoord, s.coord) < s.rayon){
                            selection = new int[]{i, j};
                        }
                    }
                }
            }
            if (selection != null && this.sliders[selection[1]][selection[0]] != null){
                int i = selection[0];
                int j = selection[1];
                Slider sl = sliders[j][i];
                Slide slide = slides[j][i];

                // Position centrale de la case actuelle
                double orig_x = coord.x - taille_tot/2 + (i + 0.5) * taille_case;
                double orig_y = coord.y - taille_tot/2 + (j + 0.5) * taille_case;
                
                double diff_x = mouseCoord.x - orig_x;
                double diff_y = mouseCoord.y - orig_y;
                
                // Contrainte de mouvement selon les directions du rail
                double new_x = orig_x;
                double new_y = orig_y;

                // Priorité à l'axe où la souris tire le plus
                if (Math.abs(diff_x) > Math.abs(diff_y)){
                    if ((diff_x > 0 && slide.canGoTo(Slide.RIGHT) && i < taille - 1 && sliders[j][i+1] == null) || (diff_x < 0 && slide.canGoTo(Slide.LEFT) && i > 0 && sliders[j][i-1] == null)){
                        new_x = mouseCoord.x;
                    }
                } else {
                    if ((diff_y > 0 && slide.canGoTo(Slide.DOWN) && j < taille - 1 && sliders[j+1][i] == null) || (diff_y < 0 && slide.canGoTo(Slide.UP) && j > 0 && sliders[j-1][i] == null)){
                        new_y = mouseCoord.y;
                    }
                }
                if (Vec2.dist(new Vec2(new_x, new_y), new Vec2(orig_x, orig_y)) < taille_case){
                    sl.setCoord(new Vec2(new_x, new_y));
                    // 3. CHANGEMENT DE CASE (Saut vers une case adjacente)
                    int[][] lp = {new int[]{-1, 0}, new int[]{1, 0}, new int[]{0, -1}, new int[]{0, 1}};
                    for (int[] possib : lp){ // Haut, Bas, Gauche, Droite
                        int dj = possib[0];
                        int di = possib[1];
                        
                        int ni = i + di;
                        int nj = j + dj;

                        if (0 <= ni && ni < taille && 0 <= nj  && nj < taille){
                            if (sliders[nj][ni] == null){ // Case vide
                                double dest_x = coord.x - taille_tot/2 + (ni + 0.5) * taille_case;
                                double dest_y = coord.y - taille_tot/2 + (nj + 0.5) * taille_case;
                                
                                // Si le slider dépasse la moitié de la case suivante
                                if (Vec2.dist(sl.coord, new Vec2(dest_x, dest_y)) < (taille_case / 2)){
                                    sliders[nj][ni] = sl;
                                    sliders[j][i] = null;
                                    selection = new int[]{ni, nj};
                                    break;
                                }
                            }
                        }
                        
                    }
                }
            }
            if (selection != null && leftRelease && sliders[selection[1]][selection[0]] != null){
                int i = selection[0];
                int j = selection[1];
                double x = coord.x-taille_tot/2 + (i+1/2) * taille_case;
                double y = coord.y-taille_tot/2 + (j+1/2) * taille_case;
                sliders[j][i].setCoord(new Vec2(x, y));
                selection = null;
            }
            updAllSliders();
            if (verifWin()){
                win = true;
                slidersToGoodCoord();
            }
        }
    }

    private void slidersToGoodCoord(){

        /* Cette méthode réaimante les Sliders à leurs emplacements dans la grille. */

        for (int j = 0; j < this.sliders.length; j++){
            for (int i = 0; i < this.sliders[j].length; i++){
                Slider s = this.sliders[j][i];
                if (s != null){
                    Vec2 c = new Vec2(this.coord.x-this.taille_tot/2 + (i+1/2) * this.taille_case, this.coord.y-this.taille_tot/2 + (j+1/2) * this.taille_case);
                    s.setCoord(c);
                }
            }
        }
    }

    private void updSlider(int i, int j){

        /* Cette méthode met à jour le Slider situé en position i, j */

        Slider s = this.sliders[j][i];
        if (s != null){
            s.state = true;
            String col = s.color;
            Outer: for (int y = Math.max(0, j-1); y < Math.min(this.sliders.length, j+2); y++){
                for (int x = Math.max(0, i-1); x < Math.min(this.sliders[j].length-1, i+2); x++){
                    Slider st = this.sliders[y][x];
                    if (x != i && y != j && st != null && !st.color.equals(col)){
                        s.state = false;
                        break Outer;
                    }
                }
            }
        }
    }

    private void updAllSliders(){

        /* Cette méthode met à jour tous les sliders. */

        for (int j = 0; j < this.sliders.length; j++){
            for (int i = 0; i < this.sliders[j].length; i++){
                updSlider(i, j);
            }
        }
    }

    public void reset(){

        /* Cette méthode reset l'énigme */

        this.win = false;
        this.selection = null;
        this.sliders = getCopy(this.debut);
        updAllSliders();
    }

    private boolean verifWin(){

        /* Cette méthode renvoie un booléen selon si l'énigme est résolue ou non. */

        for (Slider[] line : this.sliders){
            for (Slider s : line){
                if (s != null && !s.state) return false;
            }
        }
        return true;
    }

    private void initSlides(Slide[][] slides){

        /* Cette méthode initialise les Slides avec un tableau de modèles. */

        this.slides = new Slide[slides.length][slides[0].length];
        for (int j = 0; j < this.slides.length; j++){
            for (int i = 0; i < this.slides.length; i++){
                this.slides[j][i] = new Slide(slides[j][i]);
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

    private void initSliders(Object[][] sliders){

        /* Cette méthode initialise les Sliders avec un tableau de la forme {{int, int, Slider}, {int, int, Slider}} */

        this.sliders = new Slider[this.taille][this.taille];
        for (Slider[] line : this.sliders){
            for (int i = 0; i < line.length; i++){
                line[i] = null;
            }
        }
        for (Object[] o : sliders){
            int i = (int) o[0];
            int j = (int) o[1];
            this.sliders[j][i] = new Slider((Slider) o[2]);
            Vec2 c = new Vec2(this.coord.x-this.taille_tot/2 + (i+1/2) * this.taille_case, this.coord.y-this.taille_tot/2 + (j+1/2) * this.taille_case);
            this.sliders[j][i].setDrawingInfo(c, this.taille_case * LIGHT_RADIUS);
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
        for (Slide[] line : slides){
            for (Slide s : line){
                if (s != null) s.draw(g);
            }
        }
        for (Slider[] line : sliders){
            for (Slider sl : line){
                if (sl != null) sl.drawLight(g);
            }
        }
    }
}
