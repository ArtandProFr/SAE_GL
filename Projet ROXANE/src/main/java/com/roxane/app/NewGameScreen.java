package com.roxane.app;

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
            onBackToList.run();
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
        scene.getStylesheets().add("style.css");
        stage.setScene(scene);
    }
}
