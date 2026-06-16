package com.sae.enigmas;

import java.awt.Point;
import java.awt.Rectangle;
import com.roxane.app.Translations;

public class EnigmeVerre {

    private boolean corpsExamine = false;
    private boolean[] verresTrouves = new boolean[5];
    
    // Dialogues initiaux de Louis
    private final String[] textesLouis = {
        "VERRE_LOUIS_1",
        "VERRE_LOUIS_2",
        "VERRE_LOUIS_3"
    };

    // Indices textuels associés à chaque verre rouge
    private final String[] indicesVerres = {
        "VERRE_IND_0", // Verre 0 (Plante Salon 1)
        "VERRE_IND_1",        // Verre 1 (Table basse Salon 1)
        "VERRE_IND_2",         // Verre 2 (Cuisine Salon 1)
        "VERRE_IND_3", // Verre 3 (Table Salon 2)
        "VERRE_IND_4"  // Verre 4 (Table Salon 2)
    };

    public boolean isCorpsExamine() { return corpsExamine; }
    public void setCorpsExamine(boolean examine) { this.corpsExamine = examine; }

    public String[] getTextesLouis() {
        String[] out = new String[textesLouis.length];
        for (int i = 0; i < textesLouis.length; i++) out[i] = Translations.t(textesLouis[i]);
        return out;
    }

    /** Vérifie si un clic touche un verre non trouvé selon l'univers et le décor actuel */
    public int obtenirIdVerreClique(String univers, int indexDecor, Point clic, int iw, int ih) {
        if (!corpsExamine) return -1;

        // Salon 1 (Premier décor)
        if (univers.equals("SALON") && indexDecor == 0) {
            if (new Rectangle((int)(iw * 0.09), (int)(ih * 0.55), (int)(iw * 0.02), (int)(ih * 0.04)).contains(clic) && !verresTrouves[0]) return 0;
            if (new Rectangle((int)(iw * 0.51), (int)(ih * 0.45), (int)(iw * 0.02), (int)(ih * 0.04)).contains(clic) && !verresTrouves[1]) return 1;
            if (new Rectangle((int)(iw * 0.58), (int)(ih * 0.33), (int)(iw * 0.02), (int)(ih * 0.04)).contains(clic) && !verresTrouves[2]) return 2;
        }
        
        // Salon 2 (Deuxième décor)
        if (univers.equals("SALON") && indexDecor == 1) {
            if (new Rectangle((int)(iw * 0.51), (int)(ih * 0.41), (int)(iw * 0.02), (int)(ih * 0.04)).contains(clic) && !verresTrouves[3]) return 3;
            if (new Rectangle((int)(iw * 0.49), (int)(ih * 0.46), (int)(iw * 0.02), (int)(ih * 0.04)).contains(clic) && !verresTrouves[4]) return 4;
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
            return Translations.t(indicesVerres[id]);
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