package com.roxane.app;

import javafx.embed.swing.SwingNode;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class NewGameScreen {
    private final Stage stage;
    private final Font minecraftFont;
    private final Runnable onBackToList;
    private final Runnable onBackToMenu;

    public NewGameScreen(Stage stage, Font minecraftFont, Runnable onBackToList, Runnable onBackToMenu) {
        this.stage = stage;
        this.minecraftFont = minecraftFont;
        this.onBackToList = onBackToList;
        this.onBackToMenu = onBackToMenu;
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

        VBox formContainer = new VBox(14);
        formContainer.getStyleClass().add("gold-panel");
        formContainer.setPadding(new Insets(16));

        HBox nameRow = new HBox(12);
        nameRow.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(Translations.t("NOM_SAUVEGARDE:"));
        nameLabel.getStyleClass().add("panel-label");
        if (minecraftFont != null) {
            nameLabel.setFont(Font.font(minecraftFont.getFamily(), 18));
        }

        TextField saveNameField = new TextField();
        saveNameField.setPromptText("MA_PARTIE");
        saveNameField.getStyleClass().add("line-input");
        HBox.setHgrow(saveNameField, Priority.ALWAYS);

        nameRow.getChildren().addAll(nameLabel, saveNameField);

        VBox emptyZone = new VBox();
        emptyZone.getStyleClass().add("empty-work-zone");
        VBox.setVgrow(emptyZone, Priority.ALWAYS);

        formContainer.getChildren().add(nameRow);

        HBox actions = new HBox(14);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Bouton createButton = new Bouton(Translations.t("CREER"));
        createButton.getStyleClass().add("small-action-button");
        createButton.setPrefWidth(200);
        if (minecraftFont != null) {
            createButton.setFont(Font.font(minecraftFont.getFamily(), 16));
        }
        createButton.setOnAction(e -> {
            String name = saveNameField.getText().isBlank() ? "MA_PARTIE" : saveNameField.getText().trim();
            System.out.println("Nouvelle sauvegarde creee: " + name);
            launchSwingGameInScene(name);
        });

        Bouton backListButton = new Bouton(Translations.t("RETOUR LISTE"));
        backListButton.getStyleClass().add("small-action-button");
        backListButton.setPrefWidth(220);
        if (minecraftFont != null) {
            backListButton.setFont(Font.font(minecraftFont.getFamily(), 16));
        }
        backListButton.setOnAction(e -> onBackToList.run());

        Bouton backMenuButton = new Bouton(Translations.t("RETOUR MENU"));
        backMenuButton.getStyleClass().add("small-action-button");
        backMenuButton.setPrefWidth(220);
        if (minecraftFont != null) {
            backMenuButton.setFont(Font.font(minecraftFont.getFamily(), 16));
        }
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
     * Affiche le jeu Swing ({@link com.sae.game.Jeu}) à l'intérieur de la
     * fenêtre JavaFX courante, sans ouvrir de seconde fenêtre.
     */
    private void launchSwingGameInScene(String partyName) {
        SwingNode swingNode = new SwingNode();

        // On instancie la scène JavaFX de jeu d'abord pour y avoir accès dans l'écouteur
        StackPane gameRoot = new StackPane(swingNode);
        gameRoot.setStyle("-fx-background-color: black;");

        // Bouton "RETOUR MENU" overlay JavaFX (en haut à droite).
        Bouton backButton = new Bouton(Translations.t("RETOUR MENU"));
        backButton.getStyleClass().add("small-action-button");
        backButton.setPrefWidth(180);
        if (minecraftFont != null) {
            backButton.setFont(Font.font(minecraftFont.getFamily(), 14));
        }
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

        // Lancement et configuration de la partie Swing sur l'Event Dispatch Thread (EDT)
        javax.swing.SwingUtilities.invokeLater(() -> {
            com.sae.game.Jeu jeu = new com.sae.game.Jeu();
            
            // BRANCHEMENT DU CURSEUR JAVAFX :
            // On écoute les requêtes de survol envoyées par le code de la classe Jeu
            jeu.setCursorChangeListener(surElementInteractif -> {
                // On force le changement de curseur sur le Thread JavaFX principal
                javafx.application.Platform.runLater(() -> {
                    if (surElementInteractif) {
                        gameScene.setCursor(javafx.scene.Cursor.HAND);
                    } else {
                        gameScene.setCursor(javafx.scene.Cursor.DEFAULT);
                    }
                });
            });

            // Attachement du JPanel du jeu au SwingNode
            swingNode.setContent(jeu.getGamePanel());
        });

        // Application de la scène sur le Stage
        stage.setScene(gameScene);
        stage.setTitle("Escape Game - " + partyName);
    }
}