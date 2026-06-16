package com.sae.core;

public class Phase {

    private static Phase[] TOUTES_LES_PHASES = new Phase[0];
    private static int POIDS_TOTAL = 0;
    private double numero;       
    private String description;  
    private int avancee;
    private int poids;
    private boolean isLastStep;

    static {
        // Phase 0
        addPhase("PHASE_DESC_0", 0, true);

        // Phase 1
        addPhase("PHASE_DESC_1", 4, true);

        // Phase 2
        addPhase("PHASE_DESC_2", 3, true);

        // Phase 3
        addPhase("PHASE_DESC_31", 5);
        addPhase("PHASE_DESC_32", 3);
        addPhase("PHASE_DESC_33", 2);
        addPhase("PHASE_DESC_34", 5);
        addPhase("PHASE_DESC_35", 6);
        addPhase("PHASE_DESC_36", 1);
        addPhase("PHASE_DESC_37", 3, true);

        // Phase 4
        addPhase("PHASE_DESC_41", 6);
        addPhase("PHASE_DESC_42", 0);
        addPhase("PHASE_DESC_43", 4, true);

        // Phase 5
        addPhase("PHASE_DESC_51", 0);
        addPhase("PHASE_DESC_52", 8, true);

        // Phase 6
        addPhase("PHASE_DESC_61", 0, true);

        // Phase 7
        addPhase("PHASE_DESC_71", 0, true);
    }

    public Phase(double numero){
        Phase p = searchPhase(numero);
        if (p != null) change(p);
    }
    public Phase(double numero, String description, int avancee, int poids) {
        this.numero = numero;
        this.description = description;
        this.avancee = avancee;
        this.poids = poids;
        this.isLastStep = false;
    }
    public Phase(double numero, String description, int avancee, int poids, boolean isLastStep) {
        this.numero = numero;
        this.description = description;
        this.avancee = avancee;
        this.poids = poids;
        this.isLastStep = isLastStep;
    }
    public Phase(Phase p){
        this.numero = p.numero;
        this.description = p.description;
        this.avancee = p.avancee;
        this.poids = p.poids;
        this.isLastStep = p.isLastStep;
    }
    public void change(Phase p){
        this.numero = p.numero;
        this.description = p.description;
        this.avancee = p.avancee;
        this.poids = p.poids;
        this.isLastStep = p.isLastStep;
    }

    public void showPhase(){
        System.out.println("-----------------------------------");
        System.out.println("Phase " + numero + " :");
        System.out.println("Description : " + description);
        System.out.println("Avancée : " + avancee);
        System.out.println("Pourcentage : " + getPourcentage() + "%");
        System.out.println("Poids : " + poids);
        System.out.println("Dernière étape : " + isLastStep);
        System.out.println("Fin du jeu : " + estJeuFini());
    }

    private Phase searchPhase(double num){
        int i = 0;
        for (Phase p : TOUTES_LES_PHASES){
            if (p.numero == num) return TOUTES_LES_PHASES[i];
            i++;
        }
        return null;
    }

    private static boolean alreadyExists(String description){
        for (Phase p : TOUTES_LES_PHASES){
            if (p.description.equals(description)){
                return true;
            }
        }
        return false;
    }

    public boolean estJeuFini() {
        return this.numero >= TOUTES_LES_PHASES[TOUTES_LES_PHASES.length-1].numero;
    }

    public double getNumero() { return numero; }
    public String getDescription() { return description; }
    public int getAvancee() { return avancee; }
    public int getPoids() { return poids; }
    public boolean isLastStep() { return isLastStep; }
    public double getPourcentage() { return 100 * (double) getAvancee()/POIDS_TOTAL; }
    public int getIndex(){ 
        for (int i = 0; i < TOUTES_LES_PHASES.length; i++){
            if (this.isEqualTo(TOUTES_LES_PHASES[i])){
                return i;
            }
        }
        return -1;
    }
    public static int getIndex(double num){
        for (int i = 0; i < TOUTES_LES_PHASES.length; i++){
            if (num == TOUTES_LES_PHASES[i].getNumero()){
                return i;
            }
        }
        return -1;
    }

    public static Phase[] getAllPhases(){
        Phase[] ans = new Phase[TOUTES_LES_PHASES.length];
        for (int i = 0; i < TOUTES_LES_PHASES.length; i++){
            Phase p = new Phase(TOUTES_LES_PHASES[i]);
            ans[i] = p;
        }
        return ans;
    }

    public String getStrPourcentage(){
        double prc = getPourcentage();
        String str = String.valueOf(prc);
        if (prc%1 == 0){
            str = str.substring(0, str.length()-2);
        }
        str += "%";
        return str;
    }

    public void nextPhase(){
        int i = getIndex();
        if (i >= 0 && i < TOUTES_LES_PHASES.length - 1) {change(TOUTES_LES_PHASES[i+1]);}
    }

    private static void addPhase(String description, int poids){
        addPhase(description, poids, false);
    }

    private boolean isEqualTo(Phase p){ return this.numero == p.numero;}

    private static double arrondirNum(double num){
        return (int) Math.round(num*10)/10.0;
    }

    private static void addPhase(String description, int poids, boolean isLastStep){
        if (!alreadyExists(description)){
            Phase[] copie = new Phase[TOUTES_LES_PHASES.length+1];
            int i = 0;
            for (Phase p : TOUTES_LES_PHASES){
                copie[i] = p;
                i++;
            }
            int av = 0;
            double num = 0.1;
            if (TOUTES_LES_PHASES.length != 0){
                av = copie[i-1].avancee + copie[i-1].poids;
                num = copie[i-1].numero;
                if (copie[i-1].isLastStep){
                    num += 1;
                    num = (int) num + 0.1;
                } else {
                    num += 0.1; // Attention à ne pas avoir plus de 9 étapes par phases.
                }
                num = arrondirNum(num);
            }
            POIDS_TOTAL += poids;
            copie[i] = new Phase(num, description, av, poids, isLastStep);
            TOUTES_LES_PHASES = copie;
        }
    }
}