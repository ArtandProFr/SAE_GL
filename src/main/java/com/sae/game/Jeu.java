package com.sae.game;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.roxane.app.Translations;
import com.sae.core.GameInfos;
import com.sae.core.Phase;
import com.sae.core.Save;
import com.sae.enigmas.ChambrePierreManager;
import com.sae.enigmas.EnigmeEmpreinteUI;
import com.sae.enigmas.EnigmeVerre;

public class Jeu extends JFrame {

    private static final String IMG_DIR = "images/Thomas/";

    public interface CursorChangeListener {
        void onCursorChanged(boolean surElementInteractif);
    }
    private CursorChangeListener cursorChangeListener;

    private ChambrePierreManager pierreManager = new ChambrePierreManager();
    private String[] decorsLouis = {"louis1.jpg", "louis2.jpg"};
    private String[] decorsJacques = {"jacques1.jpg", "jacques2.jpg"};
    private String[] decorsSalon = {"salon1.jpg", "salon2.jpg"};
    
    private String[] decorsActuels = decorsSalon; 
    private int indexDecor = 0;
    private String universActuel = "SALON"; 
    //private int indexPhaseActuelle = 0;
    
    private EnigmeVerre enigmeVerre = new EnigmeVerre();
    
    private boolean dialogueActif = false;
    private int indexDialogueArthur = 0;
    private String texteDialogueCourant = "";
    private boolean modeEnigmeEmpreinteActive = false;

    private BackgroundPanel backgroundPanel;
    private JLabel txtExplicatif;
    private JButton btnGauche;
    private JButton btnDroite;
    private JButton btnQuitterZoom;

    private Save save;
    private Phase phase;

