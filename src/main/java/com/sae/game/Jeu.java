package com.sae.game;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import com.roxane.app.NewGameScreen;
import com.roxane.app.Translations;
import com.sae.core.GameInfos;
import com.sae.core.Phase;
import com.sae.core.Save;
import com.sae.core.Time;
import com.sae.enigmas.ChambrePierreManager;
import com.sae.enigmas.EnigmeEmpreinteUI;
import com.sae.enigmas.EnigmeVerre;
import com.sae.enigmas.LogicGateUI;
import com.sae.enigmas.MovingBallsUI;
import com.sae.enigmas.MovingLightsUI;
import com.sae.enigmas.OndesUI;
import com.sae.enigmas.OrdiLouisUI;
import com.sae.enigmas.RotaryDialUI;
import com.sae.enigmas.UVLampUI;
import com.sae.enigmas.UVLampServiettesUI;

import javafx.stage.Stage;

/**
 * Boucle principale du jeu : navigation entre univers (salon, chambres, sdb),
 * déclenchement des énigmes selon la phase et restauration de l'état au
 * chargement d'une partie sauvegardée.
 * <p>
 * Conventions :
 * <ul>
 *   <li>{@code phase}    : numéro de phase persisté dans la sauvegarde (ex. 3.4).</li>
 *   <li>{@code subStep}  : avancée à l'intérieur d'une phase contenant plusieurs étapes
 *       (verre→empreinte→correspondance, clé→armoire, code→déverrouillage, etc.).
 *       Cette valeur n'est PAS persistée : à la reprise d'une partie, elle vaut 0
 *       et le joueur doit (re)déclencher l'énigme qui ouvre la phase.</li>
 *   <li>{@code universActuel} : SALON, PIERRE, LOUIS, JACQUES, SALLE_DE_BAIN, THOMAS, PAUL.</li>
 * </ul>
 */
public class Jeu extends JFrame {

    private static final String IMG_DIR = "images/Thomas/";

    /* ─── Univers ───────────────────────────────────────────────────────────── */
    private static final String U_SALON   = "SALON";
    private static final String U_PIERRE  = "PIERRE";
    private static final String U_LOUIS   = "LOUIS";
    private static final String U_JACQUES = "JACQUES";
    private static final String U_SDB     = "SALLE_DE_BAIN";
    private static final String U_THOMAS  = "THOMAS";
    private static final String U_PAUL    = "PAUL";
    private static final String U_DEHORS  = "DEHORS";

    public interface CursorChangeListener {
        void onCursorChanged(boolean surElementInteractif);
    }
    private CursorChangeListener cursorChangeListener;

    /* ─── Gestionnaires & énigmes ───────────────────────────────────────────── */
    private ChambrePierreManager pierreManager = new ChambrePierreManager();
    private EnigmeVerre enigmeVerre = new EnigmeVerre();

    private final String[] decorsLouis   = {"louis1.jpg", "louis2.jpg"};
    private final String[] decorsJacques = {"jacques1.jpg", "jacques2.jpg"};
    private final String[] decorsSalon   = {"salon1.jpg", "salon2.jpg"};
    private final String[] decorsSdb     = {"sdb1.jpg", "sdb2.jpg"};
    /** Pour les pièces sans visuel dédié (SDB / Thomas / Paul) : décor procédural. */
    private final String[] decorsPlaceholderSdb     = {"__SDB1__",     "__SDB2__"};
    private final String[] decorsPlaceholderThomas  = {"__THOMAS__"};
    private final String[] decorsPlaceholderPaul    = {"__PAUL__"};

    private String[] decorsActuels = decorsSalon;
    private int indexDecor = 0;
    private String universActuel = U_SALON;

    /* ─── État de jeu ───────────────────────────────────────────────────────── */
    private boolean dialogueActif = false;
    private int  indexDialogue = 0;
    private String texteDialogueCourant = "";
    private String[] textesDialogueCustom = null;
    private boolean modeEnigmeActive = false;

    /** Avancée intra-phase non persistée (cf. javadoc de la classe). */
    private int subStep = 0;

    /** Mode crédits : à la fin du jeu, on affiche les crédits sur fond noir
     *  et un clic ramène au menu (déclenché sur le thread JavaFX). */
    private boolean creditsMode = false;

    /** Flags d'objets ramassés / découverts par phase 3 / 4 / 5. */
    private boolean postItAffiche       = false;
    private boolean codeLouisTrouve     = false;  // post-it lu
    private boolean ordiLouisDeverr     = false;
    private boolean foundTableauElectrique = false; // 3.3 résolue
    private boolean tableauElectriqueOk = false;  // 3.4 résolue
    private boolean lumieresOk          = false;  // 3.5 résolue
    private boolean discussionLue       = false;  // 3.6
    private boolean livreChimieOuvert   = false;  // 3.7
    private boolean telephoneDecroche   = false;  // 4.1
    private boolean repondeurEcoute     = false;  // 4.2
    private boolean lampeUVRamassee     = false;  // 4.3
    private boolean serviettesVues      = false;  // 4.3
    //private boolean uvSurPlafondJacques = false;  // 5.2 optionnel
    private boolean tiroirJacquesOuvert = false;  // 5.2 énigme boules
    private boolean fioleTrouvee        = false;  // 5.2

    /* ─── UI Swing ──────────────────────────────────────────────────────────── */
    private BackgroundPanel backgroundPanel;
    private JLabel txtExplicatif;
    private JButton btnGauche;
    private JButton btnDroite;
    private JButton btnQuitterZoom;

    private final Save save;
    private final Phase phase;
    private final Stage stage;
    private final String DEFAULT_TITLE;

    private NewGameScreen parent;

