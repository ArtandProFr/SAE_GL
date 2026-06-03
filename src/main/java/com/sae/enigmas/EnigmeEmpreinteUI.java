package com.sae.enigmas;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.net.URL;

public class EnigmeEmpreinteUI extends JDialog {

    private static final String IMG_DIR = "images/Thomas/";
    private boolean reussite = false;
    private String suspectSelectionne = "";

    private JLabel lblEmpreinteVerre;
    private JLabel lblEmpreinteSuspect;
    private JPanel panelEmpreinteSuspect;

    public EnigmeEmpreinteUI(Frame parent) {
        super(parent, "Laboratoire d'Analyse Criminelle - Empreintes", true);
        setSize(780, 450);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());

        JPanel panelNord = new JPanel();
        panelNord.setBackground(new Color(45, 52, 54));
        JLabel lblTitre = new JLabel("STATION D'ANALYSE : RECHERCHE DE CORRESPONDANCE");
        lblTitre.setForeground(Color.WHITE);
        lblTitre.setFont(new Font("Consolas", Font.BOLD, 16));
        panelNord.add(lblTitre);
        add(panelNord, BorderLayout.NORTH);

        JPanel panelCentre = new JPanel(new GridLayout(1, 2, 20, 0));
        panelCentre.setBackground(Color.BLACK);
        panelCentre.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel panelEmpreinteVerre = new JPanel(new BorderLayout());
        panelEmpreinteVerre.setBackground(new Color(25, 25, 25));
        panelEmpreinteVerre.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.RED, 2), 
                " ÉCHANTILLON : VERRE ", TitledBorder.LEFT, TitledBorder.TOP, null, Color.RED));
        
        lblEmpreinteVerre = new JLabel("", SwingConstants.CENTER);
        chargerImageEmpreinte(lblEmpreinteVerre, "empreinte_verre.png");
        panelEmpreinteVerre.add(lblEmpreinteVerre, BorderLayout.CENTER);

        panelEmpreinteSuspect = new JPanel(new BorderLayout());
        panelEmpreinteSuspect.setBackground(new Color(25, 25, 25));
        panelEmpreinteSuspect.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY, 2), 
                " FICHIER SÉLECTIONNÉ ", TitledBorder.LEFT, TitledBorder.TOP, null, Color.LIGHT_GRAY));
        
        lblEmpreinteSuspect = new JLabel("Aucun fichier chargé", SwingConstants.CENTER);
        lblEmpreinteSuspect.setForeground(Color.GRAY);
        lblEmpreinteSuspect.setFont(new Font("Arial", Font.ITALIC, 14));
        panelEmpreinteSuspect.add(lblEmpreinteSuspect, BorderLayout.CENTER);

        panelCentre.add(panelEmpreinteVerre);
        panelCentre.add(panelEmpreinteSuspect);
        add(panelCentre, BorderLayout.CENTER);

        JPanel panelDroite = new JPanel();
        panelDroite.setBackground(new Color(45, 52, 54));
        panelDroite.setLayout(new BoxLayout(panelDroite, BoxLayout.Y_AXIS));
        panelDroite.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));

        JLabel lblSuspects = new JLabel("BASE DE DONNÉES :");
        lblSuspects.setForeground(Color.WHITE);
        lblSuspects.setFont(new Font("Arial", Font.BOLD, 12));
        lblSuspects.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelDroite.add(lblSuspects);
        panelDroite.add(Box.createVerticalStrut(15));

        String[] colocs = {"Louis", "Jacques", "Paul", "Pierre"};
        for (String coloc : colocs) {
            JButton btnColoc = new JButton("Fichier: " + coloc);
            btnColoc.setMaximumSize(new Dimension(180, 35));
            btnColoc.setAlignmentX(Component.CENTER_ALIGNMENT);
            btnColoc.setFont(new Font("Arial", Font.PLAIN, 13));
            btnColoc.addActionListener(e -> selectionnerSuspect(coloc));
            panelDroite.add(btnColoc);
            panelDroite.add(Box.createVerticalStrut(10));
        }

        panelDroite.add(Box.createVerticalGlue());

        JButton btnValider = new JButton("CONFIRMER ACCUSATION");
        btnValider.setMaximumSize(new Dimension(180, 45));
        btnValider.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnValider.setBackground(new Color(38, 166, 91));
        btnValider.setForeground(Color.WHITE);
        btnValider.setFont(new Font("Arial", Font.BOLD, 11));
        btnValider.addActionListener(e -> validerAnalyse());
        
        panelDroite.add(btnValider);
        add(panelDroite, BorderLayout.EAST);
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
        suspectSelectionne = nom;
        panelEmpreinteSuspect.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(46, 204, 113), 2), 
                " RELEVÉ : " + nom.toUpperCase() + " ", TitledBorder.LEFT, TitledBorder.TOP, null, new Color(46, 204, 113)));
        chargerImageEmpreinte(lblEmpreinteSuspect, "empreinte_" + nom.toLowerCase() + ".png");
        repaint();
    }

    private void validerAnalyse() {
        if (suspectSelectionne.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Veuillez d'abord sélectionner un fichier de suspect.", 
                "Analyse impossible", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (suspectSelectionne.equalsIgnoreCase("Pierre")) {
            reussite = true;
            dispose(); 
        } else {
            JOptionPane.showMessageDialog(this, 
                "ANALYSE : Correspondance négative.\nLes minuties et bifurcations des lignes ne correspondent pas.", 
                "Échec de l'alignement", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isReussite() {
        return reussite;
    }
}