package com.roxane.app;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class GameScreen {
    private final Stage stage;
    private final Font minecraftFont;
    private final Runnable onBack;

    public GameScreen(Stage stage, Font minecraftFont, Runnable onBack) {
        this.stage = stage;
        this.minecraftFont = minecraftFont;
        this.onBack = onBack;
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
        content.setCenter(createMainContent());

        StackPane root = new StackPane(backgroundImageView, content);
        Settings.getInstance().applyBrightness(root);

        Scene gameScene = new Scene(root, 1280, 720);
        gameScene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setScene(gameScene);
    }

    private HBox createMainContent() {
        HBox main = new HBox(20);
        main.setAlignment(Pos.TOP_LEFT);

        VBox left = createLeftColumn();
        VBox right = createRightPanel();

        HBox.setHgrow(left, Priority.ALWAYS);
        main.getChildren().addAll(left, right);
        return main;
    }

    private VBox createLeftColumn() {
        VBox left = new VBox(16);

        Label title = new Label(Translations.t("SAUVEGARDES"));
        title.getStyleClass().add("panel-title");
        if (minecraftFont != null) {
            title.setFont(Font.font(minecraftFont.getFamily(), 28));
        }

        HBox header = new HBox(20);
        header.getStyleClass().add("gold-panel");
        header.setPadding(new Insets(12));
        header.setAlignment(Pos.CENTER_LEFT);

        Label hName = new Label(Translations.t("NOM_SAUVEGARDE"));
        Label hDate = new Label(Translations.t("DATE"));
        Label hTime = new Label(Translations.t("TEMPS"));
        styleLabel(hName, 16);
        styleLabel(hDate, 16);
        styleLabel(hTime, 16);

        Region spacer1 = new Region();
        Region spacer2 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        header.getChildren().addAll(hName, spacer1, hDate, spacer2, hTime);

        VBox saveList = new VBox(10);
        saveList.setPadding(new Insets(10));
        for (int i = 1; i <= 10; i++) {
            saveList.getChildren().add(createSaveItem("PARTIE_" + i, "18/05", i + "m"));
        }

        ScrollPane scrollPane = new ScrollPane(saveList);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(430);
        scrollPane.getStyleClass().add("save-scroll");

        left.getChildren().addAll(title, header, scrollPane);
        return left;
    }

    private HBox createSaveItem(String name, String date, String time) {
        HBox item = new HBox(20);
        item.getStyleClass().add("save-item");
        item.setPadding(new Insets(10));
        item.setAlignment(Pos.CENTER_LEFT);

        Label lName = new Label(name);
        Label lDate = new Label(date);
        Label lTime = new Label(time);
        styleLabel(lName, 14);
        styleLabel(lDate, 14);
        styleLabel(lTime, 14);

        Region spacer1 = new Region();
        Region spacer2 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        item.getChildren().addAll(lName, spacer1, lDate, spacer2, lTime);
        return item;
    }

    private VBox createRightPanel() {
        VBox panel = new VBox(16);
        panel.setAlignment(Pos.TOP_CENTER);

        Bouton newGameButton = new Bouton(Translations.t("LANCER UNE NOUVELLE PARTIE"));
        newGameButton.getStyleClass().add("side-action-button");
        newGameButton.setPrefSize(260, 360);
        if (minecraftFont != null) {
            newGameButton.setFont(Font.font(minecraftFont.getFamily(), 22));
        }
        newGameButton.setOnAction(e -> new NewGameScreen(stage, minecraftFont, this::show, onBack).show());

        Bouton backButton = new Bouton(Translations.t("RETOUR MENU"));
        backButton.getStyleClass().add("small-action-button");
        backButton.setPrefWidth(260);
        if (minecraftFont != null) {
            backButton.setFont(Font.font(minecraftFont.getFamily(), 16));
        }
        backButton.setOnAction(e -> onBack.run());

        panel.getChildren().addAll(newGameButton, backButton);
        return panel;
    }

    private void styleLabel(Label label, double size) {
        label.setTextFill(javafx.scene.paint.Color.web("#ffde64"));
        if (minecraftFont != null) {
            label.setFont(Font.font(minecraftFont.getFamily(), size));
        } else {
            label.setFont(Font.font(size));
        }
    }
}
