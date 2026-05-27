package com.roxane.app;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
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

public class MeilleurTempsScreen {
    private final Stage stage;
    private final Font minecraftFont;
    private final Runnable onBack;

    public MeilleurTempsScreen(Stage stage, Font minecraftFont, Runnable onBack) {
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

        Bouton backButton = new Bouton(Translations.t("RETOUR MENU"));
        backButton.getStyleClass().add("small-action-button");
        backButton.setPrefWidth(240);
        if (minecraftFont != null) {
            backButton.setFont(Font.font(minecraftFont.getFamily(), 16));
        }
        backButton.setOnAction(e -> onBack.run());

        VBox right = new VBox(backButton);
        right.setAlignment(Pos.BOTTOM_CENTER);
        right.setPadding(new Insets(0, 0, 0, 20));
        content.setRight(right);

        StackPane root = new StackPane(backgroundImageView, content);
        Settings.getInstance().applyBrightness(root);

        Scene scene = new Scene(root, 1280, 720);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setScene(scene);
    }

    private VBox createMainContent() {
        VBox main = new VBox(12);

        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        Label clock = new Label("⏱");
        clock.setFont(Font.font(56));
        clock.setTextFill(Color.web("#ffde64"));

        Label title = new Label(Translations.t("MEILLEUR TEMPS"));
        title.getStyleClass().add("panel-title");
        if (minecraftFont != null) {
            title.setFont(Font.font(minecraftFont.getFamily(), 36));
        }

        header.getChildren().addAll(clock, title);

        HBox colHeader = new HBox();
        colHeader.getStyleClass().add("gold-panel");
        colHeader.setPadding(new Insets(10, 16, 10, 16));
        colHeader.setAlignment(Pos.CENTER_LEFT);
        colHeader.getChildren().addAll(
            makeColLabel("#", 60, "#101010"),
            makeColLabel(Translations.t("TEMPS"), 200, "#101010"),
            makeColLabel(Translations.t("NOM"), -1, "#101010")
        );

        VBox scoreList = new VBox(8);
        scoreList.setPadding(new Insets(10));

        String[][] data = {
            {"1", "1m 23s", "JOUEUR_1"},
            {"2", "1m 45s", "JOUEUR_2"},
            {"3", "2m 01s", "JOUEUR_3"},
            {"4", "2m 18s", "JOUEUR_4"},
            {"5", "2m 35s", "JOUEUR_5"},
            {"6", "3m 02s", "JOUEUR_6"},
            {"7", "3m 40s", "JOUEUR_7"},
            {"8", "4m 11s", "JOUEUR_8"},
            {"9", "4m 55s", "JOUEUR_9"},
            {"10", "5m 30s", "JOUEUR_10"}
        };
        for (String[] row : data) {
            scoreList.getChildren().add(createScoreItem(row[0], row[1], row[2]));
        }

        ScrollPane scroll = new ScrollPane(scoreList);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("save-scroll");

        main.getChildren().addAll(header, colHeader, scroll);
        return main;
    }

    private HBox createScoreItem(String rank, String time, String name) {
        HBox item = new HBox();
        item.getStyleClass().add("save-item");
        item.setPadding(new Insets(8, 16, 8, 16));
        item.setAlignment(Pos.CENTER_LEFT);
        item.getChildren().addAll(
            makeColLabel(rank, 60, "#ffde64"),
            makeColLabel(time, 200, "#ffde64"),
            makeColLabel(name, -1, "#ffde64")
        );
        return item;
    }

    private Label makeColLabel(String text, double width, String color) {
        Label l = new Label(text);
        l.setTextFill(Color.web(color));
        if (minecraftFont != null) {
            l.setFont(Font.font(minecraftFont.getFamily(), 15));
        }
        if (width > 0) {
            l.setPrefWidth(width);
        } else {
            HBox.setHgrow(l, Priority.ALWAYS);
        }
        return l;
    }
}
