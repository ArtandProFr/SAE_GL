package com.sae.enigmas;

import java.awt.*;

public class ChambrePierreManager {

    private boolean aLaCle = false;
    private boolean armoireOuverte = false;
    private String modeZoom = "AUCUN"; // "AUCUN", "TIROIR", "ARMOIRE"

    // Les images de la chambre de Pierre
    private String decorPierre1 = "pierre1.jpg";
    private String decorPierre2 = "pierre2c.jpg"; 

    /** Détermine si le joueur clique sur un élément interactif de la chambre */
    public boolean gererClic(int indexDecor, Point clic, int iw, int ih, com.sae.game.Jeu jeu) {
        // Si on est en mode Zoom, on gère d'abord le bouton "Quitter" ou les objets du zoom
        if (!modeZoom.equals("AUCUN")) {
            return false; 
        }

        // --- VUE 1 : Vue sur la commode (pierre1.jpg) ---
        if (indexDecor == 0) {
            // Zone de la commode entourée en rouge
            Rectangle zoneCommode = new Rectangle((int)(iw * 0.58), (int)(ih * 0.47), (int)(iw * 0.16), (int)(ih * 0.38));
            if (zoneCommode.contains(clic)) {
                modeZoom = "TIROIR";
                // Réalisme : on affiche le tiroir plein ou vide selon l'état de l'inventaire
                if (!aLaCle) {
                    jeu.activerModeZoom("pierre_tiroirc.jpg");
                } else {
                    jeu.activerModeZoom("pierre_tiroir.jpg");
                }
                return true;
            }
        }

        // --- VUE 2 : Vue sur l'armoire à pharmacie ---
        if (indexDecor == 1) {
            // Zone de l'armoire à pharmacie (commune avec ou sans cadenas)
            Rectangle zoneArmoire = new Rectangle((int)(iw * 0.44), (int)(ih * 0.32), (int)(iw * 0.15), (int)(ih * 0.31));
            
            if (zoneArmoire.contains(clic)) {
                if (armoireOuverte) {
                    modeZoom = "ARMOIRE";
                    jeu.activerModeZoom("pierre_armoire.jpg");
                } else {
                    if (aLaCle) {
                        armoireOuverte = true;
                        modeZoom = "ARMOIRE";
                        decorPierre2 = "pierre2.jpg"; // On change définitivement l'image de fond (sans cadenas)
                        jeu.activerModeZoom("pierre_armoire.jpg");
                    } else {
                        javax.swing.JOptionPane.showMessageDialog(jeu, 
                            "L'armoire à pharmacie est verrouillée par un cadenas.\nIl me faut une clé pour l'ouvrir.", 
                            "Armoire verrouillée", javax.swing.JOptionPane.WARNING_MESSAGE);
                    }
                }
                return true;
            }
        }
        return false;
    }

    /** Gère les clics spécifiques à l'intérieur des images zooms */
    public void gererClicZoom(Point clic, int iw, int ih, com.sae.game.Jeu jeu) {
        // Logique du tiroir : on ne peut interagir avec la clé que si on ne l'a pas encore ramassée
        if (modeZoom.equals("TIROIR") && !aLaCle) {
            // Zone de la clé jaune dans le tiroir ouvert
            Rectangle zoneCle = new Rectangle((int)(iw * 0.43), (int)(ih * 0.48), (int)(iw * 0.12), (int)(ih * 0.15));
            if (zoneCle.contains(clic)) {
                aLaCle = true;
                javax.swing.JOptionPane.showMessageDialog(jeu, 
                    "Vous avez récupéré la clé de l'armoire à pharmacie !", 
                    "Objet trouvé", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                
                // Réalisme : on change instantanément l'image pour afficher le tiroir désormais VIDE
                jeu.activerModeZoom("pierre_tiroir.jpg");
            }
        } else if (modeZoom.equals("ARMOIRE")) {
            // Zone de la fiole de poison (médicaments) dans l'armoire
            Rectangle zoneFiole = new Rectangle((int)(iw * 0.46), (int)(ih * 0.35), (int)(iw * 0.08), (int)(ih * 0.22));
            if (zoneFiole.contains(clic)) {
                javax.swing.JOptionPane.showMessageDialog(jeu, 
                    "Une fiole de poison numérique... Serait-ce l'arme du crime ?\nPierre cache bien son jeu.", 
                    "Indice Suspect", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    /** Vérifie si la souris survole un élément interactif dans la chambre de Pierre */
    public boolean verifierSurvol(int indexDecor, Point p, int iw, int ih) {
        if (modeZoom.equals("TIROIR") && !aLaCle) {
            Rectangle zoneCle = new Rectangle((int)(iw * 0.43), (int)(ih * 0.48), (int)(iw * 0.12), (int)(ih * 0.15));
            return zoneCle.contains(p);
        }
        if (modeZoom.equals("ARMOIRE")) {
            Rectangle zoneFiole = new Rectangle((int)(iw * 0.46), (int)(ih * 0.35), (int)(iw * 0.08), (int)(ih * 0.22));
            return zoneFiole.contains(p);
        }
        if (modeZoom.equals("AUCUN")) {
            if (indexDecor == 0) {
                Rectangle zoneCommode = new Rectangle((int)(iw * 0.58), (int)(ih * 0.47), (int)(iw * 0.16), (int)(ih * 0.38));
                return zoneCommode.contains(p);
            }
            if (indexDecor == 1) {
                Rectangle zoneArmoire = new Rectangle((int)(iw * 0.44), (int)(ih * 0.32), (int)(iw * 0.15), (int)(ih * 0.31));
                return zoneArmoire.contains(p);
            }
        }
        return false;
    }

    public void quitterZoom() {
        this.modeZoom = "AUCUN";
    }

    public String[] obtenirDecorsPierre() {
        return new String[]{decorPierre1, decorPierre2};
    }

    public String getModeZoom() { return modeZoom; }
    public boolean isaLaCle() { return aLaCle; }
}