public class RotaryDial{

    public static final int[] EMPTY_LIST = new int[] {};
    public static final int[] EASY_LIST = new int[] {1, 2, 3, 1, 2, 1};
    public static final int[] NORMAL_LIST = new int[] {1, 3, 2, 1, 1, 2, 1, 3};
    public static final int[] HARD_LIST = new int[] {1, 3, 2, 1, 1, 2, 1, 3}; // A modifier
    
    public Vec2 coord;
    public int taille;
    public int taille_possib;
    public int[] liste;
    public boolean win = false;
    public int position = 0;
    public boolean[] pushed;
    public double[] angle_cadran;
    public double angle_unite;
    public double angle_jouable;

    public double taille_bouton;
    public int rayon_ext;
    public int rayon_int;

    public final boolean isValid(){

        /* Cette méthode renvoie si l'énigme chargée est valide ou non. */

        return (liste != EMPTY_LIST && (liste == EASY_LIST || liste == NORMAL_LIST || liste == HARD_LIST) && (1 < taille_possib && taille_possib < taille));
    }

    public void reset(){

        /* Cette méthode remet le cadran en position initiale. */
        
        position = 0;
        pushed = new boolean[taille];
        for (int i = 0; i < taille; i++){
            pushed[i] = false;
        }
        win = false;
    }

    public boolean isFinished(){

        /* Cette méthode renvoie si l'énigme est résolue ou non. */

        if (pushed.length == 0) return false;
        for (boolean b : pushed){
            if (!b) return false;
        }
        return true;
    }

    public boolean canPlay(){

        /* Cette méthode renvoie si le joueur peut jouer ou non. */

        for (int i = 0; i < taille_possib; i++){
            if (!pushed[(position+i)%taille]) return true;
        }
        return false;
    }

    public boolean isStuck(){

        /* Cette méthode renvoie un booléen selon s'il est bloqué ou non. (pas de timer, équivalent à il ne peut pas jouer.) */

        return !canPlay();
    }

    public int getSelectedPos(double angle){
        for (int i = 0; i < taille; i++){
            if (angle_cadran[i] < angle && angle < angle_cadran[(i+1)%taille]) return i;
        }
        return -1;
    }

    public static RotaryDial create(String difficulty, Vec2 coord, int r_ext, double t_bouton){

        /* Cette méthode renvoie une énigme chargée, ou null si la création a échoué. */

        try {
            return new RotaryDial(difficulty, coord, r_ext, t_bouton);
        } catch (Exception e) {
            return null;
        }
    }

    public RotaryDial(String difficulty, Vec2 coord, int r_ext, double t_bouton){

        /* Cette méthode charge (initialise) une énigme. */

        this.coord = coord;
        rayon_ext = r_ext;
        taille_bouton = t_bouton;
        rayon_int = (int) (rayon_ext * (1.0-taille_bouton));
        switch(difficulty){
            case "Easy":
                taille_possib = 2;
                liste = EASY_LIST;
            case "Normal":
                taille_possib = 2;
                liste = NORMAL_LIST;
            case "Hard":
                taille_possib = 2;
                liste = HARD_LIST;
            default:
                taille_possib = 0;
                liste = EMPTY_LIST;
        }
        taille = liste.length;
        if (isValid()){
            angle_unite = 360.0 / taille;
            angle_jouable = taille_possib * angle_unite;
            angle_cadran = new double[taille];
            for (int i = 0; i < taille; i++){
                angle_cadran[i] = (position + i) * angle_unite;
            }
        } else {
            throw new ExceptionInInitializerError("Initialization error : length must be strictly positive. Check every levels. ");
        }
    }

    public void update(Vec2 mouseCoord, boolean leftClick){

        /* Cette méthode met à jour le cadran selon les entrées utilisateurs. */

        if (leftClick && !win){
            double dist = mouseCoord.distanceTo(coord);
            if (rayon_int < dist && dist < rayon_ext){
                double dx = mouseCoord.x - coord.x;
                double dy = mouseCoord.y - coord.y;
                double angle = Math.toDegrees(Math.atan2(dx, -dy))%360;
                double a1 = angle_cadran[position];
                double a2 = angle_cadran[position] + angle_jouable;
                if (a2 <= a1){
                    double temp = a1;
                    a1 = a2;
                    a2 = temp;
                }
                if (a1 < angle && angle < a2){
                    int pos = getSelectedPos(angle);
                    if (!pushed[pos]){
                        pushed[pos] = true;
                        position += liste[pos];
                        position %= taille;
                        if (isFinished()){
                            win = true;
                        }
                    } else {
                        reset();
                    }
                } else if (getSelectedPos(angle) != -1){
                    reset();
                }
            }
        }
    }


}