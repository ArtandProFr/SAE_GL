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

    public void update(Vec2 mouseCoord, boolean leftClick, boolean leftRelease) {
        /* Version fidèle à la maquette Python sans bug de décalage */
        if (!this.win) {
            // 1. DÉTECTION DU CLIC (SÉLECTION)
            if (this.selection == null && leftClick) {
                for (int j = 0; j < this.taille; j++) {
                    for (int i = 0; i < this.taille; i++) {
                        Slider s = this.sliders[j][i];
                        if (s != null && mouseCoord.distanceTo(s.coord) < (this.taille_case * LIGHT_RADIUS)) {
                            this.selection = new int[]{i, j}; // {colonne, ligne}
                        }
                    }
                }
            }

            // 2. GESTION DU MOUVEMENT CONTINU (DRAG)
            if (this.selection != null && this.sliders[this.selection[1]][this.selection[0]] != null) {
                int i = this.selection[0];
                int j = this.selection[1];
                Slider sl = this.sliders[j][i];
                Slide slide = this.slides[j][i];

                // Position centrale de la case actuelle (0.5 au lieu de 1/2)
                double orig_x = this.coord.x - this.taille_tot / 2 + (i + 0.5) * this.taille_case;
                double orig_y = this.coord.y - this.taille_tot / 2 + (j + 0.5) * this.taille_case;

                double diff_x = mouseCoord.x - orig_x;
                double diff_y = mouseCoord.y - orig_y;

                double new_x = orig_x;
                double new_y = orig_y;

                // Priorité à l'axe où la souris tire le plus
                if (Math.abs(diff_x) > Math.abs(diff_y)) {
                    if ((diff_x > 0 && slide.canGoTo(Slide.RIGHT) && i < this.taille - 1 && this.sliders[j][i + 1] == null) ||
                        (diff_x < 0 && slide.canGoTo(Slide.LEFT) && i > 0 && this.sliders[j][i - 1] == null)) {
                        new_x = mouseCoord.x;
                    }
                } else {
                    if ((diff_y > 0 && slide.canGoTo(Slide.DOWN) && j < this.taille - 1 && this.sliders[j + 1][i] == null) ||
                        (diff_y < 0 && slide.canGoTo(Slide.UP) && j > 0 && this.sliders[j - 1][i] == null)) {
                        new_y = mouseCoord.y;
                    }
                }

                // Vérification de la contrainte de taille de case
                Vec2 currentCenter = new Vec2(orig_x, orig_y);
                Vec2 newPos = new Vec2(new_x, new_y);

                if (newPos.distanceTo(currentCenter) < this.taille_case) {
                    sl.setCoord(newPos);

                    // 3. CHANGEMENT DE CASE DYNAMIQUE
                    int[][] voisins = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}}; // Haut, Bas, Gauche, Droite
                    for (int[] v : voisins) {
                        int ni = i + v[1];
                        int nj = j + v[0];

                        if (ni >= 0 && ni < this.taille && nj >= 0 && nj < this.taille) {
                            if (this.sliders[nj][ni] == null) {
                                // Remplacement de 1/2 par 0.5
                                double dest_x = this.coord.x - this.taille_tot / 2 + (ni + 0.5) * this.taille_case;
                                double dest_y = this.coord.y - this.taille_tot / 2 + (nj + 0.5) * this.taille_case;
                                Vec2 destCenter = new Vec2(dest_x, dest_y);

                                // Si l'ampoule dépasse la moitié de la case suivante
                                if (sl.coord.distanceTo(destCenter) < (this.taille_case / 2.0)) {
                                    this.sliders[nj][ni] = sl;
                                    this.sliders[j][i] = null;
                                    this.selection = new int[]{ni, nj};
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            // 4. RELÂCHEMENT : On réaimante
            if (this.selection != null && leftRelease) {
                this.slidersToGoodCoord(); 
                this.selection = null;
            }

            // Mise à jour de l'état d'allumage des lampes
            updAllSliders();
            this.win = verifWin();
            if (this.win) {
                this.slidersToGoodCoord();
            }
        }
    }

    public void drag(Vec2 mouseCoord) {
        /* L'ampoule suit visuellement le curseur pendant qu'on glisse */
        if (this.win || selection == null) return;

        int i_sel = selection[0];
        int j_sel = selection[1];

        // On met à jour les coordonnées graphiques de l'ampoule au pixel près de la souris
        if (sliders[j_sel][i_sel] != null) {
            sliders[j_sel][i_sel].setCoord(new Vec2(mouseCoord.x, mouseCoord.y));
        }
    }

    public void release(Vec2 mouseCoord) {
        /* Quand on lâche la souris, on valide le mouvement si la case est valide et adjacente */
        if (this.win || selection == null) return;

        int i_sel = selection[0]; // Case de départ X
        int j_sel = selection[1]; // Case de départ Y

        // Case d'arrivée visée par la souris
        int i_mouse = (int) ((mouseCoord.x - (this.coord.x - this.taille_tot / 2)) / this.taille_case);
        int j_mouse = (int) ((mouseCoord.y - (this.coord.y - this.taille_tot / 2)) / this.taille_case);

        boolean validMove = false;

        if (i_mouse >= 0 && i_mouse < taille && j_mouse >= 0 && j_mouse < taille && sliders[j_mouse][i_mouse] == null) {
            // Vérification de l'adjacence et des rails
            if (i_mouse == i_sel + 1 && j_mouse == j_sel && slides[j_sel][i_sel].canGoTo(Slide.RIGHT) && slides[j_mouse][i_mouse].canComeFrom(Slide.RIGHT)) {
                validMove = true;
            } else if (i_mouse == i_sel - 1 && j_mouse == j_sel && slides[j_sel][i_sel].canGoTo(Slide.LEFT) && slides[j_mouse][i_mouse].canComeFrom(Slide.LEFT)) {
                validMove = true;
            } else if (j_mouse == j_sel + 1 && i_mouse == i_sel && slides[j_sel][i_sel].canGoTo(Slide.DOWN) && slides[j_mouse][i_mouse].canComeFrom(Slide.DOWN)) {
                validMove = true;
            } else if (j_mouse == j_sel - 1 && i_mouse == i_sel && slides[j_sel][i_sel].canGoTo(Slide.UP) && slides[j_mouse][i_mouse].canComeFrom(Slide.UP)) {
                validMove = true;
            }
        }

        if (validMove) {
            // On déplace l'ampoule vers sa nouvelle case dans la matrice
            sliders[j_mouse][i_mouse] = sliders[j_sel][i_sel];
            sliders[j_sel][i_sel] = null;
            
            // On la recentre parfaitement sur sa nouvelle case
            Vec2 centreCase = new Vec2(this.coord.x - this.taille_tot / 2 + (i_mouse + 0.5) * this.taille_case,
                                       this.coord.y - this.taille_tot / 2 + (j_mouse + 0.5) * this.taille_case);
            sliders[j_mouse][i_mouse].setCoord(centreCase);
        } else {
            // Mouvement invalide ou lâché hors zone : on la renvoie à son centre d'origine
            Vec2 centreOrigine = new Vec2(this.coord.x - this.taille_tot / 2 + (i_sel + 0.5) * this.taille_case,
                                          this.coord.y - this.taille_tot / 2 + (j_sel + 0.5) * this.taille_case);
            if (sliders[j_sel][i_sel] != null) {
                sliders[j_sel][i_sel].setCoord(centreOrigine);
            }
        }

        // Nettoyage de la sélection
        this.selection = null;
        updAllSliders();
        this.win = verifWin();
    }

    private void slidersToGoodCoord(){
        /* Cette méthode réaimante les Sliders à leurs emplacements dans la grille. */
        for (int j = 0; j < this.sliders.length; j++){
            for (int i = 0; i < this.sliders[j].length; i++){
                Slider s = this.sliders[j][i];
                if (s != null){
                    // Utilisation de 0.5 au lieu de 1/2
                    Vec2 c = new Vec2(this.coord.x-this.taille_tot/2 + (i+0.5) * this.taille_case, this.coord.y-this.taille_tot/2 + (j+0.5) * this.taille_case);
                    s.setCoord(c);
                }
            }
        }
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
                if (j == this.slides.length - 1 && this.slides[j][i].canGoTo(Slide.DOWN)){ // Correction de l'index au passage (length - 1)
                    this.slides[j][i].directions.remove(Slide.DOWN);
                    this.slides[j][i].provenances.remove(Slide.UP);
                }
                if (i == this.slides[0].length - 1 && this.slides[j][i].canGoTo(Slide.RIGHT)){ // Correction de l'index au passage (length - 1)
                    this.slides[j][i].directions.remove(Slide.RIGHT);
                    this.slides[j][i].provenances.remove(Slide.LEFT);
                }
                // Utilisation de 0.5 au lieu de 1/2
                Vec2 c = new Vec2(this.coord.x-this.taille_tot/2 + (i+0.5) * this.taille_case, this.coord.y-this.taille_tot/2 + (j+0.5) * this.taille_case);
                this.slides[j][i].setDrawingInfo(c, this.taille_case);
            }
        }
    }

    private void initSliders(Object[][] sliders){
        /* Cette méthode initialise les Sliders avec un tableau */
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
            // Utilisation de 0.5 au lieu de 1/2
            Vec2 c = new Vec2(this.coord.x-this.taille_tot/2 + (i+0.5) * this.taille_case, this.coord.y-this.taille_tot/2 + (j+0.5) * this.taille_case);
            this.sliders[j][i].setDrawingInfo(c, this.taille_case * LIGHT_RADIUS);
        }
    }

    private void updSlider(int i, int j) {

        /* Cette méthode met à jour un slider */

        Slider s = this.sliders[j][i];
        if (s != null) {
            s.state = true;
            String col = s.color;
            // On parcourt les 8 voisins
            for (int y = Math.max(0, j - 1); y <= Math.min(this.taille - 1, j + 1); y++) {
                for (int x = Math.max(0, i - 1); x <= Math.min(this.taille - 1, i + 1); x++) {
                    // On ignore uniquement la case courante
                    if (x == i && y == j) continue;
                    
                    Slider st = this.sliders[y][x];
                    // Si on trouve une ampoule d'une autre couleur, elle s'éteint
                    if (st != null && !st.color.equals(col)) {
                        s.state = false;
                        return; // Sortie immédiate, pas besoin de vérifier les autres
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
                if (s != null) s.draw(g, "LIGHTS");
            }
        }
        for (Slider[] line : sliders){
            for (Slider sl : line){
                if (sl != null) sl.drawLight(g);
            }
        }
    }
}
