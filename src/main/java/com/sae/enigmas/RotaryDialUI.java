package com.sae.enigmas;
import com.roxane.app.Translations;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Window;

import com.sae.core.Save;

/**
 * Phase 3.1 — "Cadran Rotatif" : déverrouillage de la porte de la chambre de Louis.
 * Le RotaryDial sous-jacent gère toute la logique.
 */
public class RotaryDialUI extends EnigmaDialog {

    private final RotaryDial dial;
    private final int W = 640;
    private final int H = 560;
    private Save save = null;

    public RotaryDialUI(Window parent, Save save) {
        super(parent, Translations.t("ROTARY_TITLE"), 640, 600);
        String diff = (save != null && save.getDifficulty() != null) ? save.getDifficulty() : "Normal";
        this.save = save;
        this.dial = RotaryDial.create(diff, new Vec2(W / 2.0, H / 2.0 - 30), 210, 0.55);
        setStatus(Translations.t("ROTARY_STATUS"),
                new Color(41, 128, 185));
    }

    @Override
    protected void onMousePressed(Vec2 p) {
        if (dial == null || dial.win || dial.enAnimation) return;

        // On sauvegarde l'ancienne position pour savoir d'où on part
        int anciennePos = dial.position;

        // Exécution du déplacement logique
        boolean coupValide = dial.coupValide(p);
        if (!dial.enAnimation) dial.update(p, true);

        // Si la position a changé, on lance l'animation graphique
        if (!dial.enAnimation && dial.position != anciennePos) {
            dial.enAnimation = true;
            
            double distanceAngle;
            if (coupValide) {
                // CORRECTION DU DERNIER CRAN :
                // Si dial.position est retombé à 0 (ou inférieur) à cause du bouclage de victoire,
                // le nombre de cases avancées est : (taille totale - anciennePos) + dial.position
                int nbCasesAvancees = (dial.position <= anciennePos) 
                                      ? (dial.taille - anciennePos + dial.position) 
                                      : (dial.position - anciennePos);
                                      
                distanceAngle = -(nbCasesAvancees * dial.angle_unite);
            } else {
                // CORRECTION DU PREMIER CRAN (Erreur sur la première case) :
                // Si le joueur fait une erreur dès la case 0 (anciennePos == 0) et que le cadran doit bouger,
                // ou pour tout autre reset, on force le recul de la distance logique nécessaire.
                int nbCasesReculees = (anciennePos == 0) ? dial.taille : anciennePos;
                
                distanceAngle = nbCasesReculees * dial.angle_unite;
            }

            // On initialise l'animation à l'exact opposé pour annuler la téléportation
            dial.angleAnimation = -distanceAngle;

            // La vitesse va faire progresser angleAnimation vers 0
            final double vitesse = coupValide ? -4.0 : 4.0;

            // Création du Timer d'animation
            javax.swing.Timer timer = new javax.swing.Timer(16, null);
            timer.addActionListener(e -> {
                dial.angleAnimation += vitesse;

                // Condition d'arrêt : dès qu'on a comblé le décalage (on atteint ou dépasse 0)
                if ((vitesse > 0 && dial.angleAnimation >= 0.0) || (vitesse < 0 && dial.angleAnimation <= 0.0)) {
                    dial.angleAnimation = 0.0; // Recalage parfait à destination
                    dial.enAnimation = false;  // Redonne la main au joueur
                    timer.stop();

                    // Vérification de la victoire à la fin de la rotation
                    if (dial.win) {
                        setStatus(Translations.t("ROTARY_OK"), new Color(46, 204, 113));
                        markSolvedAndClose();
                    }
                }
                canvas.repaint(); // Rafraîchissement graphique
            });
            timer.start();
        }
    }

    @Override
    protected void paintEnigme(Graphics2D g, int w, int h) {
        if (dial == null) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 18));
            g.drawString(Translations.t("ROTARY_ERROR_LOAD"), 20, 40);
            return;
        }
        dial.draw(g);
        if (save.getDifficulty().equals("Easy")){
            g.setColor(new Color(180, 185, 195));
            g.setFont(new Font("SansSerif", Font.ITALIC, 12));
            g.drawString(Translations.t("ROTARY_HINT_EASY"),
                    20, h - 12);
        }
    }

    @Override
    protected boolean isCursorInteractive(Vec2 p) {
        if (dial == null || dial.enAnimation || dial.win) return false;
        double d = p.distanceTo(dial.coord);
        return d > dial.rayon_int && d < dial.rayon_ext;
    }
}