    public Jeu(Save save, Phase phase, Stage stage, NewGameScreen parent) {
        this.save = save;
        this.parent = parent;
        this.stage = stage;
        this.DEFAULT_TITLE = "Escape Game - " + save.getSavename()
                + " (" + save.getUsername() + ") / [" + Translations.t(save.getDifficulty()) + "]";

        // Extraction et lissage de la phase sauvegardée
        double numPhaseSauvegardee = (this.save != null) ? this.save.getPhase() : 0.1;
        if (numPhaseSauvegardee <= 0) numPhaseSauvegardee = 0.1;
        numPhaseSauvegardee = Math.round(numPhaseSauvegardee * 10) / 10.0;
        this.phase = phase;
        this.phase.change(new Phase(numPhaseSauvegardee));
        if (this.save != null) this.save.save();
        changeTitleProgression();
        setTitle(Translations.t(GameInfos.GAMENAME_TYPE_2));
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        backgroundPanel = new BackgroundPanel(decorsActuels[indexDecor]);
        backgroundPanel.setLayout(null);

        txtExplicatif = new JLabel();
        txtExplicatif.setFont(new Font("Arial", Font.BOLD, 15));
        txtExplicatif.setForeground(Color.WHITE);

        btnGauche = new JButton("<");
        btnGauche.setFont(new Font("Arial", Font.BOLD, 20));
        btnGauche.addActionListener(event -> naviguerDecor(-1));

        btnDroite = new JButton(">");
        btnDroite.setFont(new Font("Arial", Font.BOLD, 20));
        btnDroite.addActionListener(event -> naviguerDecor(+1));

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

        // ─── Restauration de l'état selon la phase ─────────────────────────
        restaurerEtatProgression();
        mettreAJourTexteChambre();
        rafraichirAffichage();
        this.repaint();

        backgroundPanel.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { gererClicSouris(e); }
        });
        backgroundPanel.addMouseMotionListener(new MouseAdapter() {
            @Override public void mouseMoved(MouseEvent e) { gererMouvementSouris(e); }
        });

        // ─── Intégration globale du raccourci Admin '!' via Key Bindings ───
        setupAdminKeyBinding();

        setContentPane(backgroundPanel);
    }

    /**
     * Configure le raccourci clavier global pour la touche '²' (indépendant du focus)[cite: 4]
     */
    private void setupAdminKeyBinding() {
        // Raccourci corrigé sur la touche '!' pour le mode admin
        backgroundPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke('!'), "ouvrirAdminPhase"
        );
        backgroundPanel.getActionMap().put("ouvrirAdminPhase", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                promptChangePhase();
            }
        });
    }

    /* ════════════════════════════════════════════════════════════════════════
     *                       RESTAURATION D'ÉTAT (switch)
     * ════════════════════════════════════════════════════════════════════════ */

    private void resetPhase(){
        if (this.phase.getNumero() <= 2.1){
            pierreManager = new ChambrePierreManager();
        }
        enigmeVerre = new EnigmeVerre();
        decorsActuels = decorsSalon;
        indexDecor = 0;
        universActuel = U_SALON;

        /* ─── État de jeu ───────────────────────────────────────────────────────── */
        dialogueActif = false;
        indexDialogue = 0;
        texteDialogueCourant = "";
        textesDialogueCustom = null;
        modeEnigmeActive = false;

        /** Avancée intra-phase non persistée (cf. javadoc de la classe). */
        subStep = 0;

        /** Flags d'objets ramassés / découverts par phase 3 / 4 / 5. */
        codeLouisTrouve     = false;  // post-it lu
        ordiLouisDeverr     = false;
        foundTableauElectrique = false; // 3.3 résolue
        tableauElectriqueOk = false;  // 3.4 résolue
        lumieresOk          = false;  // 3.5 résolue
        discussionLue       = false;  // 3.6
        livreChimieOuvert   = false;  // 3.7
        telephoneDecroche   = false;  // 4.1
        repondeurEcoute     = false;  // 4.2
        lampeUVRamassee     = false;  // 4.3
        serviettesVues      = false;  // 4.3
        //uvSurPlafondJacques = false;  // 5.2 optionnel
        tiroirJacquesOuvert = false;  // 5.2 énigme boules
        fioleTrouvee        = false;  // 5.2
    }

    /**
     * Place le joueur dans la bonne pièce/vue selon la phase chargée.
     * Toute énigme rencontrée en arrivant est <b>réinitialisée</b> ; le joueur
     * devra cliquer sur l'élément déclencheur pour la (re)faire.
     */
    private void restaurerEtatProgression() {
        if (this.save == null) return;
        resetPhase();
        subStep = 0;

        // Tronque le double en clé d'entier (0.1→1, 1.1→11, 3.4→34…)
        int key = numPhase();

        switch (key) {
            case 1 -> { // 0.1
                enigmeVerre.setCorpsExamine(false);
                placerDans(U_SALON, decorsSalon, 0);
            }
            case 11 -> { // 1.1
                enigmeVerre.setCorpsExamine(true);
                placerDans(U_SALON, decorsSalon, 0);
            }
            case 21 -> { // 2.1
                marquerEnqueteSalonComplete();
                placerDans(U_SALON, decorsSalon, 0);
            }
            case 31 -> { // 3.1
                marquerEnqueteSalonComplete();
                marquerPierreComplet();
                placerDans(U_SALON, decorsSalon, 1);
            }
            case 32 -> { // 3.2 - Ordinateur
                preparerJusqua32();
                placerDans(U_LOUIS, decorsLouis, 0);
            }
            case 33 -> { // 3.3 - Coupure courant
                preparerJusqua33();
                placerDans(U_LOUIS, decorsLouis, 1);
            }
            case 34 -> { // 3.4 - Portes logiques
                preparerJusqua34();
                placerDans(U_SALON, decorsSalon, 0);
            }
            case 35 -> { // 3.5 - Lumières
                preparerJusqua35();
                placerDans(U_SALON, decorsSalon, 0);
            }
            case 36 -> { // 3.6 - Discussion
                preparerJusqua36();
                placerDans(U_LOUIS, decorsLouis, 0);
            }
            case 37 -> { // 3.7 - Chimie
                preparerJusqua37();
                placerDans(U_LOUIS, decorsLouis, 1);
            }
            case 41 -> { // 4.1 - Téléphone
                preparerJusqua41();
                placerDans(U_SALON, decorsSalon, 1);
            }
            case 42 -> { // 4.2 - Répondeur (cinématique)
                preparerJusqua37();
                placerDans(U_SALON, decorsSalon, 1);
            }
            case 43 -> { // 4.3 - Enquête SDB
                preparerJusqua42();
                placerDans(U_SDB, decorsSdb, 0);
            }
            case 51 -> { // 5.1 - Paul rentre (cinématique)
                preparerJusqua43();
                placerDans(U_SALON, decorsSalon, 0);
                cinematiquePaul();
            }
            case 52 -> { // 5.2 - Chambre Jacques
                preparerJusqua43();
                placerDans(U_JACQUES, decorsJacques, 0);
            }
            case 61 -> { // 6.1 - Révélations
                preparerJusqua52();
                placerDans(U_JACQUES, decorsJacques, 0);
                cinematiqueRevelations();
            }
            case 71 -> { // 7.1 - Crédits
                preparerJusqua52();
                placerDans(U_SALON, decorsSalon, 0);
                entrerModeCredits();
            }
            default -> {
                enigmeVerre.setCorpsExamine(false);
                placerDans(U_SALON, decorsSalon, 0);
            }
        }
        backgroundPanel.setNewImage(decorsActuels[indexDecor]);
        repaint();
    }

    private void placerDans(String u, String[] decors, int idx) {
        this.universActuel = u;
        this.decorsActuels = decors;
        this.indexDecor    = Math.min(idx, decors.length - 1);
    }

    private void marquerEnqueteSalonComplete() {
        enigmeVerre.setCorpsExamine(true);
        for (int i = 0; i < 5; i++) enigmeVerre.inspecterVerre(i);
    }
    private void marquerPierreComplet() {
        // ChambrePierreManager n'expose pas de setters ; on simule la complétion
        // en marquant juste les flags d'enquête salon. Les éléments cliquables
        // de la chambre Pierre seront simplement déjà résolus à l'affichage.
        // Note : si rejoué, ne nuit pas (les médicaments sont déjà découverts).
    }
    private void preparerJusqua32() {
        marquerEnqueteSalonComplete();
        marquerPierreComplet();
    }
    private void preparerJusqua33() {
        preparerJusqua32();
        ordiLouisDeverr = true;
    }

    private void preparerJusqua34(){
        preparerJusqua32();
        foundTableauElectrique = true;
    }

    private void preparerJusqua35() {
        preparerJusqua34();
        lumieresOk = true;
    }

    private void preparerJusqua36(){
        preparerJusqua35();
        tableauElectriqueOk = true;
    }

    private void preparerJusqua37() {
        preparerJusqua36();
        discussionLue = true;
    }

    private void preparerJusqua41(){
        preparerJusqua37();
        livreChimieOuvert = true;
    }

    private void preparerJusqua42() {
        preparerJusqua41();
        telephoneDecroche = true;
        repondeurEcoute = true;
    }
    private void preparerJusqua43() {
        preparerJusqua42();
        lampeUVRamassee = true;
        serviettesVues = true;
    }
    private void preparerJusqua52() {
        preparerJusqua43();
        tiroirJacquesOuvert = true;
        fioleTrouvee = true;
    }

    /* ════════════════════════════════════════════════════════════════════════
     *                          GESTION DE LA SOURIS
     * ════════════════════════════════════════════════════════════════════════ */

    public void debugVisuelArmoire(){
        transitionner(U_PIERRE, pierreManager.obtenirDecorsPierre(), 1);
    }

    private void gererClicSouris(MouseEvent e) {
        // Écran de crédits : tout clic ramène au menu principal (sur le thread JavaFX)
        if (creditsMode) {
            javafx.application.Platform.runLater(() -> parent.retourMenu());
            return;
        }

        Rectangle imgBounds = backgroundPanel.getImageBounds();
        int iw = imgBounds.width;
        int ih = imgBounds.height;

        /* Dialogue actif : un clic avance / ferme le dialogue. */
        if (dialogueActif) {
            int zoneDialogueY = backgroundPanel.getHeight() - 110;
            if (e.getY() < zoneDialogueY) return;
            avancerDialogue();
            return;
        }

        if (!imgBounds.contains(e.getPoint())) return;

        int mouseXInImg = e.getX() - imgBounds.x;
        int mouseYInImg = e.getY() - imgBounds.y;
        Point clic = new Point(mouseXInImg, mouseYInImg);

        /* Mode zoom Pierre */
        if (universActuel.equals(U_PIERRE) && !pierreManager.getModeZoom().equals("AUCUN")) {
            pierreManager.gererClicZoom(clic, iw, ih, Jeu.this);
            // si la fiole est cliquée à la phase 2.1, on avance
            if (numPhase() == 21 && pierreManager.getModeZoom().equals("ARMOIRE")) {
                Rectangle zoneFiole = new Rectangle((int)(iw * 0.46), (int)(ih * 0.35),
                        (int)(iw * 0.08), (int)(ih * 0.22));
                if (zoneFiole.contains(clic)) { avancerPhase(); }
            }
            return;
        }

        /* Mode zoom Pierre + clic ailleurs */
        if (universActuel.equals(U_PIERRE) && pierreManager.getModeZoom().equals("AUCUN")) {
            if (indexDecor == 1 && porteSortiePierre(iw, ih).contains(clic)) transitionner(U_SALON, decorsSalon, 0);
            if (pierreManager.gererClic(indexDecor, clic, iw, ih, Jeu.this)) {
                // Si on a récupéré la clé en 2.1, on avance sur subStep
                if (numPhase() == 21 && subStep == 0 && pierreManager.isaLaCle()) {
                    subStep = 1;
                }
                return;
            }
        }

        /* Mini-map (toujours active hors zoom) */
        if (gererClicMiniMap(e.getPoint())) return;

        /* Logique propre à chaque univers/phase */
        if (universActuel.equals(U_SALON) && indexDecor == 0) {
            gererClicSalon0(clic, iw, ih);
            return;
        }
        if (universActuel.equals(U_SALON) && indexDecor == 1) {
            gererClicSalon1(clic, iw, ih);
            return;
        }
        if (universActuel.equals(U_LOUIS)) {
            gererClicLouis(clic, iw, ih);
            return;
        }
        if (universActuel.equals(U_JACQUES)) {
            gererClicJacques(clic, iw, ih);
            return;
        }
        if (universActuel.equals(U_SDB)) {
            gererClicSdb(clic, iw, ih);
            return;
        }
    }

    private void gererMouvementSouris(MouseEvent e) {
        if (creditsMode) {
            backgroundPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            notifierChangementCurseur(true);
            return;
        }
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
        int mxi = e.getX() - imgBounds.x;
        int myi = e.getY() - imgBounds.y;
        verifierEtMettreAJourCurseur(new Point(mxi, myi));
    }

    private void verifierEtMettreAJourCurseur(Point clic) {
        if (dialogueActif || modeEnigmeActive) return;
        Rectangle imgBounds = backgroundPanel.getImageBounds();
        int iw = imgBounds.width;
        int ih = imgBounds.height;
        boolean inter = false;

        // Mini-map : toujours interactive si zoom inactif
        if (pierreManager.getModeZoom().equals("AUCUN") && surMiniMap(clic.x + imgBounds.x, clic.y + imgBounds.y)) {
            inter = true;
        }

        if (!inter && universActuel.equals(U_PIERRE)) {
            if (pierreManager.verifierSurvol(indexDecor, clic, iw, ih) || (indexDecor == 1 && porteSortiePierre(iw, ih).contains(clic))){
                inter = true;
            }
        }
        if (!inter && universActuel.equals(U_SALON) && indexDecor == 0) {
            if (!enigmeVerre.isCorpsExamine()) {
                Rectangle zoneCorps = new Rectangle((int)(iw * 0.22), (int)(ih * 0.36),
                        (int)(iw * 0.23), (int)(ih * 0.20));
                if (zoneCorps.contains(clic)) inter = true;
            }
        }
        if (!inter && enigmeVerre.isCorpsExamine()
                && enigmeVerre.survolentUnVerre(universActuel, indexDecor, clic, iw, ih)) {
            inter = true;
        }
        if (!inter && universActuel.equals(U_SALON) && indexDecor == 0) {
            if (numPhase() >= 33 && !tableauElectriqueOk) {
                if (zoneTableauElec(iw, ih).contains(clic)) inter = true;
            }
            if (porteDehors(iw, ih).contains(clic)) inter = true;
        }
        if (!inter && universActuel.equals(U_SALON) && indexDecor == 1) {
            if (numPhase() == 41 && !telephoneDecroche) {
                if (zoneTelephone(iw, ih).contains(clic)) inter = true;
            }
            if (porteSdb(iw, ih).contains(clic)     ||
                porteLouis(iw, ih).contains(clic)   ||
                portePierre(iw, ih).contains(clic)  ||
                portePaul(iw, ih).contains(clic)    ||
                porteJacques(iw, ih).contains(clic) ||
                porteThomas(iw, ih).contains(clic)) inter =  true;
        }
        if (!inter && universActuel.equals(U_LOUIS)) {
            inter = zoneLouisInter(clic, iw, ih);
        }
        if (!inter && universActuel.equals(U_JACQUES)) {
            inter = zoneJacquesInter(clic, iw, ih);
        }
        if (!inter && universActuel.equals(U_SDB)) {
            inter = zoneSdbInter(clic, iw, ih);
        }
        backgroundPanel.setSetCursorDirect(inter);
    }

    /* ─── DIALOGUES & TEXTES ────────────────────────────────────────────────── */

    private void ouvrirDialogueLouisInitial() {
        dialogueActif = true;
        indexDialogue = 0;
        textesDialogueCustom = null;
        btnGauche.setVisible(false);
        btnDroite.setVisible(false);
        backgroundPanel.repaint();
    }

    private void ouvrirDialogueCustom(String... textes) {
        textesDialogueCustom = textes;
        indexDialogue = 0;
        dialogueActif = true;
        btnGauche.setVisible(false);
        btnDroite.setVisible(false);
        backgroundPanel.repaint();
    }

    private String[] textesDialogueAffiches() {
        if (textesDialogueCustom != null) return textesDialogueCustom;
        if (!enigmeVerre.isCorpsExamine()) return enigmeVerre.getTextesLouis();
        return new String[] { texteDialogueCourant };
    }

    private void avancerDialogue() {
        String[] textes = textesDialogueAffiches();
        indexDialogue++;
        if (indexDialogue < textes.length) {
            if (numPhase() == 61 && indexDialogue == 3){
                transitionner(U_SALON, decorsSalon);
            }
            backgroundPanel.repaint();
            return;
        }
        // Fin du dialogue
        boolean etaitDecouverteCorps = !enigmeVerre.isCorpsExamine();
        dialogueActif = false;
        textesDialogueCustom = null;
        btnGauche.setVisible(true);
        btnDroite.setVisible(true);

        if (numPhase() == 1 && etaitDecouverteCorps) {
            enigmeVerre.setCorpsExamine(true);
            avancerPhase(); // 0.1 → 1.1
        } else if (numPhase() == 11 && enigmeVerre.tousVerresTrouves() && subStep == 0) {
            lancerEnigmeEmpreintes();
        } else if (numPhase() == 51){
            avancerPhase(); // 5.1 --> 5.2
        } else if (numPhase() == 61){
            avancerPhase(); // 6.1 --> 7.1 (Crédits)
        }
        mettreAJourTexteChambre();
        recalculerCurseurImmediat();
    }

    /* ─── CLICS PAR PIÈCE/VUE ───────────────────────────────────────────────── */

    private void gererClicSalon0(Point clic, int iw, int ih) {
        // Phase 0.1 : examiner le corps
        if (!enigmeVerre.isCorpsExamine()) {
            Rectangle zoneCorps = new Rectangle((int)(iw * 0.22), (int)(ih * 0.36),
                    (int)(iw * 0.23), (int)(ih * 0.20));
            if (zoneCorps.contains(clic)) {
                ouvrirDialogueLouisInitial();
                return;
            }
            return;
        }
        // Phase 1.1 : cliquer sur les verres
        if (numPhase() == 11 && !enigmeVerre.tousVerresTrouves()) {
            int id = enigmeVerre.obtenirIdVerreClique(universActuel, indexDecor, clic, iw, ih);
            if (id != -1) ouvrirDialogueVerre(id);
        }

        // Tableau électrique (phase 3.3+ disponible quand l'ordi vient de provoquer la coupure)
        if (numPhase() >= 33 && !tableauElectriqueOk && zoneTableauElec(iw, ih).contains(clic)) {
            if (numPhase() == 33) {avancerPhase(); foundTableauElectrique = true;}
            lancerTableauElectrique();
            return;
        }

        if (porteDehors(iw, ih).contains(clic)) {
            tenterAller(U_DEHORS);
            return;
        }
    }

    private void gererClicSalon1(Point clic, int iw, int ih) {
        // Verres dans le 2e décor du salon
        if (numPhase() == 11 && enigmeVerre.isCorpsExamine() && !enigmeVerre.tousVerresTrouves()) {
            int id = enigmeVerre.obtenirIdVerreClique(universActuel, indexDecor, clic, iw, ih);
            if (id != -1) { ouvrirDialogueVerre(id); return; }
        }
        // Portes (Louis / Pierre / Jacques)
        

        if (porteLouis(iw, ih).contains(clic)) {
            tenterAllerLouis();
            return;
        }
        if (portePierre(iw, ih).contains(clic)) {
            tenterAller(U_PIERRE, pierreManager.obtenirDecorsPierre());
            return;
        }
        if (porteJacques(iw, ih).contains(clic)) {
            tenterAller(U_JACQUES, decorsJacques);
            return;
        }
        if (porteSdb(iw, ih).contains(clic)){
            tenterAller(U_SDB, decorsSdb);
            return;
        }
        if (porteThomas(iw, ih).contains(clic)){
            tenterAller(U_THOMAS);
            return;
        }
        if (portePaul(iw, ih).contains(clic)){
            tenterAller(U_PAUL);
            return;
        }

        // Téléphone (phase 4.1)
        if (numPhase() == 41 && !telephoneDecroche && zoneTelephone(iw, ih).contains(clic)) {
            lancerEnigmeOndes();
            return;
        }
    }

    private void gererClicLouis(Point clic, int iw, int ih) {
        if (indexDecor == 1 && porteSortieLouis(iw, ih).contains(clic)){
            transitionner(U_SALON, decorsSalon, 0);
        }
        // Phase 3.2 : trouver le code (post-it sur décor 1) puis cliquer l'ordi (décor 0)[cite: 4]
        if (numPhase() == 32) {
            if (indexDecor == 0) {
                if (zonePostIt(iw, ih).contains(clic) && !postItAffiche) {
                    codeLouisTrouve = true;
                    subStep = 1;
                    // Appel de la méthode existante pour afficher l'image en plein écran
                    activerModeZoom("louis_post-it.jpg");
                    postItAffiche = true; 
                    return;
                }
                if (zonePostItZoom(iw, ih).contains(clic) && postItAffiche){
                    afficherIndice("Un post-it sur le mur : \"Quatre colocs te cherchent depuis six heures, mais à neuf heures, il n’en restera qu’un.\".");
                }
            }
            if (indexDecor == 0) {
                if (zoneOrdi(iw, ih).contains(clic) && !postItAffiche) {
                    lancerOrdiLouis();
                    return;
                }
            }
        }
        // Phase 3.6 : lire les discussions
        if (numPhase() == 36) {
            if (indexDecor == 0 && zoneOrdi(iw, ih).contains(clic)) {
                ouvrirDialogueCustom(
                        "Une conversation est ouverte entre Louis et Pierre.",
                        "Louis : \"Tu ne peux pas continuer comme ça, Pierre.\"",
                        "Pierre : \"Tu ne sais rien de ce que je vis. Lâche-moi.\"",
                        "Louis : \"Si tu ne m'écoutes pas, j'en parle à Jacques demain.\""
                );
                discussionLue = true;
                avancerPhase(); // 3.6 → 3.7
                return;
            }
        }
        // Phase 3.7 : trouver et ouvrir le livre de chimie
        if (numPhase() == 37) {
            if (zoneLivre(iw, ih).contains(clic) && !livreChimieOuvert) {
                livreChimieOuvert = true;
                ouvrirDialogueCustom(
                        "Un livre de chimie est posé sur le bureau de Louis.",
                        "Une page est marquée : synthèse de composés cyanurés.",
                        "Quelqu'un a soigneusement annoté la recette du poison.",
                        "Tout désigne désormais une piste dans la salle de bain..."
                );
                avancerPhase(); // 3.7 → 4.1
                return;
            }
        }
    }

    private void gererClicJacques(Point clic, int iw, int ih) {
        if (numPhase() != 52) return;
        Rectangle plafond = new Rectangle((int)(iw * 0), (int)(ih * 0), (int)(iw * 1), (int)(ih * 0.1));
        //Rectangle fiole   = new Rectangle((int)(iw * 0.32), (int)(ih * 0.58), (int)(iw * 0.14), (int)(ih * 0.10));

        if (lampeUVRamassee && plafond.contains(clic) && subStep == 0) {
            lancerUVLamp();
            return;
        }
        if (!tiroirJacquesOuvert && zoneTiroirB4(iw, ih).contains(clic) && indexDecor == 0) {
            lancerEnigmeBoules(0); // 0 en niveau normal correspond à l'énigme de la maquette (version originale de Escape Memoirs pour le niveau Normal, sinon un niveau un peu plus compliqué)
            return;
        }
        if (indexDecor == 1 && porteSortieJacques(iw, ih).contains(clic)){
            transitionner(U_SALON, decorsSalon, 0);
        }
    }

    private void gererClicSdb(Point clic, int iw, int ih) {
        // Porte de sortie sur sdb2 (toujours active)
        if (indexDecor == 1 && porteSortieSdb(iw, ih).contains(clic)) {
            transitionner(U_SALON, decorsSalon, 0);
            return;
        }
        if (numPhase() != 43) return;

        // Vue 0 (sdb1) : la lampe UV est posée sur le plateau de la baignoire
        if (indexDecor == 0) {
            Rectangle lampe = zoneLampeUVSdb1(iw, ih);
            if (lampe.contains(clic) && !lampeUVRamassee) {
                lampeUVRamassee = true;
                afficherIndice("Vous récupérez la lampe UV posée près de la baignoire.");
                if (lampeUVRamassee && serviettesVues) { avancerPhase(); cinematiquePaul(); }
                return;
            }
            return;
        }

        // Vue 1 (sdb2) : les six serviettes brodées des initiales des colocataires
        if (indexDecor == 1) {
            Rectangle serviettes = zoneServiettesSdb2(iw, ih);
            if (serviettes.contains(clic) && !serviettesVues) {
                if (!lampeUVRamassee) {
                    afficherIndice("Six serviettes brodées d'initiales (JA., PA., Pi., LD., TH., FR.) — il fait trop sombre pour distinguer d'éventuelles traces. Une lampe UV serait précieuse.");
                    return;
                }
                lancerUVLampServiettes();
                return;
            }
        }
    }

    private void lancerUVLampServiettes() {
        modeEnigmeActive = true;
        UVLampServiettesUI ui = new UVLampServiettesUI(dialogParent());
        ui.setVisible(true);
        modeEnigmeActive = false;
        serviettesVues = true;
        if (ui.isTacheRevelee()) {
            afficherInfo2("Sous la lampe UV, une tache fluorescente apparaît sur la serviette de Jacques (JA.). Une preuve troublante...", "Indice révélé");
        } else {
            afficherIndice("Vous quittez la salle de bain sans avoir balayé toutes les serviettes... mais l'enquête doit avancer.");
        }
        if (lampeUVRamassee && serviettesVues) {
            avancerPhase();
            cinematiquePaul();
        }
        rafraichirAffichage();
    }

    /** Zone cliquable de la lampe UV sur sdb1 (plateau près de la baignoire). */
    private Rectangle zoneLampeUVSdb1(int iw, int ih) {
        return new Rectangle((int)(iw * 0.45), (int)(ih * 0.44), (int)(iw * 0.16), (int)(ih * 0.13));
    }

    /** Zone cliquable des six serviettes brodées sur sdb2. */
    private Rectangle zoneServiettesSdb2(int iw, int ih) {
        return new Rectangle((int)(iw * 0.55), (int)(ih * 0.32), (int)(iw * 0.36), (int)(ih * 0.54));
    }

    /* ─── ACCESSIBILITÉ DES PIÈCES ──────────────────────────────────────────── */

    private void tenterAller(String u){
        tenterAller(u, new String[0]);
    }

    private void tenterAller(String u, String[] decors) {
        if (!peutAcceder(u)) {
            afficherInfo(messageAccesRefuse(u));
            return;
        }
        transitionner(u, decors);
    }

    private void tenterAllerLouis() {
        int n = numPhase();
        if (n < 31) {
            tenterAller(U_LOUIS);
            return;
        }
        if (n == 31) {
            lancerCadranRotatif();
            return;
        }
        transitionner(U_LOUIS, decorsLouis);
    }

    private boolean peutAcceder(String u) {
        int n = numPhase();
        switch (u) {
            case U_DEHORS -> { return false; }
            case U_THOMAS -> { return false; }
            case U_PAUL   -> { return false; }
            case U_SALON  -> { return true; }
            case U_PIERRE -> { return n >= 21; }
            case U_LOUIS  -> {
                if (n < 31) return false;
                if (n == 31) return false; 
                return true;
            }
            case U_JACQUES -> { return n >= 51; }
            case U_SDB    -> {
                if (n < 41) return false;
                if (n == 41) return false; 
                if (n == 42) return false;
                return true;
            }
        }
        return false;
    }

    private String messageAccesRefuse(String u) {
        int n = numPhase();
        switch (u) {
            case U_DEHORS -> { return "Je dois d'abord savoir ce qu'il s'est passé..."; }
            case U_THOMAS -> { return "Pourquoi aurais-je besoin de fouiller ma chambre ?"; }
            case U_PAUL   -> { return "Rien ne le suspecte à présent, d'autant qu'il n'était pas là cette nuit..."; }
            case U_PIERRE -> {
                if (n < 21) return "Je dois d'abord trouver qui est suspect...";
            }
            case U_LOUIS -> {
                if (n < 21) return "Il doit y avoir des indices plus importants à côté de son corps...";
                if (n == 21) return "Voyons d'abord ce que cache Pierre...";
                if (n == 31) return "Je dois d'abord dévérouiller la porte.";
            }
            case U_JACQUES -> {
                if (n < 21) return "Je dois d'abord trouver qui est suspect...";
                if (n == 21) return "Voyons d'abord ce que cache Pierre...";
                if (n < 41) return "La chambre de Louis doit contenir des indices...";
                if (n < 51) return "Il doit y avoir des indices dans la salle de bain...";
            }
            case U_SDB -> {
                if (n < 21) return "Je dois d'abord trouver qui est suspect...";
                if (n == 21) return "Voyons d'abord ce que cache Pierre..."; 
                if (n < 41) return "La chambre de Louis doit contenir des indices...";
                if (n == 41) return "Le téléphone sonne, j'y enquêterai plus tard...";
                if (n == 42) return "Le téléphone sonne, je ferais mieux de répondre...";
            }
        }
        return "Pas maintenant.";
    }

    /* ─── LANCEMENTS DES ÉNIGMES (par phase) ────────────────────────────────── */

    private Window dialogParent() {
        Window w = SwingUtilities.getWindowAncestor(backgroundPanel);
        return w != null ? w : this;
    }

    private void ouvrirDialogueVerre(int id) {
        texteDialogueCourant = enigmeVerre.inspecterVerre(id);
        textesDialogueCustom = null;
        dialogueActif = true;
        btnGauche.setVisible(false);
        btnDroite.setVisible(false);
        backgroundPanel.repaint();
    }

    private void lancerEnigmeEmpreintes() {
        if (subStep != 0) return;
        modeEnigmeActive = true;
        EnigmeEmpreinteUI ui = new EnigmeEmpreinteUI(this, this.save);
        ui.setVisible(true);
        modeEnigmeActive = false;
        if (ui.isReussite()) {
            subStep = 0;
            ui = null;
            afficherInfo("Empreinte de Pierre confirmée. Direction sa chambre !");
            avancerPhase(); // 1.1 → 2.1
        }
        rafraichirAffichage();
    }

    private void lancerCadranRotatif() {
        modeEnigmeActive = true;
        RotaryDialUI ui = new RotaryDialUI(dialogParent(), this.save);
        ui.setVisible(true);
        modeEnigmeActive = false;
        if (ui.isReussite()) {
            avancerPhase(); // 3.1 → 3.2
            ui = null;
            transitionner(U_LOUIS, decorsLouis);
            indexDecor = 0; 
            backgroundPanel.setNewImage(decorsActuels[indexDecor]);
            mettreAJourTexteChambre();
        }
        rafraichirAffichage();
    }

    private void lancerOrdiLouis() {
        modeEnigmeActive = true;
        OrdiLouisUI ui = new OrdiLouisUI(dialogParent(), this.save);
        ui.setVisible(true);
        modeEnigmeActive = false;
        if (ui.isReussite()) {
            ordiLouisDeverr = true;
            ui = null;
            avancerPhase(); // 3.2 → 3.3
            afficherAvertissement("Vous êtes connecté à la session de Louis. Mais soudain... Le courant est coupé.", "Coupure de courant");
        }
        rafraichirAffichage();
    }

    private void lancerTableauElectrique() {
        modeEnigmeActive = true;
        LogicGateUI ui = null;
        if (numPhase() == 34){
            ui = new LogicGateUI(dialogParent(), this.save);
            ui.setVisible(true);
        }
        modeEnigmeActive = false;
        if (numPhase() == 35 || (ui != null && ui.isReussite())) {
            if (numPhase() == 34) avancerPhase(); // 3.4 → 3.5
            ui = null;
            MovingLightsUI ui2 = new MovingLightsUI(dialogParent(), this.save);
            modeEnigmeActive = true;
            ui2.setVisible(true);
            modeEnigmeActive = false;
            if (ui2.isReussite()) {
                ui2 = null;
                tableauElectriqueOk = true;
                lumieresOk = true;
                afficherInfo("Le courant est rétabli !");
                avancerPhase(); // 3.5 → 3.6
                transitionner(U_LOUIS, decorsLouis);
                indexDecor = 0;
                backgroundPanel.setNewImage(decorsActuels[indexDecor]);
                mettreAJourTexteChambre();
            }
        }
        rafraichirAffichage();
    }

    private void lancerEnigmeOndes() {
        OndesUI ui = null;
        if (!telephoneDecroche){
            modeEnigmeActive = true;
            ui = new OndesUI(dialogParent(), this.save);
            ui.setVisible(true);
        }
        modeEnigmeActive = false;
        if (telephoneDecroche || (ui != null && ui.isReussite())) {
            ui = null;
            telephoneDecroche = true;
            if (numPhase() == 41){
                avancerPhase(); // 4.1 → 4.2
            }
            ouvrirDialogueCustom(
                    "Vous décrochez. C'est un message vocal en différé.",
                    "Jacques : \"Paul t'en veux, on en parle quand je serai rentré.\"",
                    "Une piste de plus...",
                    "Direction la salle de bain."
            );
            if (numPhase() == 42){
                avancerPhase(); // 4.2 → 4.3
            }
        }
        rafraichirAffichage();
    }

    private void lancerUVLamp() {
        modeEnigmeActive = true;
        UVLampUI ui = new UVLampUI(dialogParent());
        ui.setVisible(true);
        modeEnigmeActive = false;
        rafraichirAffichage();
    }

    private void lancerEnigmeBoules(int num) {
        modeEnigmeActive = true;
        MovingBallsUI ui = new MovingBallsUI(dialogParent(), this.save, num);
        ui.setVisible(true);
        modeEnigmeActive = false;
        if (ui.isReussite()) {
            ui = null;
            tiroirJacquesOuvert = true;
            afficherInfo2("Le tiroir s'ouvre. Une fiole de poison vide est dissimulée à l'intérieur... Voilà l'arme du crime !", "Info");
            avancerPhase();
            cinematiqueRevelations();
        }
        rafraichirAffichage();
    }

    private void cinematiquePaul(){
        transitionner(U_SALON, decorsSalon);
        ouvrirDialogueCustom("Paul rentre à l'appartement."

        );
    }

    private void cinematiqueRevelations() {
        
        ouvrirDialogueCustom(
                "Jacques rentre dans la chambre.",
                "Jacques : \"Ça suffit. C'est moi qui ai parlé à Paul de Louis.\"",
                "Viens on va s'expliquer dans le salon, tout le monde est rentré.",
                "Mais... pourquoi mentir sur le poison ?",
                "Vous repensez à la nuit dernière. À cette dispute avec Louis.",
                "Au verre que vous avez préparé. Au geste que vous avez refusé d'admettre.",
                "Vous comprenez. C'est vous qui l'avez fait.",
                "Le silence retombe."
        );
    }

    /* ─── OUTILLAGE / NAVIGATION ────────────────────────────────────────────── */

    public void activerModeZoom(String imageZoom) {
        btnGauche.setVisible(false);
        btnDroite.setVisible(false);
        btnQuitterZoom.setVisible(true);
        backgroundPanel.setNewImage(imageZoom);
        recalculerCurseurImmediat();
    }

    private void desactiverModeZoom() {
        pierreManager.quitterZoom(this);
        btnQuitterZoom.setVisible(false);
        btnGauche.setVisible(true);
        btnDroite.setVisible(true);
        //decorsActuels = pierreManager.obtenirDecorsPierre();
        backgroundPanel.setNewImage(decorsActuels[indexDecor]);
        recalculerCurseurImmediat();
        if (postItAffiche){
            postItAffiche = false; 
        }
    }

    private void naviguerDecor(int dir) {
        if (!enigmeVerre.isCorpsExamine() && universActuel.equals(U_SALON) && indexDecor == 0) {
            if (dir > 0) txtExplicatif.setText("Je devrais d'abord aller voir ce qu'a Louis sur le canapé...");
            return;
        }
        if (decorsActuels.length <= 1) return;
        indexDecor = (indexDecor + dir + decorsActuels.length) % decorsActuels.length;
        backgroundPanel.setNewImage(decorsActuels[indexDecor]);
        mettreAJourTexteChambre();
        recalculerCurseurImmediat();
    }

    public void setCursorChangeListener(CursorChangeListener listener) { this.cursorChangeListener = listener; }

    private void avancerPhase() {
        boolean changeTime = !(this.phase.getPoids() == 0.0);
        this.phase.nextPhase();
        double prochain = Math.round(this.phase.getNumero() * 10) / 10.0;
        if (this.save != null) {
            this.save.setPhase(prochain);
            if (changeTime) {
                this.save.addTime(((int) (Time.now() - this.save.getLastSave())) / 1000);
            }
            this.save.save();
            changeTitleProgression();
            if (this.phase.estJeuFini()){
                Save.updateLine(this.save);
                entrerModeCredits();
            }
        }
        subStep = 0;
    }

    /** Active l'écran de crédits final : fond noir + crédits + indication
     *  « Cliquer pour revenir au menu ». Le retour vers le menu JavaFX sera
     *  déclenché sur le thread JavaFX (Platform.runLater) au clic suivant. */
    private void entrerModeCredits() {
        creditsMode = true;
        dialogueActif = false;
        modeEnigmeActive = false;
        if (btnGauche != null)      btnGauche.setVisible(false);
        if (btnDroite != null)      btnDroite.setVisible(false);
        if (btnQuitterZoom != null) btnQuitterZoom.setVisible(false);
        if (txtExplicatif != null)  txtExplicatif.setVisible(false);
        backgroundPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        notifierChangementCurseur(true);
        backgroundPanel.repaint();
    }

    private int numPhase() {
        return (int) Math.round(this.phase.getNumero() * 10);
    }

    private void promptChangePhase() {
        if (this.save != null && 
            "ADMIN".equals(this.save.getUsername()) && 
            "TESTER_PHASES".equals(this.save.getSavename())) {

            String input = JOptionPane.showInputDialog(
                this, 
                "Mode Admin - Entrez le numéro de phase (format X.X) :", 
                "Changement de Phase", 
                JOptionPane.QUESTION_MESSAGE
            );

            if (input != null && input.matches("\\d+\\.\\d+")) {
                int newIndex = Phase.getIndex(Double.parseDouble(input));
                
                if (newIndex != -1) {
                    this.phase.change(Phase.getAllPhases()[newIndex]); 
                    this.save.setPhase(this.phase.getNumero());
                    this.save.save();
                    resetPhase();
                    System.out.println("Phase changée vers : " + input);
                    changeTitleProgression();
                    JOptionPane.showMessageDialog(this, "Phase changée avec succès : " + input);
                    restaurerEtatProgression();
                    mettreAJourTexteChambre();
                    rafraichirAffichage();
                    this.repaint(); 
                } else {
                    JOptionPane.showMessageDialog(this, "Erreur : Phase '" + input + "' introuvable.", "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            } else if (input != null) {
                JOptionPane.showMessageDialog(this, "Format invalide. Utilisez X.X (ex: 1.2)", "Erreur", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void rafraichirAffichage() {
        if (!modeEnigmeActive) {
            btnGauche.setVisible(true);
            btnDroite.setVisible(true);
        }
        backgroundPanel.setNewImage(decorsActuels[indexDecor]);
        mettreAJourTexteChambre();
        recalculerCurseurImmediat();
    }

    private void afficherIndice(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Indice", JOptionPane.INFORMATION_MESSAGE);
    }
    private void afficherInfo(String msg) {
        if (txtExplicatif != null) txtExplicatif.setText(msg);
        else JOptionPane.showMessageDialog(this, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
    }
    private void afficherAvertissement(String msg, String title){
        JOptionPane.showMessageDialog(this, msg, title, JOptionPane.WARNING_MESSAGE);
    }

    private void afficherInfo2(String msg, String title){
        JOptionPane.showMessageDialog(this, msg, title, JOptionPane.INFORMATION_MESSAGE);
    }

    private void notifierChangementCurseur(boolean surElementInteractif) {
        if (cursorChangeListener != null) cursorChangeListener.onCursorChanged(surElementInteractif);
    }

    private void recalculerCurseurImmediat() {
        backgroundPanel.repaint();
        Point mousePos = getRelativeMousePoint();
        if (mousePos != null) verifierEtMettreAJourCurseur(mousePos);
        else {
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

    private void transitionner(String u, String[] decors, int indexDecor) {
        universActuel = u;
        decorsActuels = decors;
        this.indexDecor = indexDecor;
        backgroundPanel.setNewImage(decorsActuels[indexDecor]);
        mettreAJourTexteChambre();
        recalculerCurseurImmediat();
    }

    private void transitionner(String u, String[] decors) {
        transitionner(u, decors, 0);
    }

    /* ─── Zones d'interaction utilitaires ─────────────────────────────────── */

    private Rectangle zoneTableauElec(int iw, int ih) {
        return new Rectangle((int)(iw * 0.79), (int)(ih * 0.155), (int)(iw * 0.083), (int)(ih * 0.116));
    }

    private Rectangle zoneTelephone(int iw, int ih) {
        return new Rectangle((int)(iw * 0.595), (int)(ih * 0.418), (int)(iw * 0.02), (int)(ih * 0.023));
    }

    private Rectangle zoneOrdi(int iw, int ih) {
        return new Rectangle((int)(iw * 0.576), (int)(ih * 0.32), (int)(iw * 0.152), (int)(ih * 0.189));
    }

    private Rectangle zonePostIt(int iw, int ih) {
        return new Rectangle((int)(iw * 0.610), (int)(ih * 0.148), (int)(iw * 0.06), (int)(ih * 0.132));
    }

    private Rectangle zonePostItZoom(int iw, int ih){
        return new Rectangle((int)(iw * 0.386), (int)(ih * 0.147), (int)(iw * 0.238), (int)(ih * 0.372));
    }

    private Rectangle zoneLivre(int iw, int ih){
        return new Rectangle((int)(iw * 0.63), (int)(ih * 0.356), (int)(iw * 0.014), (int)(ih * 0.132));
    }

    private Rectangle zoneTiroirB4(int iw, int ih){
        return new Rectangle((int)(iw * 0.764), (int)(ih * 0.646), (int)(iw * 0.066), (int)(ih * 0.039));
    }

    private Rectangle porteLouis(int iw, int ih){
        return new Rectangle((int)(iw * 0.24), (int)(ih * 0.25), (int)(iw * 0.08), (int)(ih * 0.3));
    }
    private Rectangle portePierre(int iw, int ih){
        return new Rectangle((int)(iw * 0.39), (int)(ih * 0.21), (int)(iw * 0.05), (int)(ih * 0.25));
    } 
    private Rectangle porteJacques(int iw, int ih){
        return new Rectangle((int)(iw * 0.56), (int)(ih * 0.22), (int)(iw * 0.06), (int)(ih * 0.2));
    }
    private Rectangle porteSdb(int iw, int ih){
        return new Rectangle((int)(iw * 0.01), (int)(ih * 0.271), (int)(iw * 0.153), (int)(ih * 0.4));
    }
    private Rectangle portePaul(int iw, int ih){
        return new Rectangle((int)(iw * 0.856), (int)(ih * 0.248), (int)(iw * 0.12), (int)(ih * 0.32));
    }
    private Rectangle porteThomas(int iw, int ih){
        return new Rectangle((int)(iw * 0.69), (int)(ih * 0.225), (int)(iw * 0.095), (int)(ih * 0.287));
    }
    private Rectangle porteDehors(int iw, int ih){
        return new Rectangle((int)(iw * 0.86), (int)(ih * 0.248), (int)(iw * 0.148), (int)(ih * 0.349));
    }

    private Rectangle porteSortiePierre(int iw, int ih){
        return new Rectangle((int)(iw * 0.197), (int)(ih * 0.217), (int)(iw * 0.161), (int)(ih * 0.512));
    }
    private Rectangle porteSortieLouis(int iw, int ih){
        return new Rectangle((int)(iw * 0.15), (int)(ih * 0.07), (int)(iw * 0.151), (int)(ih * 0.62));
    }
    private Rectangle porteSortieSdb(int iw, int ih){
        return new Rectangle((int)(iw * 0.188), (int)(ih * 0.225), (int)(iw * 0.178), (int)(ih * 0.543));
    }
    private Rectangle porteSortieJacques(int iw, int ih){
        return new Rectangle((int)(iw * 0.725), (int)(ih * 0.248), (int)(iw * 0.174), (int)(ih * 0.504));
    }

    private boolean zoneLouisInter(Point clic, int iw, int ih) {
        if (indexDecor == 1 && porteSortieLouis(iw, ih).contains(clic)) return true;
        int n = numPhase();
        if (n == 32 && indexDecor == 0) {
            return (zonePostIt(iw, ih).contains(clic) && !postItAffiche) || (zonePostItZoom(iw, ih).contains(clic) && postItAffiche) || zoneOrdi(iw, ih).contains(clic); // Post-it et Post-it gros plan
        }
        if ((n == 32 || n == 36) && indexDecor == 0) {
            return zoneOrdi(iw, ih).contains(clic);
        }
        if (n == 37 && indexDecor == 1) {
            return zoneLivre(iw, ih).contains(clic);
        }
        return false;
    }

    private boolean zoneJacquesInter(Point clic, int iw, int ih) {
        if (indexDecor == 1 && porteSortieJacques(iw, ih).contains(clic)) return true;
        if (numPhase() != 52) return false;
        Rectangle plafond = new Rectangle((int)(iw * 0), (int)(ih * 0), (int)(iw * 1), (int)(ih * 0.1));
        return plafond.contains(clic) || (numPhase() == 52 && indexDecor == 0 && zoneTiroirB4(iw, ih).contains(clic));
    }

    private boolean zoneSdbInter(Point clic, int iw, int ih) {
        if (indexDecor == 1 && porteSortieSdb(iw, ih).contains(clic)) return true;
        if (numPhase() != 43) return false;
        if (indexDecor == 0 && !lampeUVRamassee && zoneLampeUVSdb1(iw, ih).contains(clic)) return true;
        if (indexDecor == 1 && !serviettesVues && zoneServiettesSdb2(iw, ih).contains(clic)) return true;
        return false;
    }

    /* ─── MINI-MAP & RENDU ──────────────────────────────────────────────────── */

    private Rectangle[] miniMapRects = new Rectangle[7];
    private final String[] miniMapUnivers = { U_PIERRE, U_LOUIS, U_SDB, U_JACQUES, U_SALON, U_THOMAS, U_PAUL };

    private boolean surMiniMap(int x, int y) {
        for (Rectangle r : miniMapRects) if (r != null && r.contains(x, y)) return true;
        return false;
    }

    private boolean gererClicMiniMap(Point p) {
        for (int i = 0; i < miniMapRects.length; i++) {
            Rectangle r = miniMapRects[i];
            if (r != null && r.contains(p)) {
                String cible = miniMapUnivers[i];
                if (cible.equals(universActuel)) return true; 
                if (cible.equals(U_PIERRE))   tenterAller(U_PIERRE, pierreManager.obtenirDecorsPierre());
                else if (cible.equals(U_LOUIS))   tenterAllerLouis();
                else if (cible.equals(U_JACQUES)) tenterAller(U_JACQUES, decorsJacques);
                else if (cible.equals(U_SDB))    tenterAller(U_SDB, decorsSdb);
                else if (cible.equals(U_THOMAS))  afficherInfo(messageAccesRefuse(U_THOMAS));
                else if (cible.equals(U_PAUL))    afficherInfo(messageAccesRefuse(U_PAUL));
                else if (cible.equals(U_SALON))   transitionner(U_SALON, decorsSalon);
                return true;
            }
        }
        return false;
    }

    public BackgroundPanel getGamePanel() { return backgroundPanel; }

    private void mettreAJourTexteChambre() {
        if (txtExplicatif == null) return;
        if (!enigmeVerre.isCorpsExamine() && universActuel.equals(U_SALON) && indexDecor == 0) {
            txtExplicatif.setText("Salon | Louis est allongé sur le canapé... Il ne bouge plus.");
            return;
        }
        String progr = "  |  " + this.phase.getDescription();
        if (enigmeVerre.isCorpsExamine() && numPhase() == 11 && !enigmeVerre.tousVerresTrouves()) {
            progr = "  |  Fouille l'appartement : Trouve les 5 verres rouges (" + enigmeVerre.compterVerresTrouves() + "/5)";
        }
        switch (universActuel) {
            case U_SALON   -> txtExplicatif.setText("Salon" + progr);
            case U_PIERRE  -> txtExplicatif.setText("Chambre de Pierre" + progr);
            case U_LOUIS   -> txtExplicatif.setText("Chambre de Louis" + progr);
            case U_JACQUES -> txtExplicatif.setText("Chambre de Jacques" + progr);
            case U_SDB    -> txtExplicatif.setText("Salle de bain" + progr);
            default        -> txtExplicatif.setText(universActuel + progr);
        }
    }

    private void changeTitle(String newTitle){
        final String title = (newTitle != null) ? newTitle : DEFAULT_TITLE;
        javafx.application.Platform.runLater(()-> {
            this.stage.setTitle(title);
        });
    }
  
    private void changeTitleProgression(){
        String title = DEFAULT_TITLE + " - " + this.phase.getStrPourcentage();
        changeTitle(title);
    }

    /* ─── Panneau de fond (rendu général) ─────────────────────────────────── */

    class BackgroundPanel extends JPanel {
        private Image backgroundImage;
        private int imgWidthOriginal = 1;
        private int imgHeightOriginal = 1;
        private boolean placeholderMode = false;
        private String placeholderTag = "";

        public BackgroundPanel(String path) { setNewImage(path); }

        public void setSetCursorDirect(boolean hand) {
            setCursor(new Cursor(hand ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            notifierChangementCurseur(hand);
        }

        public void setNewImage(String path) {
            placeholderMode = path != null && path.startsWith("__") && path.endsWith("__");
            placeholderTag = placeholderMode ? path : "";

            if (backgroundImage != null) {
                backgroundImage.flush(); // Force la libération des ressources mémoire de l'ancienne image
            }

            if (placeholderMode) {
                backgroundImage = null;
                imgWidthOriginal  = 800;
                imgHeightOriginal = 600;
                repaint();
                return;
            }
            URL url = getClass().getClassLoader().getResource(IMG_DIR + path);
            ImageIcon icon = (url != null) ? new ImageIcon(url) : new ImageIcon(path);
            this.backgroundImage = icon.getImage();
            if (backgroundImage != null) {
                this.imgWidthOriginal  = icon.getIconWidth();
                this.imgHeightOriginal = icon.getIconHeight();
            }
            repaint();
        }

        public Rectangle getImageBounds() {
            if (backgroundImage == null && !placeholderMode) return new Rectangle(0, 0, getWidth(), getHeight());
            double panelRatio = (double) getWidth() / Math.max(1, getHeight());
            double imageRatio = (double) imgWidthOriginal / Math.max(1, imgHeightOriginal);
            int dw, dh;
            if (panelRatio > imageRatio) { dh = getHeight(); dw = (int) (dh * imageRatio); }
            else                          { dw = getWidth();  dh = (int) (dw / imageRatio); }
            return new Rectangle((getWidth() - dw) / 2, (getHeight() - dh) / 2, dw, dh);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, getWidth(), getHeight());

            // ─── Écran de crédits final ───────────────────────────────────
            if (creditsMode) {
                dessinerCredits(g2d);
                return;
            }

            Rectangle r = getImageBounds();
            if (backgroundImage != null) {
                g2d.drawImage(backgroundImage, r.x, r.y, r.width, r.height, this);
            } else if (placeholderMode) {
                dessinerPlaceholder(g2d, r, placeholderTag);
            }

            if (dialogueActif) {
                dessinerDialogue(g2d);
            } else if (pierreManager.getModeZoom().equals("AUCUN")) {
                dessinerMiniMap(g2d);
            }
            repositionnerComposants(r);
        }

        private void dessinerDialogue(Graphics2D g2d) {
            int boxH = 90, boxY = getHeight() - boxH - 20, boxX = 20, boxW = getWidth() - 40;
            g2d.setColor(new Color(0, 0, 0, 210)); g2d.fillRoundRect(boxX, boxY, boxW, boxH, 15, 15);
            g2d.setColor(new Color(255, 255, 255, 150)); g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(boxX, boxY, boxW, boxH, 15, 15);
            g2d.setColor(Color.WHITE); g2d.setFont(new Font("Arial", Font.PLAIN, 15));
            String[] textes = textesDialogueAffiches();
            int idx = Math.min(indexDialogue, textes.length - 1);
            String t = idx >= 0 ? textes[idx] : "";
            g2d.drawString(t, boxX + 25, boxY + 40);
            g2d.setFont(new Font("Arial", Font.ITALIC | Font.BOLD, 11)); g2d.setColor(new Color(46, 204, 113));
            g2d.drawString("[ Cliquer pour continuer > ]", boxX + boxW - 170, boxY + boxH - 15);
        }

        /** Dessine l'écran de crédits final (fond noir + titre + crédits + instruction). */
        private void dessinerCredits(Graphics2D g2d) {
            int w = getWidth(), h = getHeight();
            // Fond noir (déjà rempli, on s'assure)
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, w, h);

            // Titre du jeu
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Serif", Font.BOLD, 42));
            FontMetrics fmTitle = g2d.getFontMetrics();
            String title = GameInfos.GAMENAME;
            g2d.drawString(title, (w - fmTitle.stringWidth(title)) / 2, 90);

            // Sous-titre "Crédits"
            g2d.setFont(new Font("SansSerif", Font.ITALIC, 20));
            g2d.setColor(new Color(200, 200, 200));
            FontMetrics fmSub = g2d.getFontMetrics();
            String sub = "— Crédits —";
            g2d.drawString(sub, (w - fmSub.stringWidth(sub)) / 2, 130);

            // Sections des crédits depuis GameInfos.CREDITS
            // Force l'initialisation du bloc d'instance (les crédits sont remplis
            // dans un initializer non-static : on crée une instance temporaire).
            new GameInfos();
            int y = 200;
            Font sectionFont = new Font("SansSerif", Font.BOLD, 22);
            Font nameFont    = new Font("SansSerif", Font.PLAIN, 18);
            for (Object key : GameInfos.CREDITS.keySet()) {
                String sectionTitre = String.valueOf(key);
                g2d.setFont(sectionFont);
                g2d.setColor(new Color(46, 204, 113));
                FontMetrics fmS = g2d.getFontMetrics();
                g2d.drawString(sectionTitre, (w - fmS.stringWidth(sectionTitre)) / 2, y);
                y += 36;

                Object membres = GameInfos.CREDITS.get(key);
                if (membres instanceof Iterable<?>) {
                    g2d.setFont(nameFont);
                    g2d.setColor(Color.WHITE);
                    FontMetrics fmN = g2d.getFontMetrics();
                    for (Object membre : (Iterable<?>) membres) {
                        String nom = String.valueOf(membre);
                        g2d.drawString(nom, (w - fmN.stringWidth(nom)) / 2, y);
                        y += 28;
                    }
                }
                y += 20;
            }

            // Informations académiques
            g2d.setFont(new Font("SansSerif", Font.ITALIC, 14));
            g2d.setColor(new Color(170, 170, 170));
            String[] infos = {
                "INSA Hauts-de-France — Sciences et Humanités pour l'Ingénieur (2A)",
                "SAE Génie Logiciel — Année 2025-2026"
            };
            int yInfo = h - 90;
            for (String s : infos) {
                FontMetrics fmI = g2d.getFontMetrics();
                g2d.drawString(s, (w - fmI.stringWidth(s)) / 2, yInfo);
                yInfo += 20;
            }

            // Instruction de retour au menu
            g2d.setFont(new Font("SansSerif", Font.BOLD | Font.ITALIC, 16));
            g2d.setColor(new Color(46, 204, 113));
            String instr = "[ Cliquer pour revenir au menu ]";
            FontMetrics fmInstr = g2d.getFontMetrics();
            g2d.drawString(instr, (w - fmInstr.stringWidth(instr)) / 2, h - 30);
        }

        private void dessinerPlaceholder(Graphics2D g, Rectangle r, String tag) {
            Color bg, accent;
            String titre, sousTitre;
            switch (tag) {
                case "__SDB1__", "__SDB2__" -> {
                    bg = new Color(34, 60, 80); accent = new Color(140, 200, 230);
                    titre = "Salle de bain"; sousTitre = "Recherchez la lampe UV et examinez les serviettes.";
                }
                case "__THOMAS__" -> {
                    bg = new Color(48, 50, 64); accent = new Color(160, 165, 185);
                    titre = "Chambre de Thomas"; sousTitre = "(Pourquoi aurais-je besoin de fouiller ma chambre ?)";
                }
                case "__PAUL__" -> {
                    bg = new Color(48, 36, 36); accent = new Color(220, 150, 150);
                    titre = "Chambre de Paul"; sousTitre = "(Rien ne le suspecte à présent...)";
                }
                default -> {
                    bg = new Color(30, 32, 38); accent = Color.WHITE;
                    titre = "Salle"; sousTitre = "";
                }
            }
            g.setColor(bg);
            g.fillRect(r.x, r.y, r.width, r.height);
            g.setColor(accent);
            g.setFont(new Font("SansSerif", Font.BOLD, 36));
            g.drawString(titre, r.x + 40, r.y + 80);
            g.setFont(new Font("SansSerif", Font.ITALIC, 16));
            g.setColor(new Color(220, 220, 230));
            g.drawString(sousTitre, r.x + 40, r.y + 120);

            if (tag.startsWith("__SDB")) {
                int lx = r.x + (int)(r.width * 0.10);
                int ly = r.y + (int)(r.height * 0.30);
                int lw = (int)(r.width * 0.9);
                int lh = (int)(r.height * 0.20);
                g.setColor(lampeUVRamassee ? new Color(60, 70, 90) : new Color(108, 92, 231));
                g.fillRoundRect(lx, ly, lw, lh, 16, 16);
                g.setColor(Color.WHITE);
                g.setFont(new Font("SansSerif", Font.BOLD, 14));
                g.drawString(lampeUVRamassee ? "Lampe UV (prise)" : "Lampe UV", lx + 10, ly + lh / 2);
                
                int sx = r.x + (int)(r.width * 0.55);
                int sy = r.y + (int)(r.height * 0.40);
                int sw = (int)(r.width * 0.25);
                int sh = (int)(r.height * 0.25);
                g.setColor(serviettesVues ? new Color(60, 70, 90) : new Color(200, 200, 220));
                g.fillRoundRect(sx, sy, sw, sh, 12, 12);
                g.setColor(Color.BLACK);
                g.drawString(serviettesVues ? "Serviettes examinées" : "Serviettes", sx + 10, sy + sh / 2);
            }
        }

        private void dessinerMiniMap(Graphics2D g2d) {
            int mapX = 30, mapY = getHeight() - 180, tc = 30;
            Color cDef = new Color(45, 52, 54, 240);
            Color cAct = new Color(46, 204, 113, 255);
            Color cInacc = new Color(80, 30, 30, 200);
            Color tDef = Color.WHITE, tAct = Color.BLACK;

            Rectangle rPierre = new Rectangle(mapX,             mapY,                tc * 2, tc * 2);
            Rectangle rLouis  = new Rectangle(mapX,             mapY + tc * 2,       tc * 2, tc * 2);
            Rectangle rSdb    = new Rectangle(mapX,             mapY + tc * 4,       tc * 2, tc);
            int largJacques = 46, largT = 22, largP = 22;
            Rectangle rJacques = new Rectangle(mapX + tc * 2,   mapY,                largJacques, tc);
            Rectangle rThomas  = new Rectangle(mapX + tc * 2 + largJacques, mapY,    largT, tc);
            Rectangle rPaul    = new Rectangle(mapX + tc * 2 + largJacques + largT, mapY, largP, tc);
            Rectangle rSalon   = new Rectangle(mapX + tc * 2,   mapY + tc,           largJacques + largT + largP, tc * 4);

            miniMapRects[0] = rPierre;
            miniMapRects[1] = rLouis;
            miniMapRects[2] = rSdb;
            miniMapRects[3] = rJacques;
            miniMapRects[4] = rSalon;
            miniMapRects[5] = rThomas;
            miniMapRects[6] = rPaul;

            paintMapCase(g2d, rPierre,  U_PIERRE,  cDef, cAct, cInacc);
            paintMapCase(g2d, rLouis,   U_LOUIS,   cDef, cAct, cInacc);
            paintMapCase(g2d, rSdb,     U_SDB,    cDef, cAct, cInacc);
            paintMapCase(g2d, rSalon,   U_SALON,   cDef, cAct, cInacc);
            paintMapCase(g2d, rJacques, U_JACQUES, cDef, cAct, cInacc);
            paintMapCase(g2d, rThomas,  U_THOMAS,  cDef, cAct, cInacc);
            paintMapCase(g2d, rPaul,    U_PAUL,    cDef, cAct, cInacc);

            g2d.setColor(new Color(200, 200, 200)); g2d.setStroke(new BasicStroke(1.5f));
            for (Rectangle box : new Rectangle[]{rPierre, rLouis, rSdb, rSalon, rJacques, rThomas, rPaul})
                g2d.drawRect(box.x, box.y, box.width, box.height);

            g2d.setFont(new Font("Arial", Font.BOLD, 10));
            labelMap(g2d, rPierre,  "Pierre",  U_PIERRE,  tAct, tDef);
            labelMap(g2d, rLouis,   "Louis",   U_LOUIS,   tAct, tDef);
            g2d.setColor(tDef); g2d.drawString("SdB", rSdb.x + 18, rSdb.y + 20);
            labelMap(g2d, rJacques, "Jacques", U_JACQUES, tAct, tDef);
            g2d.setColor(new Color(200,170,170));
            g2d.drawString("T", rThomas.x + 7, rThomas.y + 18);
            g2d.drawString("P", rPaul.x + 7,   rPaul.y + 18);
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            labelMap(g2d, rSalon, "SALON", U_SALON, tAct, tDef);
        }

        private void paintMapCase(Graphics2D g, Rectangle r, String u, Color def, Color act, Color inacc) {
            boolean isHere = universActuel.equals(u);
            boolean accessible = peutAcceder(u) || u.equals(U_SALON);
            Color c = isHere ? act : (accessible ? def : inacc);
            g.setColor(c);
            g.fillRect(r.x, r.y, r.width, r.height);
        }

        private void labelMap(Graphics2D g, Rectangle r, String txt, String u, Color tAct, Color tDef) {
            g.setColor(universActuel.equals(u) ? tAct : tDef);
            g.drawString(txt, r.x + 6, r.y + 18);
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
                    c.setBounds(r.x + 20, r.y + 20, 750, 30);
                }
            }
        }
    }
}