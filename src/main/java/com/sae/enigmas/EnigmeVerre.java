package com.sae.enigmas;

import java.awt.Point;
import java.awt.Rectangle;

public class EnigmeVerre {

    private boolean corpsExamine = false;
    private boolean[] verresTrouves = new boolean[5];
    
    // Dialogues initiaux d'Arthur
    private final String[] textesArthur = {
        "Arthur... ? Oh non, il ne respire plus. Son corps est déjà froid...",
        "Regarde ses lèvres... elles ont une étrange teinte bleutée. Un empoisonnement ? C'est impensable...",
        "Je n'ai pas le choix. Je dois fouiller l'appartement et trouver les indices qui mèneront au coupable."
    };

    // Indices textuels associés à chaque verre rouge
    private final String[] indicesVerres = {
        "Une trace de rouge à lèvres grasse... Ce verre appartient à Jacques.", // Verre 0 (Plante Salon 1)
        "Ce verre sent fortement le soda tiède. C'est celui de Louis.",        // Verre 1 (Table basse Salon 1)
        "Ce verre de jus est intact et propre. Paul n'y a pas touché.",         // Verre 2 (Cuisine Salon 1)
        "De la poudre blanche s'est déposée au fond... C'est le verre empoisonné d'Arthur !", // Verre 3 (Table Salon 2)
        "Des empreintes digitales très nettes entourent ce verre... Ce sont celles de Pierre."  // Verre 4 (Table Salon 2)
    };

    public boolean isCorpsExamine() { return corpsExamine; }
    public void setCorpsExamine(boolean examine) { this.corpsExamine = examine; }

    public String[] getTextesArthur() { return textesArthur; }

    /** * Vérifie si un clic touche un verre non trouvé selon l'univers et le décor actuel.
     * Retourne l'ID du verre (0 à 4) ou -1 si aucun verre n'est touché.
     */
    public int obtenirIdVerreClique(String univers, int indexDecor, Point clic, int iw, int ih) {
        if (!corpsExamine) return -1;

        // Salon 1 (Premier décor)
        if (univers.equals("SALON") && indexDecor == 0) {
            if (new Rectangle((int)(iw * 0.09), (int)(ih * 0.52), (int)(iw * 0.02), (int)(ih * 0.04)).contains(clic) && !verresTrouves[0]) return 0;
            if (new Rectangle((int)(iw * 0.51), (int)(ih * 0.45), (int)(iw * 0.02), (int)(ih * 0.04)).contains(clic) && !verresTrouves[1]) return 1;
            if (new Rectangle((int)(iw * 0.58), (int)(ih * 0.33), (int)(iw * 0.02), (int)(ih * 0.04)).contains(clic) && !verresTrouves[2]) return 2;
        }
        
        // Salon 2 (Deuxième décor)
        if (univers.equals("SALON") && indexDecor == 1) {
            if (new Rectangle((int)(iw * 0.49), (int)(ih * 0.41), (int)(iw * 0.02), (int)(ih * 0.04)).contains(clic) && !verresTrouves[3]) return 3;
            if (new Rectangle((int)(iw * 0.48), (int)(ih * 0.46), (int)(iw * 0.02), (int)(ih * 0.04)).contains(clic) && !verresTrouves[4]) return 4;
        }
        return -1;
    }

    /** Vérifie si la souris survole un verre interactif */
    public boolean survolentUnVerre(String univers, int indexDecor, Point p, int iw, int ih) {
        return obtenirIdVerreClique(univers, indexDecor, p, iw, ih) != -1;
    }

    /** Marque un verre comme trouvé et retourne son indice textuel */
    public String inspecterVerre(int id) {
        if (id >= 0 && id < verresTrouves.length) {
            verresTrouves[id] = true;
            return indicesVerres[id];
        }
        return "";
    }

    public boolean isVerreTrouve(int id) {
        if (id >= 0 && id < verresTrouves.length) return verresTrouves[id];
        return false;
    }

    public int compterVerresTrouves() {
        int total = 0;
        for (boolean b : verresTrouves) if (b) total++;
        return total;
    }

    public boolean tousVerresTrouves() {
        return compterVerresTrouves() == 5;
    }
}