package com.roxane.app;

import com.sae.core.GameInfos;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class ParametresScreen {
    private final Stage stage;
    private final Font minecraftFont;
    private final Runnable onBack;
    private final String backbuttonText;

    public ParametresScreen(Stage stage, Font minecraftFont, Runnable onBack, String backbuttonText) {
        this.stage = stage;
        this.minecraftFont = minecraftFont;
        this.onBack = onBack;
        this.backbuttonText = backbuttonText;
    }

    public void show() {
        Image backgroundImage = new Image(getClass().getResourceAsStream("/assets/background.png"));
        ImageView backgroundImageView = new ImageView(backgroundImage);
        backgroundImageView.setFitWidth(1280);
        backgroundImageView.setFitHeight(720);
        backgroundImageView.setPreserveRatio(false);

        Settings s = Settings.getInstance();

        Slider brightnessSlider = new Slider(0, 2, s.getBrightness());
        brightnessSlider.getStyleClass().add("param-slider");
        brightnessSlider.setPrefWidth(280);
        brightnessSlider.setShowTickMarks(true);
        brightnessSlider.setShowTickLabels(true);
        brightnessSlider.setMajorTickUnit(1);

        Label brightnessValue = makeValueLabel(String.format("%.1f", s.getBrightness()));

        Slider volumeSlider = new Slider(0, 100, s.getVolume());
        volumeSlider.getStyleClass().add("param-slider");
        volumeSlider.setPrefWidth(280);
        volumeSlider.setShowTickMarks(true);
        volumeSlider.setShowTickLabels(true);
        volumeSlider.setMajorTickUnit(50);

        Label volumeValue = makeValueLabel(String.format("%.0f%%", s.getVolume()));

        ComboBox<String> langCombo = new ComboBox<>();
        langCombo.getItems().addAll("Francais", "English");
        langCombo.setValue(s.getLanguage());
        langCombo.getStyleClass().add("param-combo");
        langCombo.setPrefWidth(200);

        VBox settingsPanel = new VBox(28);
        settingsPanel.getStyleClass().add("gold-frame");
        settingsPanel.setPadding(new Insets(36, 48, 36, 48));
        settingsPanel.setMaxWidth(700);
        settingsPanel.setAlignment(Pos.CENTER_LEFT);
        settingsPanel.getChildren().addAll(
            makeSliderRow(Translations.t("LUMINOSITE"), brightnessSlider, brightnessValue),
            makeSliderRow(Translations.t("SON"), volumeSlider, volumeValue),
            makeLanguageRow(langCombo)
        );

        Label title = new Label(Translations.t("PARAMETRES"));
        title.getStyleClass().add("panel-title");
        if (minecraftFont != null) {
            title.setFont(Font.font(minecraftFont.getFamily(), 40));
        }

        Bouton backButton = new Bouton(Translations.t(this.backbuttonText));
        backButton.getStyleClass().add("small-action-button");
        backButton.setPrefWidth(260);
        if (minecraftFont != null) {
            backButton.setFont(Font.font(minecraftFont.getFamily(), 16));
        }
        backButton.setOnAction(e -> onBack.run());

        VBox content = new VBox(32);
        content.getStyleClass().add("game-overlay");
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(48));
        content.getChildren().addAll(title, settingsPanel, backButton);

        StackPane root = new StackPane(backgroundImageView, content);

        s.applyBrightness(root);

        brightnessSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double val = newVal.doubleValue();
            s.setBrightness(val);
            brightnessValue.setText(String.format("%.1f", val));
            s.applyBrightness(root);
        });

        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double val = newVal.doubleValue();
            s.setVolume(val);
            volumeValue.setText(String.format("%.0f%%", val));
        });

        langCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null){
                s.setLanguage(newVal);
                this.stage.setTitle(Translations.t(GameInfos.GAMENAME_TYPE_2));
                show();
            }
        });

        Scene scene = new Scene(root, 1280, 720);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setScene(scene);
    }

    private HBox makeSliderRow(String labelText, Slider slider, Label valueLabel) {
        HBox row = new HBox(24);
        row.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label(labelText);
        label.setTextFill(Color.web("#ffde64"));
        label.setPrefWidth(160);
        if (minecraftFont != null) {
            label.setFont(Font.font(minecraftFont.getFamily(), 18));
        }

        row.getChildren().addAll(label, slider, valueLabel);
        return row;
    }

    private HBox makeLanguageRow(ComboBox<String> combo) {
        HBox row = new HBox(24);
        row.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label(Translations.t("LANGUES"));
        label.setTextFill(Color.web("#ffde64"));
        label.setPrefWidth(160);
        if (minecraftFont != null) {
            label.setFont(Font.font(minecraftFont.getFamily(), 18));
        }

        row.getChildren().addAll(label, combo);
        return row;
    }

    private Label makeValueLabel(String text) {
        Label l = new Label(text);
        l.setTextFill(Color.web("#ffde64"));
        l.setPrefWidth(55);
        if (minecraftFont != null) {
            l.setFont(Font.font(minecraftFont.getFamily(), 14));
        }
        return l;
    }
}
