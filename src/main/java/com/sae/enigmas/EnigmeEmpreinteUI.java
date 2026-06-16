package com.sae.enigmas;
import com.roxane.app.Translations;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage; // Import pour accéder à la difficulté
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import com.sae.core.Save;

public class EnigmeEmpreinteUI extends JDialog {

    private static final String IMG_DIR = "images/Thomas/";
    private boolean reussite = false;
    private String suspectSelectionne = "";

    private JLabel lblEmpreinteSuspect;
    private JPanel panelEmpreinteSuspect;
    private JPanel panelEmpreinteVerre; 
    private JPanel panelPuzzleGrid;     

    private List<JButton> boutonsSuspects = new ArrayList<>();
    private JButton btnValider;

    // Variables de taille devenues dynamiques (non-final)
    private int lignes = 2;
    private int colonnes = 2;
    private int totalMorceaux = 4;
    
    private MorceauPuzzle[] piecesMelangees;
    private JButton[] boutonsPuzzle;
    private int indexPremierMorceauSelectionne = -1; 
    private boolean puzzleResolu = false;
    private ImageIcon imageEntierePropre; 

    private static class MorceauPuzzle {
        int idOrigine;
        ImageIcon imageIcon;

        public MorceauPuzzle(int idOrigine, ImageIcon imageIcon) {
            this.idOrigine = idOrigine;
            this.imageIcon = imageIcon;
        }
    }

    // Le constructeur accepte désormais la Frame parent ET la sauvegarde actuelle (Requis par Jeu.java)
    public EnigmeEmpreinteUI(Frame parent, Save save) {
        super(parent, Translations.t("EMP_TITLE"), true);
        
        // Configuration dynamique de la difficulté basée sur la sauvegarde
        String diff = (save != null && save.getDifficulty() != null) ? save.getDifficulty().toUpperCase() : "NORMAL";
        
        switch (diff) {
            case "EASY" -> {
                this.lignes = 2;
                this.colonnes = 2;
            }
            case "HARD" -> {
                this.lignes = 4;
                this.colonnes = 4;
            }
            default -> { // Mode NORMAL par défaut
                this.lignes = 3;
                this.colonnes = 3;
            }
        }
        this.totalMorceaux = this.lignes * this.colonnes;

        setSize(780, 470);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());
        setAlwaysOnTop(true);

        JPanel panelNord = new JPanel();
        panelNord.setBackground(new Color(45, 52, 54));
        JLabel lblTitre = new JLabel(Translations.t("EMP_HEADER"));
        lblTitre.setForeground(Color.WHITE);
        lblTitre.setFont(new Font("Consolas", Font.BOLD, 14));
        panelNord.add(lblTitre);
        add(panelNord, BorderLayout.NORTH);

