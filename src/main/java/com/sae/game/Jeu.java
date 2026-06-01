package com.sae.game;

import com.sae.core.Phase; // Importation de la classe Phase depuis le dossier core

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

public class Jeu extends JFrame {

    /** Dossier (classpath) où sont rangées les images. */
    private static final String IMG_DIR = "images/Thomas/";

    // Interface pour communiquer avec JavaFX
    public interface CursorChangeListener {
        void onCursorChanged(boolean surElementInteractif);
    }
    private CursorChangeListener cursorChangeListener;

    private String[] decorsPierre = {"pierre1.jpg", "pierre2.jpg"};
    private String[] decorsLouis = {"louis1.jpg", "louis2.jpg"};
    private String[] decorsJacques = {"jacques1.jpg", "jacques2.jpg"};
    private String[] decorsSalon = {"salon1.jpg", "salon2.jpg"};
    
    private String[] decorsActuels = decorsSalon; 
    private int indexDecor = 0;
    private String universActuel = "SALON"; 
    
    // Index de progression dans le tableau des phases
    private int indexPhaseActuelle = 0;
    
    private BackgroundPanel backgroundPanel;
    private JLabel txtExplicatif;
    
    private boolean possedeObjet = false;
    private boolean inventaireSelectionne = false;
    private int sceneActuelleObjet = 0; 
    private String universActuelObjet = "PIERRE"; 

    private double objetRatioX = 0.50; 
    private double objetRatioY = 0.50;
    private final int TAILLE_OBJET = 20; 

