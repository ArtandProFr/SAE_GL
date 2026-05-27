package com.sae.game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

/**
 * Jeu (anciennement "jeu") - Travail de Thomas (branche Thomas).
 *
 * Adapté à la structure Maven : les images sont désormais chargées via le
 * classpath depuis {@code src/main/resources/images/Thomas/}.
 */
public class Jeu extends JFrame {

    /** Dossier (classpath) où sont rangées les images de cette branche. */
    private static final String IMG_DIR = "images/Thomas/";

    private String[] decorsPierre = {"pierre1.jpg", "pierre2.jpg"};
    private String[] decorsLouis = {"louis1.jpg", "louis2.jpg"};
    private String[] decorsActuels = decorsPierre;

    private int indexDecor = 0;
    private BackgroundPanel backgroundPanel;
    private JLabel txtExplicatif;

    private boolean possedeObjet = false;
    private boolean inventaireSelectionne = false;
    private int sceneActuelleObjet = 0;
    private boolean estDansGroupeLouis = false;
    private boolean objetEstChezLouis = false;

    // Ratios de position du point rouge (0.50 = 50% de la largeur, 0.50 = 50% de la hauteur)
    private double objetRatioX = 0.50;
    private double objetRatioY = 0.50;
    private final int TAILLE_OBJET = 20; // Diamètre du point rouge en pixels

    public Jeu() {
        setTitle("Mon Application SAE");
        setSize(800, 600);
        // DISPOSE_ON_CLOSE plutôt que EXIT_ON_CLOSE : si la JFrame est utilisée
        // seule (lancement standalone via main), elle se ferme sans tuer toute
        // l'application. Quand on embarque seulement le JPanel dans un SwingNode
        // JavaFX, la JFrame n'est de toute façon jamais affichée.
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        backgroundPanel = new BackgroundPanel("image_accueil.jpg");
        backgroundPanel.setLayout(null);

        JButton btnMenu = new JButton("MENU");
        JButton btnSauvegarde = new JButton("SAUVEGARDE");

        backgroundPanel.add(btnMenu);
        backgroundPanel.add(btnSauvegarde);

        backgroundPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!backgroundPanel.contenantBoutonFleche()) return;

                int w = backgroundPanel.getWidth();
                int h = backgroundPanel.getHeight();

                // Zones dynamiques des portes
                Rectangle zonePorteDynamique = new Rectangle((int)(w * 0.2), (int)(h * 0.35), (int)(w * 0.12), (int)(h * 0.33));
                Rectangle zonePorteLouis = new Rectangle((int)(w * 0.20), (int)(h * 0.33), (int)(w * 0.14), (int)(h * 0.36));

                // Zone dynamique actuelle du point rouge basé sur ses ratios
                int objX = (int) (w * objetRatioX) - (TAILLE_OBJET / 2);
                int objY = (int) (h * objetRatioY) - (TAILLE_OBJET / 2);
                Rectangle zonePointRougeDynamique = new Rectangle(objX, objY, TAILLE_OBJET, TAILLE_OBJET);

                int invX = w - 210;
                int invY = h - 80;
                Rectangle case1Inventaire = new Rectangle(invX, invY, 55, 55);

                if (possedeObjet && case1Inventaire.contains(e.getPoint())) {
                    inventaireSelectionne = !inventaireSelectionne;
                    backgroundPanel.repaint();
                    return;
                }

                if (!estDansGroupeLouis && indexDecor == 1 && zonePorteDynamique.contains(e.getPoint())) {
                    estDansGroupeLouis = true;
                    decorsActuels = decorsLouis;
                    indexDecor = 0;
                    backgroundPanel.setNewImage(decorsActuels[indexDecor]);
                    mettreAJourTexteChambre();
                    backgroundPanel.repaint();
                    return;
                }

                if (estDansGroupeLouis && indexDecor == 1 && zonePorteLouis.contains(e.getPoint())) {
                    estDansGroupeLouis = false;
                    decorsActuels = decorsPierre;
                    indexDecor = 0;
                    backgroundPanel.setNewImage(decorsActuels[indexDecor]);
                    mettreAJourTexteChambre();
                    backgroundPanel.repaint();
                    return;
                }

