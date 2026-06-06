package com.roxane.app;

import com.sae.core.Save;

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

    public NewGameScreen(Stage stage, Font minecraftFont, Runnable onBackToList, Runnable onBackToMenu) {
        this.stage = stage;
        this.minecraftFont = minecraftFont;
        this.onBackToList = onBackToList;
        this.onBackToMenu = onBackToMenu;
        this.save = new Save();
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
            launchSwingGameInScene(save);
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
        this.save = existingSave;
        launchSwingGameInScene(existingSave);
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

    /**
     * Affiche le jeu Swing ({@link com.sae.game.Jeu}) à l'intérieur de la
     * fenêtre JavaFX courante, sans ouvrir de seconde fenêtre.
     */
    void launchSwingGameInScene(Save save) {
        SwingNode swingNode = new SwingNode();

        StackPane gameRoot = new StackPane(swingNode);
        gameRoot.setStyle("-fx-background-color: black;");

        // Bouton "RETOUR MENU" overlay JavaFX (en haut à droite)
        Bouton backButton = new Bouton(Translations.t("RETOUR MENU"));
        backButton.getStyleClass().add("small-action-button");
        backButton.setPrefWidth(180);
        if (minecraftFont != null) backButton.setFont(Font.font(minecraftFont.getFamily(), 14));
        backButton.setOnAction(ev -> onBackToMenu.run());

        HBox topBar = new HBox(backButton);
        topBar.setAlignment(Pos.TOP_RIGHT);
        topBar.setPadding(new Insets(12));
        topBar.setMouseTransparent(false);
        topBar.setPickOnBounds(false);

        StackPane.setAlignment(topBar, Pos.TOP_RIGHT);
        gameRoot.getChildren().add(topBar);

        Settings.getInstance().applyBrightness(gameRoot);
        Scene gameScene = new Scene(gameRoot, 1280, 720);
        gameScene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        // Lancement sur l'Event Dispatch Thread (EDT)
        javax.swing.SwingUtilities.invokeLater(() -> {
            com.sae.game.Jeu jeu = new com.sae.game.Jeu(save);

            // Branchement du curseur JavaFX
            jeu.setCursorChangeListener(surElementInteractif -> {
                javafx.application.Platform.runLater(() -> {
                    if (surElementInteractif) {
                        gameScene.setCursor(javafx.scene.Cursor.HAND);
                    } else {
                        gameScene.setCursor(javafx.scene.Cursor.DEFAULT);
                    }
                });
            });

            swingNode.setContent(jeu.getGamePanel());
        });

        stage.setScene(gameScene);
        stage.setTitle("Escape Game - " + save.getSavename()
                + " (" + save.getUsername() + ") / [" + save.getDifficulty() + "]");
    }
}