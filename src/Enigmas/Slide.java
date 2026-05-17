import java.util.HashSet;

public class Slide{
    public static final String UP = "UP";
    public static final String DOWN = "DOWN";
    public static final String LEFT = "LEFT";
    public static final String RIGHT = "RIGHT";

    private static final HashSet<String> HLEFT = getHashSet(LEFT);
    private static final HashSet<String> HRIGHT = getHashSet(RIGHT);
    private static final HashSet<String> HDOWN = getHashSet(DOWN);
    private static final HashSet<String> HUP = getHashSet(UP);
    private static final HashSet<String> HVERT = combineHashSet(HUP, HDOWN);
    private static final HashSet<String> HHORI = combineHashSet(HLEFT, HRIGHT);
    private static final HashSet<String> HCROSS = combineHashSet(HVERT, HHORI);
    private static final HashSet<String> HUPRIGHT = combineHashSet(HUP, HRIGHT);
    private static final HashSet<String> HUPLEFT = combineHashSet(HUP, HLEFT);
    private static final HashSet<String> HDOWNRIGHT = combineHashSet(HDOWN, HRIGHT);
    private static final HashSet<String> HDOWNLEFT = combineHashSet(HDOWN, HLEFT);
    private static final HashSet<String> HTRIGHT = combineHashSet(HVERT, HRIGHT);
    private static final HashSet<String> HTLEFT = combineHashSet(HVERT, HLEFT);
    private static final HashSet<String> HTDOWN = combineHashSet(HHORI, HDOWN);
    private static final HashSet<String> HTUP = combineHashSet(HHORI, HUP);

    public double[] coord;
    public double taille;
    public boolean isGoal;
    public HashSet<String> directions;
    public HashSet<String> provenances;

    private static HashSet<String> getHashSet(String s){

        /* Cette méthode renvoie un hashset composé d'une chaine de caractère. */

        HashSet<String> hs = new HashSet<>();
        hs.add(s);
        return hs;
    }
    private static HashSet<String> combineHashSet(HashSet<String> h1, HashSet<String> h2){

        /* Cette méthode renvoie la combinaison de 2 hashsets sans modifier ceux de départ. */

        HashSet<String> nv = new HashSet<>(h1);
        nv.addAll(h2);
        return nv;
    }

    public Slide(Slide s){

        /* Constructeur par recopie. */

        this.coord = s.coord;
        this.taille = s.taille;
        this.isGoal = s.isGoal;
        this.directions = s.directions;
        this.provenances = s.provenances;
    }

    public Slide(String type){

        /* Constructeur par type. */

        initWithType(type);
    }

    public Slide(HashSet<String> directions){

        /* Constructeur par directions. */

        initWithDirections(directions);
    }

    public Slide(String type, boolean isGoal){

        /* Constructeur par type, connaissant isGoal */

        initIsGoal(isGoal);
        initWithType(type);
    }

    public Slide(HashSet<String> directions, boolean isGoal){

        /* Constructeur par directions, connaisant isGoal */

        initIsGoal(isGoal);
        initWithDirections(directions);
    }

    private void initWithDirections(HashSet<String> directions){

        /* Initialisation des directions. (puis des provenances associées) */

        this.directions = directions;
        initProvenances();
    }

    private void initWithType(String type){

        /* Initialisation des directions en fonction du type. */

        this.directions = switch(type){
            case UP -> HUP;
            case DOWN -> HDOWN;
            case LEFT -> HLEFT;
            case RIGHT -> HRIGHT;
            case UP+"_"+LEFT -> HUPLEFT;
            case UP+"_"+RIGHT -> HUPRIGHT;
            case DOWN+"_"+LEFT -> HDOWNLEFT;
            case DOWN+"_"+RIGHT -> HDOWNRIGHT;
            case "T_"+RIGHT -> HTRIGHT;
            case "T_"+LEFT -> HTLEFT;
            case "T_"+DOWN -> HTDOWN;
            case "T_"+UP -> HTUP;
            case "CROSS" -> HCROSS;
            case "VERT" -> HVERT;
            case "HORI" -> HHORI;
            default -> new HashSet<>();
        };
        initWithDirections(directions);
    }

    private void initIsGoal(boolean isGoal){

        /* Initialisation de isGoal. */

        this.isGoal = isGoal;
    }

    public void setDrawingInfo(double[] coord, double taille){

        /* Initialisation des variables nécessaire à l'affichage. */

        this.coord = coord;
        this.taille = taille;
    }

    public boolean canGoTo(String dir){

        /* Cette méthode renvoie si la direction est non-bloquante. */

        return directions.contains(dir);
    }

    public boolean canComeFrom(String dir){

        /* Cette méthode renvoie si la provenance est non-bloquante. */

        return provenances.contains(dir);
    }

    private void initProvenances(){

        /* Cette méthode initialise les provenances. */

        this.provenances = new HashSet<>();
        if (canGoTo(UP)){
            this.provenances.add(DOWN);
        }
        if (canGoTo(DOWN)){
            this.provenances.add(UP);
        }
        if (canGoTo(LEFT)){
            this.provenances.add(RIGHT);
        }
        if (canGoTo(RIGHT)){
            this.provenances.add(LEFT);
        }
    }
}