                // RAMASSER : Utilise la zone dynamique calculée en temps réel
                if (!possedeObjet && estDansGroupeLouis == objetEstChezLouis && indexDecor == sceneActuelleObjet && zonePointRougeDynamique.contains(e.getPoint())) {
                    possedeObjet = true;
                    sceneActuelleObjet = -1;
                    backgroundPanel.repaint();
                    return;
                }

                // POSER : Convertit le clic brut de la souris en nouveaux ratios dynamiques
                if (possedeObjet && inventaireSelectionne) {
                    objetRatioX = (double) e.getX() / w;
                    objetRatioY = (double) e.getY() / h;
                    possedeObjet = false;
                    inventaireSelectionne = false;
                    sceneActuelleObjet = indexDecor;
                    objetEstChezLouis = estDansGroupeLouis;
                    backgroundPanel.repaint();
                }
            }
        });

        backgroundPanel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (!backgroundPanel.contenantBoutonFleche()) return;

                int w = backgroundPanel.getWidth();
                int h = backgroundPanel.getHeight();

                Rectangle zonePorteDynamique = new Rectangle((int)(w * 0.2), (int)(h * 0.35), (int)(w * 0.12), (int)(h * 0.33));
                Rectangle zonePorteLouis = new Rectangle((int)(w * 0.20), (int)(h * 0.33), (int)(w * 0.14), (int)(h * 0.36));

                int objX = (int) (w * objetRatioX) - (TAILLE_OBJET / 2);
                int objY = (int) (h * objetRatioY) - (TAILLE_OBJET / 2);
                Rectangle zonePointRougeDynamique = new Rectangle(objX, objY, TAILLE_OBJET, TAILLE_OBJET);

                boolean surPorte = !estDansGroupeLouis && indexDecor == 1 && zonePorteDynamique.contains(e.getPoint());
                boolean surPorteLouis = estDansGroupeLouis && indexDecor == 1 && zonePorteLouis.contains(e.getPoint());
                boolean surObjet = !possedeObjet && indexDecor == sceneActuelleObjet && estDansGroupeLouis == objetEstChezLouis && zonePointRougeDynamique.contains(e.getPoint());

                if (surPorte || surPorteLouis || surObjet) {
                    backgroundPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                } else {
                    backgroundPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });

        btnMenu.addActionListener(e -> {
            backgroundPanel.setNewImage("image_menu.jpg");
            backgroundPanel.removeAll();

            JButton btnJouer = new JButton("JOUER");
            JButton btnQuitter = new JButton("QUITTER");
            btnQuitter.addActionListener(ev -> System.exit(0));

            btnJouer.addActionListener(ev -> {
                backgroundPanel.removeAll();
                backgroundPanel.setNewImage(decorsActuels[indexDecor]);

                txtExplicatif = new JLabel();
                txtExplicatif.setFont(new Font("Arial", Font.BOLD, 18));
                txtExplicatif.setForeground(Color.WHITE);
                mettreAJourTexteChambre();

                JButton btnGauche = new JButton("<");
                btnGauche.setFont(new Font("Arial", Font.BOLD, 20));
                btnGauche.addActionListener(event -> {
                    indexDecor = (indexDecor - 1 + decorsActuels.length) % decorsActuels.length;
                    backgroundPanel.setNewImage(decorsActuels[indexDecor]);
                    mettreAJourTexteChambre();
                });

                JButton btnDroite = new JButton(">");
                btnDroite.setFont(new Font("Arial", Font.BOLD, 20));
                btnDroite.addActionListener(event -> {
                    indexDecor = (indexDecor + 1) % decorsActuels.length;
                    backgroundPanel.setNewImage(decorsActuels[indexDecor]);
                    mettreAJourTexteChambre();
                });

                backgroundPanel.add(txtExplicatif);
                backgroundPanel.add(btnGauche);
                backgroundPanel.add(btnDroite);

                backgroundPanel.revalidate();
                backgroundPanel.repaint();
            });

            backgroundPanel.add(btnJouer);
            backgroundPanel.add(btnQuitter);
            backgroundPanel.revalidate();
            backgroundPanel.repaint();
        });

        setContentPane(backgroundPanel);
    }

    /**
     * Retourne le panneau de jeu pour permettre son intégration dans un
     * conteneur Swing externe — typiquement un {@code javafx.embed.swing.SwingNode}
     * afin d'afficher le jeu Swing à l'intérieur d'une scène JavaFX, sans
     * avoir à ouvrir une seconde fenêtre.
     */
    public JPanel getGamePanel() {
        return backgroundPanel;
    }

    private void mettreAJourTexteChambre() {
        if (txtExplicatif != null) {
            if (estDansGroupeLouis) {
                txtExplicatif.setText("Chambre de Louis");
            } else {
                txtExplicatif.setText("Chambre de Pierre");
            }
        }
    }

    class BackgroundPanel extends JPanel {
        private Image backgroundImage;

        public BackgroundPanel(String path) {
            setNewImage(path);
        }

        public void setNewImage(String path) {
            URL url = getClass().getClassLoader().getResource(IMG_DIR + path);
            if (url != null) {
                this.backgroundImage = new ImageIcon(url).getImage();
            } else {
                // Fallback : tentative de chargement direct (compat. ancienne structure)
                this.backgroundImage = new ImageIcon(path).getImage();
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }

            if (contenantBoutonFleche()) {
                // AFFICHAGE DYNAMIQUE : On recalcule sa position X et Y en pixels selon la taille de la fenêtre
                if (!possedeObjet && indexDecor == sceneActuelleObjet && estDansGroupeLouis == objetEstChezLouis) {
                    int objX = (int) (getWidth() * objetRatioX) - (TAILLE_OBJET / 2);
                    int objY = (int) (getHeight() * objetRatioY) - (TAILLE_OBJET / 2);
                    g2d.setColor(Color.RED);
                    g2d.fillOval(objX, objY, TAILLE_OBJET, TAILLE_OBJET);
                }

                int invX = getWidth() - 210;
                int invY = getHeight() - 80;

                for (int i = 0; i < 3; i++) {
                    g2d.setColor(new Color(255, 255, 255, 180));
                    g2d.fillRect(invX + (i * 65), invY, 55, 55);
                    g2d.setColor(Color.DARK_GRAY);
                    g2d.drawRect(invX + (i * 65), invY, 55, 55);
                }

                if (inventaireSelectionne) {
                    g2d.setColor(Color.YELLOW);
                    g2d.setStroke(new BasicStroke(3));
                    g2d.drawRect(invX, invY, 55, 55);
                    g2d.setStroke(new BasicStroke(1));
                }

                if (possedeObjet) {
                    g2d.setColor(Color.RED);
                    g2d.fillOval(invX + 17, invY + 17, 20, 20);
                }
            }

            repositionnerComposants();
        }

        public boolean contenantBoutonFleche() {
            for (Component c : getComponents()) {
                if (c instanceof JButton && ((JButton) c).getText().equals("<")) {
                    return true;
                }
            }
            return false;
        }

        private void repositionnerComposants() {
            Component[] composants = getComponents();
            int w = getWidth();
            int h = getHeight();

            for (Component c : composants) {
                if (c instanceof JButton) {
                    JButton btn = (JButton) c;
                    String texte = btn.getText();

                    if (texte.equals("MENU")) btn.setBounds((int)(w * 0.25), (int)(h * 0.75), 150, 50);
                    else if (texte.equals("SAUVEGARDE")) btn.setBounds((int)(w * 0.55), (int)(h * 0.75), 150, 50);
                    else if (texte.equals("JOUER")) btn.setBounds((int)(w * 0.37), (int)(h * 0.35), 200, 60);
                    else if (texte.equals("QUITTER")) btn.setBounds((int)(w * 0.37), (int)(h * 0.55), 200, 60);
                    else if (texte.equals("<")) btn.setBounds(30, (int)(h * 0.45), 60, 60);
                    else if (texte.equals(">")) btn.setBounds(w - 90, (int)(h * 0.45), 60, 60);
                }
                else if (c instanceof JLabel) {
                    c.setBounds(30, 20, 300, 30);
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Jeu().setVisible(true));
    }
}
