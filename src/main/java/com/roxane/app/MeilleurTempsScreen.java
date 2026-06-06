package com.roxane.app;

import com.sae.core.Save;
import com.sae.core.Time;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
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
    private String[][] scores;
    double ratio = 1.0;

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

        Settings s = Settings.getInstance();
        ComboBox<String> langCombo = new ComboBox<>();
        langCombo.getItems().addAll(Translations.t("Easy"), Translations.t("Normal"), Translations.t("Hard"));
        langCombo.setValue(Translations.t(s.getDifficulty()));
        langCombo.getStyleClass().add("param-combo");
        langCombo.setPrefWidth(200);

        langCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                s.setDifficulty(Translations.toEN(newVal));
                show();
            }
        });

        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        spacer.setPrefHeight(100);

        VBox right = new VBox(spacer, backButton);
        right.setAlignment(Pos.BOTTOM_CENTER);
        right.setPadding(new Insets(0, 0, 0, 20));

        VBox difficultyPanel = new VBox(28);
        difficultyPanel.getStyleClass().add("gold-frame");
        difficultyPanel.setPadding(new Insets(36, 48, 36, 48));
        difficultyPanel.setMaxWidth(700);
        difficultyPanel.setAlignment(Pos.CENTER_LEFT);
        difficultyPanel.getChildren().addAll(
            makeDifficultyRow(langCombo),
            right
        );
        content.setRight(difficultyPanel);

        StackPane root = new StackPane(backgroundImageView, content);
        Settings.getInstance().applyBrightness(root);

        Scene scene = new Scene(root, 1280, 720);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setScene(scene);
        ratio = stage.getWidth() / 1280;
    }

    private HBox makeDifficultyRow(ComboBox<String> combo) {
        HBox row = new HBox(24);
        row.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label(Translations.t("DIFFICULTE"));
        label.setTextFill(Color.web("#ffde64"));
        label.setPrefWidth(160);
        if (minecraftFont != null) {
            label.setFont(Font.font(minecraftFont.getFamily(), 18));
        }

        row.getChildren().addAll(label, combo);
        return row;
    }

    private VBox createMainContent() {
        Settings s = Settings.getInstance();
        VBox main = new VBox(12);

        // ── En-tête titre
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        Label clock = new Label("⏱");
        clock.setFont(Font.font(56));
        clock.setTextFill(Color.web("#ffde64"));

        Label title = new Label(Translations.t("MEILLEURS TEMPS"));
        title.getStyleClass().add("panel-title");
        if (minecraftFont != null) {
            title.setFont(Font.font(minecraftFont.getFamily(), 36));
        }
        header.getChildren().addAll(clock, title);

        // ── En-tête colonnes
        HBox colHeader = new HBox();
        colHeader.getStyleClass().add("gold-panel");
        colHeader.setPadding(new Insets(10, 16, 10, 16));
        colHeader.setAlignment(Pos.CENTER_LEFT);
        double globalTitleRatio = 1.3;
        colHeader.getChildren().addAll(
            makeColLabel("#",                          32  * ratio * globalTitleRatio, "#101010"),
            makeColLabel(Translations.t("TEMPS"),      75  * ratio * globalTitleRatio, "#101010"),
            makeColLabel(Translations.t("NOM"),        75  * ratio * globalTitleRatio, "#101010"),
            makeColLabel(Translations.t("DIFFICULTE"), 128 * ratio * globalTitleRatio, "#101010"),
            makeColLabel(Translations.t("DATE"),       -1,                             "#101010")
        );

        // ── Chargement et filtrage des données depuis le scoreboard
        VBox scoreList = new VBox(8);
        scoreList.setPadding(new Insets(10));

        Save[] preDataSave = Save.scoreboardToSaves();
        preDataSave = Save.difficultyFilter(preDataSave, s.getDifficulty());
        Save.lessTimeOrder(preDataSave, 1);

        // Construction du tableau d'affichage
        scores = new String[preDataSave.length][5];
        for (int i = 0; i < preDataSave.length; i++) {
            Save sv = preDataSave[i];
            scores[i][0] = String.valueOf(i + 1);                        // Numéro (classement)
            scores[i][1] = Time.chronoToString(sv.getTime());            // Temps hh:mm:ss
            scores[i][2] = sv.getUsername();                             // Pseudo
            scores[i][3] = Translations.t(sv.getDifficulty());          // Difficulté
            scores[i][4] = Time.stringFromInstant(sv.getLastSave());     // Date jj/MM/aaaa - hh:mm:ss
        }

        for (String[] row : scores) {
            scoreList.getChildren().add(createScoreItem(row[0], row[1], row[2], row[3], row[4]));
        }

        ScrollPane scroll = new ScrollPane(scoreList);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("save-scroll");

        main.getChildren().addAll(header, colHeader, scroll);
        return main;
    }

    private HBox createScoreItem(String rank, String time, String name, String difficulty, String date) {
        HBox item = new HBox();
        item.getStyleClass().add("save-item");
        item.setPadding(new Insets(8, 16, 8, 16));
        item.setAlignment(Pos.CENTER_LEFT);
        item.getChildren().addAll(
            makeColLabel(rank,       50  * ratio, "#ffde64"),
            makeColLabel(time,       100 * ratio, "#ffde64"),
            makeColLabel(name,       160 * ratio, "#ffde64"),
            makeColLabel(difficulty, 110 * ratio, "#ffde64"),
            makeColLabel(date,       200 * ratio, "#ffde64")
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