    public Jeu(Save save) {
        this.save = save;
        phase = new Phase(save.getPhase());
        this.save.save();
        setTitle(Translations.t(GameInfos.GAMENAME_TYPE_2));
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

        btnQuitterZoom = new JButton("Quitter");
        btnQuitterZoom.setName("SYSTEM");
        btnQuitterZoom.setFont(new Font("Arial", Font.BOLD, 14));
        btnQuitterZoom.setBackground(new Color(192, 57, 43));
        btnQuitterZoom.setForeground(Color.WHITE);
        btnQuitterZoom.setVisible(false);
        btnQuitterZoom.addActionListener(event -> desactiverModeZoom());

        backgroundPanel.add(txtExplicatif);
        backgroundPanel.add(btnGauche);
        backgroundPanel.add(btnDroite);
        backgroundPanel.add(btnQuitterZoom);

        backgroundPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Rectangle imgBounds = backgroundPanel.getImageBounds();
                int iw = imgBounds.width;
                int ih = imgBounds.height;
                
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

                // --- GESTION DU MODE ZOOM ---
                if (universActuel.equals("PIERRE") && !pierreManager.getModeZoom().equals("AUCUN")) {
                    pierreManager.gererClicZoom(clicDansImg, iw, ih, Jeu.this);
                    return;
                }

                // --- CLIC NORMAL CHAMBRE PIERRE ---
                if (universActuel.equals("PIERRE") && pierreManager.getModeZoom().equals("AUCUN")) {
                    if (pierreManager.gererClic(indexDecor, clicDansImg, iw, ih, Jeu.this)) {
                        return;
                    }
                }

                // --- CLIC SUR ARTHUR ---
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

                // --- FOUILLE DES VERRES ---
                if (enigmeVerre.isCorpsExamine() && !dialogueActif) {
                    int idVerreClique = enigmeVerre.obtenirIdVerreClique(universActuel, indexDecor, clicDansImg, iw, ih);
                    if (idVerreClique != -1) {
                        ouvrirDialogueVerre(idVerreClique);
                        return;
                    }
                }

                if (dialogueActif || modeEnigmeEmpreinteActive) return;

                // --- PORTES DU SALON ---
                Rectangle porte1 = new Rectangle((int)(iw * 0.24), (int)(ih * 0.25), (int)(iw * 0.08), (int)(ih * 0.3));
                Rectangle porte2 = new Rectangle((int)(iw * 0.39), (int)(ih * 0.21), (int)(iw * 0.05), (int)(ih * 0.25));
                Rectangle porte3 = new Rectangle((int)(iw * 0.56), (int)(ih * 0.22), (int)(iw * 0.06), (int)(ih * 0.2));

                Rectangle porteSortiePierre = new Rectangle((int)(iw * 0.22), (int)(ih * 0.23), (int)(iw * 0.15), (int)(ih * 0.47));
                Rectangle zonePorteLouis = new Rectangle((int)(iw * 0.16), (int)(ih * 0.13), (int)(iw * 0.14), (int)(ih * 0.53));
                Rectangle zonePorteJacques = new Rectangle((int)(iw * 0.74), (int)(ih * 0.26), (int)(iw * 0.13), (int)(ih * 0.43));

                if (universActuel.equals("SALON") && indexDecor == 1) {
                    if (porte1.contains(clicDansImg)) { transitionner("LOUIS", decorsLouis); return; }
                    if (porte2.contains(clicDansImg)) { transitionner("PIERRE", pierreManager.obtenirDecorsPierre()); return; }
                    if (porte3.contains(clicDansImg)) { transitionner("JACQUES", decorsJacques); return; }
                }

                // SORTIES DES CHAMBRES
                if (indexDecor == 1) {
                    if (universActuel.equals("PIERRE") && porteSortiePierre.contains(clicDansImg)) { transitionnerRetourSalon(); return; }
                    if (universActuel.equals("LOUIS") && zonePorteLouis.contains(clicDansImg)) { transitionnerRetourSalon(); return; }
                    if (universActuel.equals("JACQUES") && zonePorteJacques.contains(clicDansImg)) { transitionnerRetourSalon(); return; }
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

    public void activerModeZoom(String imageZoom) {
        btnGauche.setVisible(false);
        btnDroite.setVisible(false);
        btnQuitterZoom.setVisible(true);
        backgroundPanel.setNewImage(imageZoom);
        recalculerCurseurImmediat();
    }

    private void desactiverModeZoom() {
        pierreManager.quitterZoom();
        btnQuitterZoom.setVisible(false);
        btnGauche.setVisible(true);
        btnDroite.setVisible(true);
        decorsActuels = pierreManager.obtenirDecorsPierre();
        backgroundPanel.setNewImage(decorsActuels[indexDecor]);
        recalculerCurseurImmediat();
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
        EnigmeEmpreinteUI fenetreEnigme = new EnigmeEmpreinteUI(this);
        fenetreEnigme.setVisible(true); 
        
        if (fenetreEnigme.isReussite()) {
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
            modeEnigmeEmpreinteActive = false;
            recalculerCurseurImmediat();
        }
    }

    public void setCursorChangeListener(CursorChangeListener listener) { this.cursorChangeListener = listener; }

    private void avancerPhaseTest() {
        phase.nextPhase();
        save.setPhase(phase.getNumero());
        save.save();
    }

    private void verifierEtMettreAJourCurseur(Point clicDansImg) {
        if (dialogueActif || modeEnigmeEmpreinteActive) return; 

        Rectangle imgBounds = backgroundPanel.getImageBounds();
        int iw = imgBounds.width;
        int ih = imgBounds.height;
        boolean surElementInteractif = false;

        if (universActuel.equals("PIERRE")) {
            surElementInteractif = pierreManager.verifierSurvol(indexDecor, clicDansImg, iw, ih);
        }

        if (!surElementInteractif && !enigmeVerre.isCorpsExamine() && universActuel.equals("SALON") && indexDecor == 0) {
            Rectangle zoneCorpsArthur = new Rectangle((int)(iw * 0.22), (int)(ih * 0.36), (int)(iw * 0.23), (int)(ih * 0.20));
            if (zoneCorpsArthur.contains(clicDansImg)) surElementInteractif = true;
        }

        if (!surElementInteractif && enigmeVerre.survolentUnVerre(universActuel, indexDecor, clicDansImg, iw, ih)) {
            surElementInteractif = true;
        }

        if (!surElementInteractif && universActuel.equals("SALON") && indexDecor == 1) {
            Rectangle porte1 = new Rectangle((int)(iw * 0.24), (int)(ih * 0.25), (int)(iw * 0.08), (int)(ih * 0.3));
            Rectangle porte2 = new Rectangle((int)(iw * 0.39), (int)(ih * 0.21), (int)(iw * 0.05), (int)(ih * 0.25));
            Rectangle porte3 = new Rectangle((int)(iw * 0.56), (int)(ih * 0.22), (int)(iw * 0.06), (int)(ih * 0.2));
            if (porte1.contains(clicDansImg) || porte2.contains(clicDansImg) || porte3.contains(clicDansImg)) surElementInteractif = true;
        }

        if (!surElementInteractif && indexDecor == 1) {
            if (universActuel.equals("PIERRE") && (new Rectangle((int)(iw * 0.22), (int)(ih * 0.23), (int)(iw * 0.15), (int)(ih * 0.47))).contains(clicDansImg)) surElementInteractif = true;
            if (universActuel.equals("LOUIS") && (new Rectangle((int)(iw * 0.16), (int)(ih * 0.13), (int)(iw * 0.14), (int)(ih * 0.53))).contains(clicDansImg)) surElementInteractif = true;
            if (universActuel.equals("JACQUES") && (new Rectangle((int)(iw * 0.74), (int)(ih * 0.26), (int)(iw * 0.13), (int)(ih * 0.43))).contains(clicDansImg)) surElementInteractif = true;
        }

        backgroundPanel.setSetCursorDirect(surElementInteractif);
    }

    private void notifierChangementCurseur(boolean surElementInteractif) {
        if (cursorChangeListener != null) cursorChangeListener.onCursorChanged(surElementInteractif);
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
        universActuel = nomUnivers;
        decorsActuels = nouveauTableauDecors;
        indexDecor = 0; 
        backgroundPanel.setNewImage(decorsActuels[indexDecor]);
        mettreAJourTexteChambre();
        recalculerCurseurImmediat();
    }

    private void transitionnerRetourSalon() {
        universActuel = "SALON";
        decorsActuels = decorsSalon;
        indexDecor = 1;
        backgroundPanel.setNewImage(decorsActuels[indexDecor]);
        mettreAJourTexteChambre();
        recalculerCurseurImmediat();
    }

    public BackgroundPanel getGamePanel() { return backgroundPanel; }

    private void mettreAJourTexteChambre() {
        if (txtExplicatif != null) {
            if (!enigmeVerre.isCorpsExamine() && universActuel.equals("SALON") && indexDecor == 0) {
                txtExplicatif.setText("Le Salon | Arthur est allongé sur le canapé... Il ne bouge plus.");
                return;
            }

            String progressionStr = "  |  Fouille : " + phase.getDescription();

            // AJOUT : Si le corps a été examiné mais que tous les verres ne sont pas trouvés, on force le compteur (X/5)
            if (enigmeVerre.isCorpsExamine() && !enigmeVerre.tousVerresTrouves()) {
                progressionStr = "  |  Fouille l'appartement : Trouvez les 5 verres rouges (" + enigmeVerre.compterVerresTrouves() + "/5)";
            }

            switch (universActuel) {
                case "SALON" -> txtExplicatif.setText(indexDecor == 0 ? "Le Salon - Cuisine" + progressionStr : "Le Salon - Les Portes" + progressionStr);
                case "PIERRE" -> txtExplicatif.setText(pierreManager.getModeZoom().equals("AUCUN") ? "Chambre de Pierre" + progressionStr : "Chambre de Pierre - [Zoom Actif]");
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
            return new Rectangle((getWidth() - drawWidth) / 2, (getHeight() - drawHeight) / 2, drawWidth, drawHeight);
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
                int boxH = 90, boxY = getHeight() - boxH - 20, boxX = 20, boxW = getWidth() - 40;
                g2d.setColor(new Color(0, 0, 0, 210)); g2d.fillRoundRect(boxX, boxY, boxW, boxH, 15, 15);
                g2d.setColor(new Color(255, 255, 255, 150)); g2d.setStroke(new BasicStroke(2)); g2d.drawRoundRect(boxX, boxY, boxW, boxH, 15, 15);
                g2d.setColor(Color.WHITE); g2d.setFont(new Font("Arial", Font.PLAIN, 15));
                String texteAAfficher = (!enigmeVerre.isCorpsExamine()) ? enigmeVerre.getTextesArthur()[indexDialogueArthur] : texteDialogueCourant;
                g2d.drawString(texteAAfficher, boxX + 25, boxY + 40);
                g2d.setFont(new Font("Arial", Font.ITALIC | Font.BOLD, 11)); g2d.setColor(new Color(46, 204, 113));
                g2d.drawString("[ Cliquer pour continuer > ]", boxX + boxW - 170, boxY + boxH - 15);
            } else if (pierreManager.getModeZoom().equals("AUCUN")) {
                dessinerMiniMap(g2d);
            }
            repositionnerComposants(r);
        }

        private void dessinerMiniMap(Graphics2D g2d) {
            int mapX = 30, mapY = getHeight() - 170, tailleCase = 30; 
            Color couleurDefault = new Color(255, 255, 255, 120), couleurActive = new Color(46, 204, 113, 220);   
            Rectangle rectPierre = new Rectangle(mapX, mapY, tailleCase * 2, tailleCase * 2);
            Rectangle rectLouis = new Rectangle(mapX, mapY + (tailleCase * 2), tailleCase * 2, tailleCase * 2);
            Rectangle rectSdb = new Rectangle(mapX, mapY + (tailleCase * 4), tailleCase * 2, tailleCase);
            int largJacques = 46, largChambre4 = 22, largChambre5 = 22;
            Rectangle rectJacques = new Rectangle(mapX + (tailleCase * 2), mapY, largJacques, tailleCase);
            Rectangle rectSalon = new Rectangle(mapX + (tailleCase * 2), mapY + tailleCase, largJacques + largChambre4 + largChambre5, tailleCase * 4);

            g2d.setColor(universActuel.equals("PIERRE") ? couleurActive : couleurDefault); g2d.fillRect(rectPierre.x, rectPierre.y, rectPierre.width, rectPierre.height);
            g2d.setColor(universActuel.equals("LOUIS") ? couleurActive : couleurDefault); g2d.fillRect(rectLouis.x, rectLouis.y, rectLouis.width, rectLouis.height);
            g2d.setColor(couleurDefault); g2d.fillRect(rectSdb.x, rectSdb.y, rectSdb.width, rectSdb.height);
            g2d.setColor(universActuel.equals("SALON") ? couleurActive : couleurDefault); g2d.fillRect(rectSalon.x, rectSalon.y, rectSalon.width, rectSalon.height);
            g2d.setColor(universActuel.equals("JACQUES") ? couleurActive : couleurDefault); g2d.fillRect(rectJacques.x, rectJacques.y, rectJacques.width, rectJacques.height);

            g2d.setColor(Color.BLACK); g2d.setStroke(new BasicStroke(1.5f));
            for (Rectangle box : new Rectangle[]{rectPierre, rectLouis, rectSdb, rectSalon, rectJacques}) g2d.drawRect(box.x, box.y, box.width, box.height);
            g2d.setColor(Color.WHITE); g2d.setFont(new Font("Arial", Font.BOLD, 9)); 
            g2d.drawString("Pierre", rectPierre.x + 15, rectPierre.y + 35); g2d.drawString("Louis", rectLouis.x + 18, rectLouis.y + 35);
            g2d.drawString("Jacques", rectJacques.x + 4, rectJacques.y + 20); g2d.setFont(new Font("Arial", Font.BOLD, 11)); g2d.drawString("SALON", rectSalon.x + 25, rectSalon.y + 65);
        }

        private void repositionnerComposants(Rectangle r) {
            for (Component c : getComponents()) {
                if (c instanceof JButton) {
                    if (c.getName() != null && c.getName().equals("SYSTEM")) {
                        c.setBounds(r.x + r.width - 130, r.y + r.height - 60, 110, 40);
                        continue;
                    }
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
        //SwingUtilities.invokeLater(() -> new Jeu().setVisible(true));
    }
}