    public Jeu() {
        setTitle("Mon Escape Game SAE");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        backgroundPanel = new BackgroundPanel(decorsActuels[indexDecor]);
        backgroundPanel.setLayout(null); 

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
            recalculerCurseurImmediat();
        });

        JButton btnDroite = new JButton(">");
        btnDroite.setFont(new Font("Arial", Font.BOLD, 20));
        btnDroite.addActionListener(event -> {
            indexDecor = (indexDecor + 1) % decorsActuels.length;
            backgroundPanel.setNewImage(decorsActuels[indexDecor]);
            mettreAJourTexteChambre();
            recalculerCurseurImmediat();
        });

        backgroundPanel.add(txtExplicatif);
        backgroundPanel.add(btnGauche);
        backgroundPanel.add(btnDroite);

        backgroundPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Rectangle imgBounds = backgroundPanel.getImageBounds();
                int iw = imgBounds.width;
                int ih = imgBounds.height;
                if (!imgBounds.contains(e.getPoint())) return;

                int mouseXInImg = e.getX() - imgBounds.x;
                int mouseYInImg = e.getY() - imgBounds.y;

                Rectangle porte1 = new Rectangle((int)(iw * 0.24), (int)(ih * 0.25), (int)(iw * 0.08), (int)(ih * 0.3));
                Rectangle porte2 = new Rectangle((int)(iw * 0.39), (int)(ih * 0.21), (int)(iw * 0.05), (int)(ih * 0.25));
                Rectangle porte3 = new Rectangle((int)(iw * 0.56), (int)(ih * 0.22), (int)(iw * 0.06), (int)(ih * 0.2));

                Rectangle porteSortiePierre = new Rectangle((int)(iw * 0.22), (int)(ih * 0.23), (int)(iw * 0.15), (int)(ih * 0.47));
                Rectangle zonePorteLouis = new Rectangle((int)(iw * 0.16), (int)(ih * 0.13), (int)(iw * 0.14), (int)(ih * 0.53));
                Rectangle zonePorteJacques = new Rectangle((int)(iw * 0.74), (int)(ih * 0.26), (int)(iw * 0.13), (int)(ih * 0.43));

                int invX = backgroundPanel.getWidth() - 210;
                int invY = backgroundPanel.getHeight() - 80;
                Rectangle case1Inventaire = new Rectangle(invX, invY, 55, 55);

                if (possedeObjet && case1Inventaire.contains(e.getPoint())) {
                    inventaireSelectionne = !inventaireSelectionne;
                    backgroundPanel.repaint();
                    return;
                }

                Point clicDansImg = new Point(mouseXInImg, mouseYInImg);

                if (universActuel.equals("SALON") && indexDecor == 1) {
                    if (porte1.contains(clicDansImg)) { transitionner("LOUIS", decorsLouis); return; }
                    if (porte2.contains(clicDansImg)) { transitionner("PIERRE", decorsPierre); return; }
                    if (porte3.contains(clicDansImg)) { transitionner("JACQUES", decorsJacques); return; }
                }

                if (universActuel.equals("PIERRE") && indexDecor == 1 && porteSortiePierre.contains(clicDansImg)) {
                    transitionnerRetourSalon(); return;
                }
                if (universActuel.equals("LOUIS") && indexDecor == 1 && zonePorteLouis.contains(clicDansImg)) {
                    transitionnerRetourSalon(); return;
                }
                if (universActuel.equals("JACQUES") && indexDecor == 1 && zonePorteJacques.contains(clicDansImg)) {
                    transitionnerRetourSalon(); return;
                }

                int objX = (int) (iw * objetRatioX) - (TAILLE_OBJET / 2);
                int objY = (int) (ih * objetRatioY) - (TAILLE_OBJET / 2);
                Rectangle zonePointRougeDynamique = new Rectangle(objX, objY, TAILLE_OBJET, TAILLE_OBJET);

                if (!possedeObjet && universActuel.equals(universActuelObjet) && indexDecor == sceneActuelleObjet && zonePointRougeDynamique.contains(clicDansImg)) {
                    possedeObjet = true;
                    universActuelObjet = "INVENTAIRE";
                    sceneActuelleObjet = -1; 
                    recalculerCurseurImmediat();
                    return;
                }

                if (possedeObjet && inventaireSelectionne) {
                    objetRatioX = (double) mouseXInImg / iw;
                    objetRatioY = (double) mouseYInImg / ih;
                    possedeObjet = false;
                    inventaireSelectionne = false;
                    sceneActuelleObjet = indexDecor; 
                    universActuelObjet = universActuel; 
                    recalculerCurseurImmediat();
                }
            }
        });

        backgroundPanel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Rectangle imgBounds = backgroundPanel.getImageBounds();
                if (!imgBounds.contains(e.getPoint())) {
                    notifierChangementCurseur(false);
                    backgroundPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    return;
                }
                int mouseXInImg = e.getX() - imgBounds.x;
                int mouseYInImg = e.getY() - imgBounds.y;
                verifierEtMettreAJourCurseur(new Point(mouseXInImg, mouseYInImg));
            }
        });

        setContentPane(backgroundPanel);
    }

    /** Permet au contrôleur JavaFX d'écouter les demandes de changement de curseur */
    public void setCursorChangeListener(CursorChangeListener listener) {
        this.cursorChangeListener = listener;
    }

    // Surcharge pour Roxane
    public void setCursorChangeListener(Object... args) {}

    /**
     // Met à jour la phase de l'enquête.
     // Appelle cette méthode manuellement dans ton code (ex: lors d'une action clé) pour avancer.
     */
    private void avancerPhaseTest() {
        Phase phaseActuelle = Phase.TOUTES_LES_PHASES[indexPhaseActuelle];
        if (phaseActuelle.estJeuFini()) {
            System.out.println("[INFO] Méthode estJeuFini() = true ! Le jeu est complété.");
            return;
        }
        if (indexPhaseActuelle < Phase.TOUTES_LES_PHASES.length - 1) {
            indexPhaseActuelle++;
            System.out.println("[PROGRESSION] Passage à la Phase " + Phase.TOUTES_LES_PHASES[indexPhaseActuelle].getNumero());
        }
    }

    private void verifierEtMettreAJourCurseur(Point clicDansImg) {
        Rectangle imgBounds = backgroundPanel.getImageBounds();
        int iw = imgBounds.width;
        int ih = imgBounds.height;

        boolean surElementInteractif = false;

        if (universActuel.equals("SALON") && indexDecor == 1) {
            Rectangle porte1 = new Rectangle((int)(iw * 0.24), (int)(ih * 0.25), (int)(iw * 0.08), (int)(ih * 0.3));
            Rectangle porte2 = new Rectangle((int)(iw * 0.39), (int)(ih * 0.21), (int)(iw * 0.05), (int)(ih * 0.25));
            Rectangle porte3 = new Rectangle((int)(iw * 0.56), (int)(ih * 0.22), (int)(iw * 0.06), (int)(ih * 0.2));
            if (porte1.contains(clicDansImg) || porte2.contains(clicDansImg) || porte3.contains(clicDansImg)) surElementInteractif = true;
        }

        if (indexDecor == 1) {
            if (universActuel.equals("PIERRE") && (new Rectangle((int)(iw * 0.22), (int)(ih * 0.23), (int)(iw * 0.15), (int)(ih * 0.47))).contains(clicDansImg)) surElementInteractif = true;
            if (universActuel.equals("LOUIS") && (new Rectangle((int)(iw * 0.16), (int)(ih * 0.13), (int)(iw * 0.14), (int)(ih * 0.53))).contains(clicDansImg)) surElementInteractif = true;
            if (universActuel.equals("JACQUES") && (new Rectangle((int)(iw * 0.74), (int)(ih * 0.26), (int)(iw * 0.13), (int)(ih * 0.43))).contains(clicDansImg)) surElementInteractif = true;
        }

        int objX = (int) (iw * objetRatioX) - (TAILLE_OBJET / 2);
        int objY = (int) (ih * objetRatioY) - (TAILLE_OBJET / 2);
        Rectangle zonePointRougeDynamique = new Rectangle(objX, objY, TAILLE_OBJET, TAILLE_OBJET);
        if (!possedeObjet && universActuel.equals(universActuelObjet) && indexDecor == sceneActuelleObjet && zonePointRougeDynamique.contains(clicDansImg)) {
            surElementInteractif = true;
        }

        backgroundPanel.setCursor(new Cursor(surElementInteractif ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
        notifierChangementCurseur(surElementInteractif);
    }

    private void notifierChangementCurseur(boolean surElementInteractif) {
        if (cursorChangeListener != null) {
            cursorChangeListener.onCursorChanged(surElementInteractif);
        }
    }

    private void recalculerCurseurImmediat() {
        backgroundPanel.repaint();
        Point mousePos = getRelativeMousePoint();
        if (mousePos != null) {
            verifierEtMettreAJourCurseur(mousePos);
        } else {
            notifierChangementCurseur(false);
            backgroundPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }

    private Point getRelativeMousePoint() {
        PointerInfo pi = MouseInfo.getPointerInfo();
        if (pi == null || !backgroundPanel.isShowing()) return null;
        Point p = pi.getLocation();
        SwingUtilities.convertPointFromScreen(p, backgroundPanel);
        Rectangle imgBounds = backgroundPanel.getImageBounds();
        if (!imgBounds.contains(p)) return null;
        return new Point(p.x - imgBounds.x, p.y - imgBounds.y);
    }

    private void transitionner(String nomUnivers, String[] nouveauTableauDecors) {
        ChangerUnivers(nomUnivers, nouveauTableauDecors);
        recalculerCurseurImmediat();
    }

    private void transitionnerRetourSalon() {
        ChangerUnivers("SALON", decorsSalon); 
        indexDecor = 1;
        backgroundPanel.setNewImage(decorsActuels[indexDecor]);
        recalculerCurseurImmediat();
    }

    public JPanel getGamePanel() { return backgroundPanel; }

    private void ChangerUnivers(String nomUnivers, String[] nouveauTableauDecors) {
        universActuel = nomUnivers;
        decorsActuels = nouveauTableauDecors;
        indexDecor = 0; 
        backgroundPanel.setNewImage(decorsActuels[indexDecor]);
        mettreAJourTexteChambre();
    }

    private void mettreAJourTexteChambre() {
        if (txtExplicatif != null) {
            switch (universActuel) {
                case "SALON" -> txtExplicatif.setText(indexDecor == 0 ? "Le Salon - Cuisine" : "Le Salon - Les Portes");
                case "PIERRE" -> txtExplicatif.setText("Chambre de Pierre (Porte 2)");
                case "LOUIS" -> txtExplicatif.setText("Chambre de Louis (Porte 1)");
                case "JACQUES" -> txtExplicatif.setText("Chambre de Jacques (Porte 3)");
            }
        }
    }

    class BackgroundPanel extends JPanel {
        private Image backgroundImage;
        private int imgWidthOriginal = 1;
        private int imgHeightOriginal = 1;

        public BackgroundPanel(String path) { setNewImage(path); }

        public void setNewImage(String path) {
            URL url = getClass().getClassLoader().getResource(IMG_DIR + path);
            ImageIcon icon = (url != null) ? new ImageIcon(url) : new ImageIcon(path);
            this.backgroundImage = icon.getImage();
            if (backgroundImage != null) {
                this.imgWidthOriginal = icon.getIconWidth();
                this.imgHeightOriginal = icon.getIconHeight();
            }
            repaint();
        }

        public Rectangle getImageBounds() {
            if (backgroundImage == null) return new Rectangle(0, 0, getWidth(), getHeight());
            double panelRatio = (double) getWidth() / getHeight();
            double imageRatio = (double) imgWidthOriginal / imgHeightOriginal;
            int drawWidth, drawHeight;
            if (panelRatio > imageRatio) {
                drawHeight = getHeight();
                drawWidth = (int) (drawHeight * imageRatio);
            } else {
                drawWidth = getWidth();
                drawHeight = (int) (drawWidth / imageRatio);
            }
            int x = (getWidth() - drawWidth) / 2;
            int y = (getHeight() - drawHeight) / 2;
            return new Rectangle(x, y, drawWidth, drawHeight);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, getWidth(), getHeight());

            Rectangle r = getImageBounds();
            if (backgroundImage != null) g2d.drawImage(backgroundImage, r.x, r.y, r.width, r.height, this);

            if (!possedeObjet && universActuel.equals(universActuelObjet) && indexDecor == sceneActuelleObjet) {
                int objX = r.x + (int) (r.width * objetRatioX) - (TAILLE_OBJET / 2);
                int objY = r.y + (int) (r.height * objetRatioY) - (TAILLE_OBJET / 2);
                g2d.setColor(Color.RED);
                g2d.fillOval(objX, objY, TAILLE_OBJET, TAILLE_OBJET);
            }

            dessinerMiniMap(g2d);

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
            repositionnerComposants(r);
        }

        private void dessinerMiniMap(Graphics2D g2d) {
            int mapX = 30;
            int mapY = getHeight() - 170; 
            int tailleCase = 30; 
            Color couleurDefault = new Color(255, 255, 255, 120); 
            Color couleurActive = new Color(46, 204, 113, 220);   

            Rectangle rectPierre = new Rectangle(mapX, mapY, tailleCase * 2, tailleCase * 2);
            Rectangle rectLouis = new Rectangle(mapX, mapY + (tailleCase * 2), tailleCase * 2, tailleCase * 2);
            Rectangle rectSdb = new Rectangle(mapX, mapY + (tailleCase * 4), tailleCase * 2, tailleCase);

            int largJacques = 46; int largChambre4 = 22; int largChambre5 = 22;
            Rectangle rectJacques = new Rectangle(mapX + (tailleCase * 2), mapY, largJacques, tailleCase);
            Rectangle rectChambre4 = new Rectangle(rectJacques.x + largJacques, mapY, largChambre4, tailleCase);
            Rectangle rectChambre5 = new Rectangle(rectChambre4.x + largChambre4, mapY, largChambre5, tailleCase);
            Rectangle rectSalon = new Rectangle(mapX + (tailleCase * 2), mapY + tailleCase, largJacques + largChambre4 + largChambre5, tailleCase * 4);

            g2d.setColor(universActuel.equals("PIERRE") ? couleurActive : couleurDefault);
            g2d.fillRect(rectPierre.x, rectPierre.y, rectPierre.width, rectPierre.height);
            g2d.setColor(universActuel.equals("LOUIS") ? couleurActive : couleurDefault);
            g2d.fillRect(rectLouis.x, rectLouis.y, rectLouis.width, rectLouis.height);
            g2d.setColor(couleurDefault); g2d.fillRect(rectSdb.x, rectSdb.y, rectSdb.width, rectSdb.height);
            g2d.setColor(universActuel.equals("SALON") ? couleurActive : couleurDefault);
            g2d.fillRect(rectSalon.x, rectSalon.y, rectSalon.width, rectSalon.height);
            g2d.setColor(universActuel.equals("JACQUES") ? couleurActive : couleurDefault);
            g2d.fillRect(rectJacques.x, rectJacques.y, rectJacques.width, rectJacques.height);
            g2d.setColor(couleurDefault); 
            g2d.fillRect(rectChambre4.x, rectChambre4.y, rectChambre4.width, rectChambre4.height);
            g2d.fillRect(rectChambre5.x, rectChambre5.y, rectChambre5.width, rectChambre5.height);

            g2d.setColor(Color.BLACK); g2d.setStroke(new BasicStroke(1.5f));
            Rectangle[] toutesLesPieces = {rectPierre, rectLouis, rectSdb, rectSalon, rectJacques, rectChambre4, rectChambre5};
            for (Rectangle box : toutesLesPieces) g2d.drawRect(box.x, box.y, box.width, box.height);

            g2d.setFont(new Font("Arial", Font.BOLD, 9)); 
            g2d.drawString("Pierre", rectPierre.x + 15, rectPierre.y + 35);
            g2d.drawString("Louis", rectLouis.x + 18, rectLouis.y + 35);
            g2d.drawString("SdB", rectSdb.x + 22, rectSdb.y + 20);
            g2d.drawString("Jacques", rectJacques.x + 4, rectJacques.y + 20);
            g2d.drawString("4", rectChambre4.x + 8, rectChambre4.y + 20);
            g2d.drawString("5", rectChambre5.x + 8, rectChambre5.y + 20);
            g2d.setFont(new Font("Arial", Font.BOLD, 11));
            g2d.drawString("SALON", rectSalon.x + 25, rectSalon.y + 65);
        }

        private void repositionnerComposants(Rectangle r) {
            for (Component c : getComponents()) {
                if (c instanceof JButton) {
                    if (c.getName() != null && c.getName().equals("SYSTEM")) continue;
                    String txt = ((JButton) c).getText();
                    if (txt.equals("<")) c.setBounds(r.x + 20, r.y + (int)(r.height * 0.45), 60, 60);
                    else if (txt.equals(">")) c.setBounds(r.x + r.width - 80, r.y + (int)(r.height * 0.45), 60, 60);
                } else if (c instanceof JLabel) {
                    c.setBounds(r.x + 20, r.y + 20, 350, 30);
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Jeu().setVisible(true));
    }
}