        JPanel panelCentre = new JPanel(new GridLayout(1, 2, 20, 0));
        panelCentre.setBackground(Color.BLACK);
        panelCentre.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        panelEmpreinteVerre = new JPanel(new BorderLayout());
        panelEmpreinteVerre.setBackground(new Color(25, 25, 25));
        panelEmpreinteVerre.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.RED, 2), 
                Translations.t("EMP_VERRE_TITLE"), TitledBorder.LEFT, TitledBorder.TOP, null, Color.RED));
        
        // Applique dynamiquement le nombre de lignes et colonnes calculé
        panelPuzzleGrid = new JPanel(new GridLayout(this.lignes, this.colonnes, 2, 2));
        panelPuzzleGrid.setBackground(new Color(25, 25, 25));
        
        genererPuzzleSecurise();
        
        panelEmpreinteVerre.add(panelPuzzleGrid, BorderLayout.CENTER);

        panelEmpreinteSuspect = new JPanel(new BorderLayout());
        panelEmpreinteSuspect.setBackground(new Color(25, 25, 25));
        panelEmpreinteSuspect.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY, 2), 
                Translations.t("EMP_FICHIER_SEL"), TitledBorder.LEFT, TitledBorder.TOP, null, Color.LIGHT_GRAY));
        
        lblEmpreinteSuspect = new JLabel(Translations.t("EMP_LOCKED"), SwingConstants.CENTER);
        lblEmpreinteSuspect.setForeground(Color.GRAY);
        lblEmpreinteSuspect.setFont(new Font("Arial", Font.ITALIC, 11));
        panelEmpreinteSuspect.add(lblEmpreinteSuspect, BorderLayout.CENTER);

        panelCentre.add(panelEmpreinteVerre);
        panelCentre.add(panelEmpreinteSuspect);
        add(panelCentre, BorderLayout.CENTER);

        JPanel panelDroite = new JPanel();
        panelDroite.setBackground(new Color(45, 52, 54));
        panelDroite.setLayout(new BoxLayout(panelDroite, BoxLayout.Y_AXIS));
        panelDroite.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));

        JLabel lblSuspects = new JLabel(Translations.t("EMP_DB"));
        lblSuspects.setForeground(Color.WHITE);
        lblSuspects.setFont(new Font("Arial", Font.BOLD, 12));
        lblSuspects.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelDroite.add(lblSuspects);
        panelDroite.add(Box.createVerticalStrut(15));

        String[] colocs = {"Louis", "Jacques", "Paul", "Pierre"};
        for (String coloc : colocs) {
            JButton btnColoc = new JButton(String.format(Translations.t("EMP_FICHIER_FMT"), coloc));
            btnColoc.setMaximumSize(new Dimension(180, 35));
            btnColoc.setAlignmentX(Component.CENTER_ALIGNMENT);
            btnColoc.setFont(new Font("Arial", Font.PLAIN, 13));
            btnColoc.setEnabled(false); 
            btnColoc.addActionListener(e -> selectionnerSuspect(coloc));
            
            boutonsSuspects.add(btnColoc);
            panelDroite.add(btnColoc);
            panelDroite.add(Box.createVerticalStrut(10));
        }

        panelDroite.add(Box.createVerticalGlue());

        btnValider = new JButton(Translations.t("EMP_CONFIRM"));
        btnValider.setMaximumSize(new Dimension(180, 45));
        btnValider.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnValider.setBackground(Color.GRAY);
        btnValider.setForeground(Color.WHITE);
        btnValider.setFont(new Font("Arial", Font.BOLD, 11));
        btnValider.setEnabled(false); 
        btnValider.addActionListener(e -> validerAnalyse());
        
        panelDroite.add(btnValider);
        add(panelDroite, BorderLayout.EAST);
    }

    private void genererPuzzleSecurise() {
        URL url = getClass().getClassLoader().getResource(IMG_DIR + "empreinte_verre.png");
        if (url == null) {
            url = getClass().getClassLoader().getResource(IMG_DIR + "empreinte_verre.jpg");
        }

        if (url == null) {
            afficherEcranErreur("Fichier introuvable !");
            return;
        }

        BufferedImage imageOrigine = null;
        try {
            imageOrigine = ImageIO.read(url);
        } catch (IOException e) {
            afficherEcranErreur("Erreur de lecture ImageIO");
            return;
        }

        if (imageOrigine == null) {
            afficherEcranErreur("Image corrompue");
            return;
        }

        BufferedImage buffer = new BufferedImage(160, 220, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = buffer.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(imageOrigine, 0, 0, 160, 220, null);
        g2d.dispose();

        imageEntierePropre = new ImageIcon(buffer);

        int pieceW = buffer.getWidth() / colonnes;
        int pieceH = buffer.getHeight() / lignes;

        MorceauPuzzle[] piecesInitiales = new MorceauPuzzle[totalMorceaux];
        piecesMelangees = new MorceauPuzzle[totalMorceaux];
        boutonsPuzzle = new JButton[totalMorceaux];

        int index = 0;
        for (int l = 0; l < lignes; l++) {
            for (int c = 0; c < colonnes; c++) {
                BufferedImage subImg = buffer.getSubimage(c * pieceW, l * pieceH, pieceW, pieceH);
                piecesInitiales[index] = new MorceauPuzzle(index, new ImageIcon(subImg));
                index++;
            }
        }

        List<MorceauPuzzle> list = new ArrayList<>(List.of(piecesInitiales));
        while (isOrdreCorrect(list)) {
            Collections.shuffle(list);
        }

        for (int i = 0; i < totalMorceaux; i++) {
            piecesMelangees[i] = list.get(i);
        }

        rafraichirGrillePuzzle();
    }

    private void afficherEcranErreur(String msg) {
        panelPuzzleGrid.setLayout(new BorderLayout());
        JLabel lblError = new JLabel("<html><center><font color='red'><b>[ Erreur ]</b></font><br>" + msg + "</center></html>", SwingConstants.CENTER);
        panelPuzzleGrid.add(lblError, BorderLayout.CENTER);
    }

    private void rafraichirGrillePuzzle() {
        panelPuzzleGrid.removeAll();
        for (int i = 0; i < totalMorceaux; i++) {
            final int indexCourant = i;
            boutonsPuzzle[i] = new JButton(piecesMelangees[i].imageIcon);
            
            if (puzzleResolu) {
                boutonsPuzzle[i].setBorder(BorderFactory.createEmptyBorder());
                boutonsPuzzle[i].setEnabled(false);
            } else {
                boutonsPuzzle[i].setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
                boutonsPuzzle[i].addActionListener(e -> gererClicMorceau(indexCourant));
            }
            
            boutonsPuzzle[i].setFocusPainted(false);
            panelPuzzleGrid.add(boutonsPuzzle[i]);
        }
        panelPuzzleGrid.revalidate();
        panelPuzzleGrid.repaint();
    }

    private void gererClicMorceau(int indexClique) {
        if (indexPremierMorceauSelectionne == -1) {
            indexPremierMorceauSelectionne = indexClique;
            boutonsPuzzle[indexClique].setBorder(BorderFactory.createLineBorder(Color.RED, 2));
        } else {
            int p1 = indexPremierMorceauSelectionne;
            int p2 = indexClique;

            if (p1 != p2) {
                MorceauPuzzle temp = piecesMelangees[p1];
                piecesMelangees[p1] = piecesMelangees[p2];
                piecesMelangees[p2] = temp;
            }

            indexPremierMorceauSelectionne = -1;

            if (verifierVictoire()) {
                puzzleResolu = true;
                rafraichirGrillePuzzle();
                
                JOptionPane.showMessageDialog(this, 
                    Translations.t("EMP_RECONSTITUTED"), 
                    Translations.t("EMP_ANALYZER"), JOptionPane.INFORMATION_MESSAGE);
                
                afficherImageEntierePropre();
                activerComposantsBase();
            } else {
                rafraichirGrillePuzzle();
            }
        }
    }

    private void afficherImageEntierePropre() {
        panelEmpreinteVerre.remove(panelPuzzleGrid); 
        JLabel lblImageLisse = new JLabel(imageEntierePropre, SwingConstants.CENTER);
        panelEmpreinteVerre.add(lblImageLisse, BorderLayout.CENTER); 
        panelEmpreinteVerre.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GREEN, 2), 
                Translations.t("EMP_VERRE_DONE"), TitledBorder.LEFT, TitledBorder.TOP, null, Color.GREEN));
        panelEmpreinteVerre.revalidate();
        panelEmpreinteVerre.repaint();
    }

    private boolean isOrdreCorrect(List<MorceauPuzzle> list) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).idOrigine != i) return false;
        }
        return true;
    }

    private boolean verifierVictoire() {
        for (int i = 0; i < totalMorceaux; i++) {
            if (piecesMelangees[i].idOrigine != i) return false;
        }
        return true;
    }

    private void activerComposantsBase() {
        for (JButton btn : boutonsSuspects) {
            btn.setEnabled(true);
        }
        btnValider.setEnabled(true);
        btnValider.setBackground(new Color(38, 166, 91));
        
        lblEmpreinteSuspect.setText(Translations.t("EMP_SELECT_SUSPECT"));
        panelEmpreinteSuspect.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GREEN, 2), 
                Translations.t("EMP_DB_UNLOCKED"), TitledBorder.LEFT, TitledBorder.TOP, null, Color.GREEN));
    }

    private void chargerImageEmpreinte(JLabel label, String nomFichier) {
        URL url = getClass().getClassLoader().getResource(IMG_DIR + nomFichier);
        if (url != null) {
            ImageIcon icon = new ImageIcon(url);
            Image img = icon.getImage().getScaledInstance(160, 220, Image.SCALE_SMOOTH);
            label.setIcon(new ImageIcon(img));
            label.setText("");
        } else {
            label.setIcon(null);
            label.setText("<html><center>[ Erreur ]<br>Fichier manquant<br>(" + nomFichier + ")</center></html>");
            label.setForeground(Color.RED);
        }
    }

    private void selectionnerSuspect(String nom) {
        if (!puzzleResolu) return;
        suspectSelectionne = nom;
        panelEmpreinteSuspect.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(46, 204, 113), 2), 
                String.format(Translations.t("EMP_RELEVE_FMT"), nom.toUpperCase()), TitledBorder.LEFT, TitledBorder.TOP, null, new Color(46, 204, 113)));
        chargerImageEmpreinte(lblEmpreinteSuspect, "empreinte_" + nom.toLowerCase() + ".png");
        repaint();
    }

    private void validerAnalyse() {
        if (suspectSelectionne.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                Translations.t("EMP_NO_SELECT"), 
                Translations.t("EMP_NO_SELECT_TITLE"), JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (suspectSelectionne.equalsIgnoreCase("Pierre")) {
            reussite = true;
            dispose(); 
        } else {
            JOptionPane.showMessageDialog(this, 
                Translations.t("EMP_FAIL"), 
                Translations.t("EMP_FAIL_TITLE"), JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isReussite() {
        return reussite;
    }
}