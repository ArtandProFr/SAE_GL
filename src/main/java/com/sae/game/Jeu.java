package com.sae.game;

import com.sae.core.Phase;         // Importation de la classe Phase depuis core
import com.sae.enigmas.EnigmeVerre; // Notre classe de gestion des verres dans com.sae.enigmas

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
    
    // Instance de notre gestionnaire d'énigme des verres
    private EnigmeVerre enigmeVerre = new EnigmeVerre();
    
    // Variables d'état pour le bandeau de dialogue
    private boolean dialogueActif = false;
    private int indexDialogueArthur = 0;
    private String texteDialogueCourant = "";

    private boolean modeEnigmeEmpreinteActive = false;

    private BackgroundPanel backgroundPanel;
    private JLabel txtExplicatif;
    private JButton btnGauche;
    private JButton btnDroite;

    public Jeu() {
        setTitle("Mon Escape Game SAE");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        backgroundPanel = new BackgroundPanel(decorsActuels[indexDecor]);
        backgroundPanel.setLayout(null); 

        txtExplicatif = new JLabel();
        txtExplicatif.setFont(new Font("Arial", Font.BOLD, 15)); 
        txtExplicatif.setForeground(Color.WHITE);
        mettreAJourTexteChambre();

        btnGauche = new JButton("<");
        btnGauche.setFont(new Font("Arial", Font.BOLD, 20));
        btnGauche.addActionListener(event -> {
            if (!enigmeVerre.isCorpsExamine() && universActuel.equals("SALON") && indexDecor == 0) return;

            indexDecor = (indexDecor - 1 + decorsActuels.length) % decorsActuels.length;
            backgroundPanel.setNewImage(decorsActuels[indexDecor]);
            mettreAJourTexteChambre();
            recalculerCurseurImmediat();
        });

        btnDroite = new JButton(">");
        btnDroite.setFont(new Font("Arial", Font.BOLD, 20));
        btnDroite.addActionListener(event -> {
            if (!enigmeVerre.isCorpsExamine() && universActuel.equals("SALON") && indexDecor == 0) {
                txtExplicatif.setText("Je devrais d'abord aller voir ce qu'a Arthur sur le canapé...");
                return;
            }

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
                
                // GESTION DU CLIC SUR LE DIALOGUE ACTIF (Défilement / Fermeture)
                if (dialogueActif) {
                    int zoneDialogueY = backgroundPanel.getHeight() - 110;
                    if (e.getY() >= zoneDialogueY) {
                        if (!enigmeVerre.isCorpsExamine()) {
                            indexDialogueArthur++;
                            if (indexDialogueArthur < enigmeVerre.getTextesArthur().length) {
                                backgroundPanel.repaint(); 
                            } else {
                                dialogueActif = false;
                                enigmeVerre.setCorpsExamine(true);
                                avancerPhaseTest(); 
                                btnGauche.setVisible(true);
                                btnDroite.setVisible(true);
                                mettreAJourTexteChambre();
                            }
                        } else {
                            dialogueActif = false;
                            if (enigmeVerre.tousVerresTrouves() && !modeEnigmeEmpreinteActive) {
                                lancerEnigmeEmpreintes();
                            }
                            if (!modeEnigmeEmpreinteActive) {
                                btnGauche.setVisible(true);
                                btnDroite.setVisible(true);
                            }
                            mettreAJourTexteChambre();
                        }
                        recalculerCurseurImmediat();
                        return;
                    }
                }

                if (!imgBounds.contains(e.getPoint())) return;

                int mouseXInImg = e.getX() - imgBounds.x;
                int mouseYInImg = e.getY() - imgBounds.y;
                Point clicDansImg = new Point(mouseXInImg, mouseYInImg);

                // --- CLIC SUR LE CORPS D'ARTHUR (Salon 1) ---
                if (universActuel.equals("SALON") && indexDecor == 0) {
                    Rectangle zoneCorpsArthur = new Rectangle((int)(iw * 0.22), (int)(ih * 0.36), (int)(iw * 0.23), (int)(ih * 0.20));
                    if (!enigmeVerre.isCorpsExamine() && !dialogueActif && zoneCorpsArthur.contains(clicDansImg)) {
                        dialogueActif = true;
                        indexDialogueArthur = 0;
                        btnGauche.setVisible(false); 
                        btnDroite.setVisible(false);
                        backgroundPanel.repaint();
                        return;
                    }
                }

                // --- FOUILLE DES VERRES (Déléguée à EnigmeVerre) ---
                if (enigmeVerre.isCorpsExamine() && !dialogueActif) {
                    int idVerreClique = enigmeVerre.obtenirIdVerreClique(universActuel, indexDecor, clicDansImg, iw, ih);
                    if (idVerreClique != -1) {
                        ouvrirDialogueVerre(idVerreClique);
                        return;
                    }
                }

                if (dialogueActif || modeEnigmeEmpreinteActive) return;

                // --- ZONES DES PORTES ET DEPLACEMENTS ---
                Rectangle porte1 = new Rectangle((int)(iw * 0.24), (int)(ih * 0.25), (int)(iw * 0.08), (int)(ih * 0.3));
                Rectangle porte2 = new Rectangle((int)(iw * 0.39), (int)(ih * 0.21), (int)(iw * 0.05), (int)(ih * 0.25));
                Rectangle porte3 = new Rectangle((int)(iw * 0.56), (int)(ih * 0.22), (int)(iw * 0.06), (int)(ih * 0.2));

                Rectangle porteSortiePierre = new Rectangle((int)(iw * 0.22), (int)(ih * 0.23), (int)(iw * 0.15), (int)(ih * 0.47));
                Rectangle zonePorteLouis = new Rectangle((int)(iw * 0.16), (int)(ih * 0.13), (int)(iw * 0.14), (int)(ih * 0.53));
                Rectangle zonePorteJacques = new Rectangle((int)(iw * 0.74), (int)(ih * 0.26), (int)(iw * 0.13), (int)(ih * 0.43));

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
            }
        });

        backgroundPanel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (dialogueActif && e.getY() >= backgroundPanel.getHeight() - 110) {
                    backgroundPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    notifierChangementCurseur(true);
                    return;
                }

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

    private void ouvrirDialogueVerre(int idVerre) {
        texteDialogueCourant = enigmeVerre.inspecterVerre(idVerre);
        dialogueActif = true;
        btnGauche.setVisible(false);
        btnDroite.setVisible(false);
        backgroundPanel.repaint();
    }

    private void lancerEnigmeEmpreintes() {
        modeEnigmeEmpreinteActive = true;
        
        String input = JOptionPane.showInputDialog(this, 
            "Énigme : Faites correspondre les lignes papillaires de l'empreinte.\n" +
            "À quel colocataire appartient cette empreinte digitale ? (Saisir : Pierre)", 
            "Analyseur d'Empreintes v1.0", JOptionPane.QUESTION_MESSAGE);
            
        if (input != null && input.equalsIgnoreCase("Pierre")) {
            JOptionPane.showMessageDialog(this, 
                "CORRECT ! L'empreinte correspond à 99.8% avec celle de Pierre.\n" +
                "Nouveau lieu débloqué : Chambre de Pierre !", 
                "Analyse Réussie", JOptionPane.INFORMATION_MESSAGE);
                
            modeEnigmeEmpreinteActive = false;
            btnGauche.setVisible(true);
            btnDroite.setVisible(true);
            avancerPhaseTest(); 
            mettreAJourTexteChambre();
        } else {
            JOptionPane.showMessageDialog(this, 
                "ÉCHEC ! L'alignement des empreintes est incorrect. Réessaye.", 
                "Erreur d'Analyse", JOptionPane.ERROR_MESSAGE);
            lancerEnigmeEmpreintes();
        }
    }

    public void setCursorChangeListener(CursorChangeListener listener) {
        this.cursorChangeListener = listener;
    }

    public void setCursorChangeListener(Object... args) {}

    private void avancerPhaseTest() {
        Phase phaseActuelle = Phase.TOUTES_LES_PHASES[indexPhaseActuelle];
        if (phaseActuelle.estJeuFini()) return;
        if (indexPhaseActuelle < Phase.TOUTES_LES_PHASES.length - 1) {
            indexPhaseActuelle++;
            System.out.println("[PROGRESSION] Passage à la Phase " + Phase.TOUTES_LES_PHASES[indexPhaseActuelle].getNumero());
        }
    }

    private void verifierEtMettreAJourCurseur(Point clicDansImg) {
        if (dialogueActif || modeEnigmeEmpreinteActive) return; 

        Rectangle imgBounds = backgroundPanel.getImageBounds();
        int iw = imgBounds.width;
        int ih = imgBounds.height;

        boolean surElementInteractif = false;

        // Arthur (Salon 1)
        if (!enigmeVerre.isCorpsExamine() && universActuel.equals("SALON") && indexDecor == 0) {
            Rectangle zoneCorpsArthur = new Rectangle((int)(iw * 0.22), (int)(ih * 0.36), (int)(iw * 0.23), (int)(ih * 0.20));
            if (zoneCorpsArthur.contains(clicDansImg)) surElementInteractif = true;
        }

        // Vérification des verres (Déléguée à EnigmeVerre)
        if (enigmeVerre.survolentUnVerre(universActuel, indexDecor, clicDansImg, iw, ih)) {
            surElementInteractif = true;
        }

        // Portes du Salon 2
        if (universActuel.equals("SALON") && indexDecor == 1) {
            Rectangle porte1 = new Rectangle((int)(iw * 0.24), (int)(ih * 0.25), (int)(iw * 0.08), (int)(ih * 0.3));
            Rectangle porte2 = new Rectangle((int)(iw * 0.39), (int)(ih * 0.21), (int)(iw * 0.05), (int)(ih * 0.25));
            Rectangle porte3 = new Rectangle((int)(iw * 0.56), (int)(ih * 0.22), (int)(iw * 0.06), (int)(ih * 0.2));
            if (porte1.contains(clicDansImg) || porte2.contains(clicDansImg) || porte3.contains(clicDansImg)) surElementInteractif = true;
        }

        // Portes de sortie des chambres
        if (indexDecor == 1) {
            if (universActuel.equals("PIERRE") && (new Rectangle((int)(iw * 0.22), (int)(ih * 0.23), (int)(iw * 0.15), (int)(ih * 0.47))).contains(clicDansImg)) surElementInteractif = true;
            if (universActuel.equals("LOUIS") && (new Rectangle((int)(iw * 0.16), (int)(ih * 0.13), (int)(iw * 0.14), (int)(ih * 0.53))).contains(clicDansImg)) surElementInteractif = true;
            if (universActuel.equals("JACQUES") && (new Rectangle((int)(iw * 0.74), (int)(ih * 0.26), (int)(iw * 0.13), (int)(ih * 0.43))).contains(clicDansImg)) surElementInteractif = true;
        }

        backgroundPanel.setSetCursorDirect(surElementInteractif);
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
            if (!enigmeVerre.isCorpsExamine() && universActuel.equals("SALON") && indexDecor == 0) {
                txtExplicatif.setText("Le Salon | Arthur est allongé sur le canapé... Il ne bouge plus. Je devrais l'examiner.");
                return;
            }

            Phase p = Phase.TOUTES_LES_PHASES[indexPhaseActuelle];
            String progressionStr = "  |  [" + p.getNumero() + "] " + p.getDescription() + " (" + p.getPourcentage() + "%)";

            if (p.getNumero().equals("1.1")) {
                progressionStr = "  |  Fouille l'appartement : Trouvez les 5 verres rouges (" + enigmeVerre.compterVerresTrouves() + "/5)";
            }

            switch (universActuel) {
                case "SALON" -> txtExplicatif.setText(indexDecor == 0 ? "Le Salon - Cuisine" + progressionStr : "Le Salon - Les Portes" + progressionStr);
                case "PIERRE" -> txtExplicatif.setText("Chambre de Pierre" + progressionStr);
                case "LOUIS" -> txtExplicatif.setText("Chambre de Louis" + progressionStr);
                case "JACQUES" -> txtExplicatif.setText("Chambre de Jacques" + progressionStr);
            }
        }
    }

    class BackgroundPanel extends JPanel {
        private Image backgroundImage;
        private int imgWidthOriginal = 1;
        private int imgHeightOriginal = 1;

        public BackgroundPanel(String path) { setNewImage(path); }

        public void setSetCursorDirect(boolean hand) {
            setCursor(new Cursor(hand ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            notifierChangementCurseur(hand);
        }

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

            if (dialogueActif) {
                int boxH = 90;
                int boxY = getHeight() - boxH - 20;
                int boxX = 20;
                int boxW = getWidth() - 40;

                g2d.setColor(new Color(0, 0, 0, 210));
                g2d.fillRoundRect(boxX, boxY, boxW, boxH, 15, 15);

                g2d.setColor(new Color(255, 255, 255, 150));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(boxX, boxY, boxW, boxH, 15, 15);

                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.PLAIN, 15));
                
                String texteAAfficher = (!enigmeVerre.isCorpsExamine()) ? enigmeVerre.getTextesArthur()[indexDialogueArthur] : texteDialogueCourant;
                g2d.drawString(texteAAfficher, boxX + 25, boxY + 40);

                g2d.setFont(new Font("Arial", Font.ITALIC | Font.BOLD, 11));
                g2d.setColor(new Color(46, 204, 113));
                g2d.drawString("[ Cliquer pour continuer > ]", boxX + boxW - 170, boxY + boxH - 15);
            } else {
                dessinerMiniMap(g2d);
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
                    c.setBounds(r.x + 20, r.y + 20, 650, 30);
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Jeu().setVisible(true));
    }
}