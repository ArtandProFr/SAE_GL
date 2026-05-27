package com.sae.enigmas;

public class MovingBallsButton {
    public Vec2 coord;
    public double taille;
    public String sens;
    public boolean pressed; 
    public boolean clicked; 

    public MovingBallsButton(Vec2 coord, double taille, String sens){

        /* Constructeur */

        this.coord = coord;
        this.taille = taille;
        this.sens = sens; // UP, DOWN, LEFT, RIGHT
        this.pressed = false;
        this.clicked = false;
    }

    public void update(Vec2 mouseCoord, boolean leftClickPushed, boolean leftClickPressed){

        /* Cette méthode met à jour le bouton en fonction des actions du joueur. */

        pressed = false;
        clicked = false;
        if (leftClickPressed){
            if (Math.abs(mouseCoord.x - coord.x) < taille/2 && Math.abs(mouseCoord.y - coord.y) < taille/2){
                pressed = true;
                if (leftClickPushed){
                    clicked = true;
                }
            }
        }
    }
}
