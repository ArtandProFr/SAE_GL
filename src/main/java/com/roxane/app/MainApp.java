package com.roxane.app;

import com.sae.core.GameInfos;
import com.sae.core.PauseManager;
import com.sae.core.Phase;
import com.sae.core.Save;
import com.sae.core.Time;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class MainApp extends Application {

    private Stage primaryStage;
    private Font minecraftFont;
    private Font minecraftFontTitle;
    private Save save;
    private PauseManager pauseManager;

    @Override
    public void start(Stage stage){
        this.primaryStage = stage;
        this.save = new Save();
        this.pauseManager = new PauseManager();
        primaryStage.setOnCloseRequest(event -> {
            // Sauvegarde la partie en cours
            if ((this.save != null) && (!this.pauseManager.isPaused) && this.save.alreadyExists()){
                Phase phase = new Phase(this.save.getPhase());
                if (phase.getPoids() != 0){
                    save.addTime(((int) (Time.now() - save.getLastSave())) / 1000);
                }
                if (!phase.estJeuFini())
                    save.save();
                else Save.updateLine(save);
            }
            
            // 1. Arrête proprement la plateforme JavaFX
            javafx.application.Platform.exit();
            
            // 2. Tue la JVM et tous les processus/threads liés (Swing, Timers, etc.)
            System.exit(0);
        });

        // Charger la police Minecraft
        minecraftFont = null;
        minecraftFontTitle = null;
        
        try {
            var fontStream = getClass().getResourceAsStream("/fonts/Minecraft.ttf");
            if (fontStream != null) {
                minecraftFont = Font.loadFont(fontStream, 16);
                
                fontStream = getClass().getResourceAsStream("/fonts/Minecraft.ttf");
                minecraftFontTitle = Font.loadFont(fontStream, 84);
            }
        } catch (Exception e) {
            System.err.println("Erreur chargement police : " + e.getMessage());
        }
        
        if (minecraftFont == null) {
            minecraftFont = Font.font("Arial", 16);
            minecraftFontTitle = Font.font("Arial", 84);
        }

        showMenu();
    }

    public void showMenu() {
        Text title = new Text(Translations.t("MENU"));
        if (minecraftFontTitle != null) {
            title.setFont(minecraftFontTitle);
        }
        title.getStyleClass().add("title");

        // Créer l'effet 3D Minecraft pour le titre
        DropShadow blackOutline = new DropShadow();
        blackOutline.setColor(Color.web("#1c130e"));
        blackOutline.setRadius(8);
        blackOutline.setSpread(0.8);
        blackOutline.setOffsetX(0);
        blackOutline.setOffsetY(0);

        DropShadow goldHighlight = new DropShadow();
        goldHighlight.setColor(Color.web("#ffde64"));
        goldHighlight.setRadius(4);
        goldHighlight.setSpread(0.6);
        goldHighlight.setOffsetX(-2);
        goldHighlight.setOffsetY(-2);
        goldHighlight.setInput(blackOutline);

        title.setEffect(goldHighlight);

        // ─── UTILISATION DE TON BOUTON PERSONNALISÉ ─────────────────────
        Bouton playButton = new Bouton(Translations.t("LANCER UNE PARTIE"));
        Bouton settingsButton = new Bouton(Translations.t("PARAMETRES"));
        Bouton bestTimeButton = new Bouton(Translations.t("MEILLEURS TEMPS"));

        if (minecraftFont != null) {
            // On augmente la taille de la police pour le menu principal (32px au lieu de 16px)
            playButton.setFont(Font.font(minecraftFont.getFamily(), 32));
            settingsButton.setFont(Font.font(minecraftFont.getFamily(), 32));
            bestTimeButton.setFont(Font.font(minecraftFont.getFamily(), 32));
        }

        // Ajouter l'effet drop shadow aux boutons
        DropShadow buttonShadow = new DropShadow();
        buttonShadow.setColor(Color.web("#000000"));
        buttonShadow.setRadius(10);
        buttonShadow.setSpread(0.8);

        playButton.setEffect(buttonShadow);
        settingsButton.setEffect(buttonShadow);
        bestTimeButton.setEffect(buttonShadow);

        playButton.setOnAction(e -> new GameScreen(primaryStage, minecraftFont, this::showMenu, this.save, this.pauseManager).show());
        settingsButton.setOnAction(e -> new ParametresScreen(primaryStage, minecraftFont, this::showMenu, "RETOUR MENU").show());
        bestTimeButton.setOnAction(e -> new MeilleurTempsScreen(primaryStage, minecraftFont, this::showMenu).show());

        VBox menuBox = new VBox(20);
        menuBox.getChildren().addAll(title, playButton, settingsButton, bestTimeButton);
        menuBox.setAlignment(Pos.CENTER);
        menuBox.setMaxWidth(620); // Changé de 440 à 620 pour accueillir la nouvelle largeur de 600px

        // Charger et afficher l'image de fond
        Image backgroundImage = new Image(getClass().getResourceAsStream("/assets/background.png"));
        ImageView backgroundImageView = new ImageView(backgroundImage);
        backgroundImageView.setFitWidth(1280);
        backgroundImageView.setFitHeight(720);
        backgroundImageView.setPreserveRatio(false);

        StackPane root = new StackPane(backgroundImageView, menuBox);
        root.getStyleClass().add("root");
        Settings.getInstance().applyBrightness(root);

        Scene scene = new Scene(root, 1280, 720);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        primaryStage.setTitle(Translations.t(GameInfos.GAMENAME_TYPE_2));
        primaryStage.setScene(scene);
        try {
            String iconPath = "/icons/icon3.png";
            Image icone = new Image(getClass().getResourceAsStream(iconPath));
            this.primaryStage.getIcons().add(icone);
        } catch(Exception e){
            // Ignore
        }
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}