package com.sae.core;

public class Phase {

    private String numero;       
    private String description;  
    private int pourcentage;     

    public Phase(String numero, String description, int pourcentage) {
        this.numero = numero;
        this.description = description;
        this.pourcentage = pourcentage;
    }

    public boolean estJeuFini() {
        return this.numero.equals("8.1");
    }

    public String getNumero() { return numero; }
    public String getDescription() { return description; }
    public int getPourcentage() { return pourcentage; }

    public static final Phase[] TOUTES_LES_PHASES = {
        new Phase("0.1", "Réveil sans Louis.", 0),
        new Phase("1.1", "Un empoisonnement ?", 8),
        new Phase("2.1", "Pierre, que peut-il bien cacher ?", 14),
        new Phase("3.1", "Trouver comment entrer dans la chambre de Louis.", 24),
        new Phase("3.2", "Ordinateur de Louis.", 30),
        new Phase("3.2.bis", "Coupure de courant.", 34), 
        new Phase("3.3", "1/2 : Remettre le courant.", 44),
        new Phase("3.4", "2/2 : Remettre le courant.", 56),
        new Phase("3.5", "Discussion.", 58),
        new Phase("3.6", "Un peu de chimie.", 64),
        new Phase("4.1", "Téléphone.", 76),
        new Phase("4.2", "Répondeur.", 76), 
        new Phase("4.3", "Enquête dans la salle de bain.", 84),
        new Phase("5.1", "Paul rentre à l’appartement.", 84),
        new Phase("5.2", "Chambre de Jacques.", 100),
        new Phase("6.1", "Révélations.", 100),
        new Phase("7.1", "Crédits.", 100),
        new Phase("8.1", "Fin (Stockage best score)", 100)
    };
}