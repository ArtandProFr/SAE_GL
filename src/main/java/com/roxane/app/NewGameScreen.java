package com.roxane.app;

import com.sae.core.GameInfos;
import com.sae.core.PauseManager;
import com.sae.core.Phase;
import com.sae.core.Save;
import com.sae.core.Time;

import javafx.embed.swing.SwingNode;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class NewGameScreen {
    private final Stage stage;
    private final Font minecraftFont;
    private final Runnable onBackToList;
    private final Runnable onBackToMenu;
    private Save save;
    private final PauseManager pause;

    public NewGameScreen(Stage stage, Font minecraftFont, Runnable onBackToList, Runnable onBackToMenu, Save save, PauseManager pause) {
        this.stage = stage;
        this.minecraftFont = minecraftFont;
        this.onBackToList = onBackToList;
        this.onBackToMenu = onBackToMenu;
        this.save = save;
        //this.save.reset();
        this.pause = pause;
        this.pause.setStatus(false);
    }

    public void show() {
        Image backgroundImage = new Image(getClass().getResourceAsStream("/assets/background.png"));
        ImageView backgroundImageView = new ImageView(backgroundImage);
        backgroundImageView.setFitWidth(1280);
        backgroundImageView.setFitHeight(720);
        backgroundImageView.setPreserveRatio(false);

        BorderPane content = new BorderPane();
        content.getStyleClass().add("game-overlay");
        content.setPadding(new Insets(24));

        VBox panel = new VBox(18);
        panel.getStyleClass().add("gold-frame");
        panel.setPadding(new Insets(18));

        Label pageTitle = new Label(Translations.t("NOUVELLE PARTIE"));
        pageTitle.getStyleClass().add("panel-title");
        if (minecraftFont != null) {
            pageTitle.setFont(Font.font(minecraftFont.getFamily(), 34));
        }

        VBox formContainer = new VBox(18);
        formContainer.getStyleClass().add("gold-panel");
        formContainer.setPadding(new Insets(20));

        // ── Nom de la sauvegarde
        HBox nameRow = makeFormRow(
            Translations.t("NOM_SAUVEGARDE:"),
            null, null
        );
        TextField saveNameField = new TextField();
        saveNameField.setPromptText(Translations.t("MA_PARTIE"));
        saveNameField.getStyleClass().add("line-input");
        HBox.setHgrow(saveNameField, Priority.ALWAYS);
        nameRow.getChildren().add(saveNameField);

        // ── Nom du joueur
        HBox playerRow = makeFormRow(
            Translations.t("NOM_JOUEUR:"),
            null, null
        );
        TextField playerNameField = new TextField();
        playerNameField.setPromptText("Player");
        playerNameField.getStyleClass().add("line-input");
        HBox.setHgrow(playerNameField, Priority.ALWAYS);
        playerRow.getChildren().add(playerNameField);

        // ── Difficulté
        HBox diffRow = makeFormRow(
            Translations.t("DIFFICULTE:"),
            null, null
        );
        ComboBox<String> diffCombo = new ComboBox<>();
        diffCombo.getItems().addAll(
            Translations.t("Easy"),
            Translations.t("Normal"),
            Translations.t("Hard")
        );
        diffCombo.setValue(Translations.t("Normal"));
        diffCombo.getStyleClass().add("param-combo");
        diffCombo.setPrefWidth(200);
        diffRow.getChildren().add(diffCombo);

        // ── Message d'erreur (caché par défaut)
        Label errorLabel = new Label("");
        errorLabel.setTextFill(Color.web("#FF4444"));
        if (minecraftFont != null) errorLabel.setFont(Font.font(minecraftFont.getFamily(), 14));
        errorLabel.setVisible(false);

        formContainer.getChildren().addAll(nameRow, playerRow, diffRow, errorLabel);

        VBox emptyZone = new VBox();
        emptyZone.getStyleClass().add("empty-work-zone");
        VBox.setVgrow(emptyZone, Priority.ALWAYS);

        // ── Boutons
        HBox actions = new HBox(14);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Bouton createButton = new Bouton(Translations.t("CREER"));
        createButton.getStyleClass().add("small-action-button");
        createButton.setPrefWidth(200);
        if (minecraftFont != null) createButton.setFont(Font.font(minecraftFont.getFamily(), 16));

        createButton.setOnAction(e -> {
            String savename    = saveNameField.getText().isBlank()
                                 ? Translations.t("MA_PARTIE")
                                 : saveNameField.getText().trim();
            String playername  = playerNameField.getText().isBlank()
                                 ? "Player"
                                 : playerNameField.getText().trim();
            String difficulty  = Translations.toEN(diffCombo.getValue());

            save.modifySavename(savename);
            save.modifyUsername(playername);
            save.modifyDifficulty(difficulty);

            if (!save.isValidToStart()) {
                errorLabel.setText(Translations.t("INFORMATIONS_INVALIDES"));
                errorLabel.setVisible(true);
                return;
            }
            errorLabel.setVisible(false);

            if (!Save.alreadyExists(save)) {
                save.initializeSave();
            } else {
                // Sauvegarde existante : propose de la charger
                errorLabel.setText(Translations.t("SAUVEGARDE_EXISTE"));
                errorLabel.setVisible(true);
                save.initializeSaveFromFile();
            }
            launchSwingGameInScene();
        });

        Bouton backListButton = new Bouton(Translations.t("RETOUR LISTE"));
        backListButton.getStyleClass().add("small-action-button");
        backListButton.setPrefWidth(220);
        if (minecraftFont != null) backListButton.setFont(Font.font(minecraftFont.getFamily(), 16));
        backListButton.setOnAction(e -> onBackToList.run());

        Bouton backMenuButton = new Bouton(Translations.t("RETOUR MENU"));
        backMenuButton.getStyleClass().add("small-action-button");
        backMenuButton.setPrefWidth(220);
        if (minecraftFont != null) backMenuButton.setFont(Font.font(minecraftFont.getFamily(), 16));
        backMenuButton.setOnAction(e -> onBackToMenu.run());

        actions.getChildren().addAll(createButton, backListButton, backMenuButton);

        panel.getChildren().addAll(pageTitle, formContainer, emptyZone, actions);
        content.setCenter(panel);

        StackPane root = new StackPane(backgroundImageView, content);
        Settings.getInstance().applyBrightness(root);
        Scene scene = new Scene(root, 1280, 720);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setScene(scene);
    }

    /**
     * Lance directement une sauvegarde existante sélectionnée depuis GameScreen,
     * sans passer par le formulaire de création.
     */
    public void launchExistingSave(Save existingSave) {
        this.save.loadSave(existingSave);
        launchSwingGameInScene();
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private HBox makeFormRow(String labelText, String promptText, String defaultValue) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label(labelText);
        label.getStyleClass().add("panel-label");
        label.setPrefWidth(200);
        if (minecraftFont != null) label.setFont(Font.font(minecraftFont.getFamily(), 18));

        row.getChildren().add(label);
        return row;
    }

    public void retourMenu(){
        //javax.swing.SwingUtilities.invokeLater(() -> {onBackToMenu.run();});
        onBackToMenu.run();
    }

    /**
     * Affiche le jeu Swing ({@link com.sae.game.Jeu}) à l'intérieur de la
     * fenêtre JavaFX courante, sans ouvrir de seconde fenêtre.
     * Un appui sur ÉCHAP affiche/masque l'overlay de pause contenant les options.
     */
    void launchSwingGameInScene() {
        Phase phase = new Phase(0.0);
        SwingNode swingNode = new SwingNode();

        StackPane gameRoot = new StackPane(swingNode);
        gameRoot.setStyle("-fx-background-color: black;");

        // ─── CRÉATION DE L'OVERLAY DE PAUSE ──────────────────────────────────
        StackPane pauseMenu = new StackPane();
        pauseMenu.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);"); // Fond sombre transparent
        pauseMenu.setVisible(false); // Caché par défaut au lancement

        // 1. Bouton "RETOUR JEU" (pour fermer la pause proprement)
        Bouton resumeButton = new Bouton(Translations.t("RETOUR JEU"));
        resumeButton.getStyleClass().add("small-action-button");
        resumeButton.setPrefWidth(240);
        resumeButton.setPrefHeight(50);
        if (minecraftFont != null) resumeButton.setFont(Font.font(minecraftFont.getFamily(), 16));
        
        // Action de fermeture de la pause (identique au comportement de la touche Échap)
        resumeButton.setOnAction(ev -> {
            pauseMenu.setVisible(false);
            swingNode.setMouseTransparent(false);
            save.save();
        });

        // 2. Bouton "PARAMÈTRES"
        Bouton settingsButton = new Bouton(Translations.t("PARAMETRES"));
        settingsButton.getStyleClass().add("small-action-button");
        settingsButton.setPrefWidth(240);
        settingsButton.setPrefHeight(50);
        if (minecraftFont != null) settingsButton.setFont(Font.font(minecraftFont.getFamily(), 16));
        
        // On lui demande d'ouvrir ParametresScreen, et au retour, on ré-appelle 
        // cette même méthode pour restaurer l'écran de jeu avec la sauvegarde courante
        settingsButton.setOnAction(ev -> {
            new ParametresScreen(stage, minecraftFont, () -> launchSwingGameInScene(), "RETOUR AU JEU").show();
        });

        // 3. Bouton "RETOUR MENU"
        Bouton backButton = new Bouton(Translations.t("RETOUR MENU"));
        backButton.getStyleClass().add("small-action-button");
        backButton.setPrefWidth(240);
        backButton.setPrefHeight(50);
        if (minecraftFont != null) backButton.setFont(Font.font(minecraftFont.getFamily(), 16));
        backButton.setOnAction(ev -> { 
            onBackToMenu.run();});

        // Agencement vertical des boutons de pause
        VBox pauseButtonsBox = new VBox(15); // Espace de 15px entre les boutons
        pauseButtonsBox.getChildren().addAll(resumeButton, settingsButton, backButton);
        pauseButtonsBox.setAlignment(Pos.CENTER);
        
        pauseMenu.getChildren().add(pauseButtonsBox);
        StackPane.setAlignment(pauseButtonsBox, Pos.CENTER);

        // Ajout de l'overlay de pause dans le conteneur principal
        gameRoot.getChildren().add(pauseMenu);

        Settings.getInstance().applyBrightness(gameRoot);
        Scene gameScene = new Scene(gameRoot, 1280, 720);
        gameScene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        // ─── GESTION DE LA TOUCHE ÉCHAP (ESCAPE) ─────────────────────────────
        gameScene.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                this.pause.switchStatus();
                pauseMenu.setVisible(this.pause.isPaused);
                swingNode.setMouseTransparent(this.pause.isPaused);
                if (this.pause.isPaused && phase.getPoids() != 0){
                        save.addTime(((int) (Time.now() - save.getLastSave())) / 1000);
                }
                save.save();
            }
        });

        // Lancement sur l'Event Dispatch Thread (EDT)
        javax.swing.SwingUtilities.invokeLater(() -> {
            com.sae.game.Jeu jeu = new com.sae.game.Jeu(save, phase, stage, this);

            // Branchement du curseur JavaFX
            jeu.setCursorChangeListener(surElementInteractif -> {
                javafx.application.Platform.runLater(() -> {
                    if (surElementInteractif && !pauseMenu.isVisible()) {
                        gameScene.setCursor(javafx.scene.Cursor.HAND);
                    } else {
                        gameScene.setCursor(javafx.scene.Cursor.DEFAULT);
                    }
                });
            });

            swingNode.setContent(jeu.getGamePanel());
        });

        stage.setScene(gameScene);
        stage.setTitle(Translations.t(GameInfos.GAMENAME_TYPE_2)+ " - " + save.getSavename()
                + " (" + save.getUsername() + ") / [" + Translations.t(save.getDifficulty()) + "]");
    }
}