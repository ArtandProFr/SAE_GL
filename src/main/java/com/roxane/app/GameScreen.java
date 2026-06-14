package com.roxane.app;

import com.sae.core.PauseManager;
import com.sae.core.Save;
import com.sae.core.Time;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class GameScreen {
    private final Stage stage;
    private final Font minecraftFont;
    private final Runnable onBack;

    private Save   selectedSave = null;
    private String sortMode     = "DATE";
    private String diffFilter   = "All";
    private String searchQuery  = "";
    private Save save = null;
    private PauseManager pauseManager;

    // Largeurs fixes des colonnes — partagées entre header et items
    private static final double COL_SAVENAME = 155;
    private static final double COL_PLAYER   = 130;
    private static final double COL_DIFF     =  90;
    private static final double COL_DATE     = 175;
    // COL_TIME : flexible

    public GameScreen(Stage stage, Font minecraftFont, Runnable onBack, Save save, PauseManager pause) {
        this.stage = stage;
        this.minecraftFont = minecraftFont;
        this.onBack = onBack;
        this.save = save;
        this.pauseManager = pause;
    }

    public void show() {
        selectedSave = null;

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
        VBox left  = createLeftColumn();
        VBox right = createRightPanel();
        HBox.setHgrow(left, Priority.ALWAYS);
        main.getChildren().addAll(left, right);
        return main;
    }

    // ─── Colonne gauche ───────────────────────────────────────────────────────

    private VBox createLeftColumn() {
        VBox left = new VBox(12);

        Label title = new Label(Translations.t("SAUVEGARDES"));
        title.getStyleClass().add("panel-title");
        if (minecraftFont != null) title.setFont(Font.font(minecraftFont.getFamily(), 28));

        // ── Barre de recherche
        HBox searchBar = new HBox(8);
        searchBar.getStyleClass().add("search-bar");
        searchBar.setAlignment(Pos.CENTER_LEFT);

        Label searchIcon = new Label("🔍");
        searchIcon.setTextFill(Color.web("#ffde64"));
        searchIcon.setFont(Font.font(16));

        TextField searchField = new TextField(searchQuery);
        searchField.getStyleClass().add("search-field");
        searchField.setPromptText(Translations.t("RECHERCHER..."));
        HBox.setHgrow(searchField, Priority.ALWAYS);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            searchQuery = newVal == null ? "" : newVal;
            refreshList(left);
        });

        Button clearBtn = new Button("✕");
        clearBtn.getStyleClass().add("search-clear-button");
        clearBtn.setOnAction(e -> { searchField.clear(); searchQuery = ""; refreshList(left); });

        searchBar.getChildren().addAll(searchIcon, searchField, clearBtn);

        // ── Toolbar : tri + filtre difficulté
        HBox toolbar = new HBox(14);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        // ── Tri : ComboBox au lieu de ToggleButton → état actif évident
        Label sortLabel = new Label(Translations.t("TRIER PAR") + " :");
        sortLabel.setTextFill(Color.web("#ffde64"));
        if (minecraftFont != null) sortLabel.setFont(Font.font(minecraftFont.getFamily(), 13));

        ComboBox<String> sortCombo = new ComboBox<>();
        sortCombo.getItems().addAll(Translations.t("DATE"), Translations.t("NOM"));
        sortCombo.setValue(Translations.t(sortMode));
        sortCombo.getStyleClass().add("param-combo");
        sortCombo.setPrefWidth(140);
        sortCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                // On détecte si c'est date ou nom à partir de la valeur traduite
                String en = Translations.toEN(newVal);
                if (en == null) en = newVal;
                sortMode = en.equalsIgnoreCase("nom") || en.equalsIgnoreCase("name") ? "NOM" : "DATE";
                refreshList(left);
            }
        });

        Region sep = new Region();
        sep.setPrefWidth(10);

        Label diffLabel = new Label(Translations.t("DIFFICULTE") + " :");
        diffLabel.setTextFill(Color.web("#ffde64"));
        if (minecraftFont != null) diffLabel.setFont(Font.font(minecraftFont.getFamily(), 13));

        ComboBox<String> diffCombo = new ComboBox<>();
        diffCombo.getItems().addAll(
            Translations.t("TOUTES"),
            Translations.t("Easy"),
            Translations.t("Normal"),
            Translations.t("Hard")
        );
        diffCombo.setValue(diffFilter.equals("All") ? Translations.t("TOUTES") : Translations.t(diffFilter));
        diffCombo.getStyleClass().add("param-combo");
        diffCombo.setPrefWidth(135);
        diffCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) { diffFilter = toEnDiff(newVal); refreshList(left); }
        });

        toolbar.getChildren().addAll(sortLabel, sortCombo, sep, diffLabel, diffCombo);

        // ── En-tête colonnes — même padding que les items (16px gauche)
        HBox header = new HBox(0);
        header.getStyleClass().add("gold-panel");
        // padding-right = 16 + ~12px scrollbar pour que les colonnes s'alignent avec les items
        header.setPadding(new Insets(10, 28, 10, 16));
        header.setAlignment(Pos.CENTER_LEFT);
        header.getChildren().addAll(
            makeHeaderLabelLeft(Translations.t("NOM_SAUVEGARDE"), COL_SAVENAME),
            makeHeaderLabel(Translations.t("JOUEUR"),                 COL_PLAYER),
            makeHeaderLabel(Translations.t("DIFFICULTE"),             COL_DIFF),
            makeHeaderLabel(Translations.t("DATE"),                   COL_DATE),
            makeHeaderLabelGrow(Translations.t("TEMPS"))
        );

        // ── Conteneur liste
        VBox saveListContainer = new VBox(0);
        saveListContainer.setPadding(new Insets(0));

        ScrollPane scrollPane = new ScrollPane(saveListContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(380);
        scrollPane.getStyleClass().add("save-scroll");
        // Pas de barre horizontale
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        // Barre verticale toujours visible → elle sera rendue hors de la box via CSS
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        left.getChildren().addAll(title, searchBar, toolbar, header, scrollPane);
        fillSaveList(saveListContainer);
        return left;
    }

    /**
     * Recharge le contenu de la liste sans recréer la scène.
     * Structure de left : [0]=title [1]=searchBar [2]=toolbar [3]=header [4]=scrollPane
     */
    private void refreshList(VBox left) {
        ScrollPane scroll = (ScrollPane) left.getChildren().get(4);
        fillSaveList((VBox) scroll.getContent());
    }

    private void fillSaveList(VBox container) {
        container.getChildren().clear();
        selectedSave = null;

        Save[] saves = Save.searchSaves(searchQuery);
        if (!diffFilter.equals("All")) saves = Save.difficultyFilter(saves, diffFilter);
        if (sortMode.equals("DATE"))   Save.lastSaveOrder(saves, -1);
        else                           sortByPlayerName(saves);

        if (saves.length == 0) {
            Label empty = new Label(Translations.t("AUCUNE SAUVEGARDE"));
            empty.setTextFill(Color.web("#ffde64"));
            if (minecraftFont != null) empty.setFont(Font.font(minecraftFont.getFamily(), 15));
            container.getChildren().add(empty);
            return;
        }

        for (Save s : saves) container.getChildren().add(createSaveItem(s, container));
    }

    private HBox createSaveItem(Save s, VBox container) {
        HBox item = new HBox(0);
        item.getStyleClass().add("save-item");
        // Même padding horizontal que l'en-tête pour aligner les colonnes
        item.setPadding(new Insets(9, 16, 9, 16));
        VBox.setMargin(item, new Insets(4, 0, 0, 0));
        item.setAlignment(Pos.CENTER_LEFT);

        item.getChildren().addAll(
            makeItemLabelIndent(s.getSavename(),                       COL_SAVENAME),
            makeItemLabel(s.getUsername(),                             COL_PLAYER),
            makeItemLabel(Translations.t(s.getDifficulty()),           COL_DIFF),
            makeItemLabel(Time.stringFromInstant(s.getLastSave()),     COL_DATE),
            makeItemLabelGrow(Time.chronoToString(s.getTime()))
        );

        item.setOnMouseClicked(e -> {
            container.getChildren().forEach(node -> {
                node.getStyleClass().remove("save-item-selected");
                if (!node.getStyleClass().contains("save-item"))
                    node.getStyleClass().add("save-item");
            });
            item.getStyleClass().remove("save-item");
            item.getStyleClass().add("save-item-selected");
            selectedSave = s;
        });

        return item;
    }

    // ─── Colonne droite ───────────────────────────────────────────────────────

    private VBox createRightPanel() {
        VBox panel = new VBox(16);
        panel.setAlignment(Pos.TOP_CENTER);

        Bouton newGameButton = new Bouton(Translations.t("NOUVELLE PARTIE"));
        newGameButton.getStyleClass().add("side-action-button");
        newGameButton.setPrefSize(260, 200);
        if (minecraftFont != null) newGameButton.setFont(Font.font(minecraftFont.getFamily(), 20));
        newGameButton.setOnAction(e ->
            new NewGameScreen(stage, minecraftFont, this::show, onBack, this.save, pauseManager).show()
        );

        // Label d'erreur (caché par défaut)
        Label errorLabel = new Label("");
        errorLabel.setTextFill(Color.web("#FF4444"));
        errorLabel.setWrapText(true);
        errorLabel.setMaxWidth(260);
        if (minecraftFont != null) errorLabel.setFont(Font.font(minecraftFont.getFamily(), 13));
        errorLabel.setVisible(false);

        Bouton launchButton = new Bouton(Translations.t("LANCER PARTIE"));
        launchButton.getStyleClass().add("side-action-button");
        launchButton.setPrefSize(260, 140);
        if (minecraftFont != null) launchButton.setFont(Font.font(minecraftFont.getFamily(), 20));
        launchButton.setOnAction(e -> handleLaunch(launchButton, errorLabel));

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Bouton backButton = new Bouton(Translations.t("RETOUR MENU"));
        backButton.getStyleClass().add("small-action-button");
        backButton.setPrefWidth(260);
        if (minecraftFont != null) backButton.setFont(Font.font(minecraftFont.getFamily(), 16));
        backButton.setOnAction(e -> onBack.run());

        panel.getChildren().addAll(newGameButton, launchButton, errorLabel, spacer, backButton);
        return panel;
    }

    /**
     * Gère le clic sur "Lancer partie" :
     * 1. Rien de sélectionné → message temporaire dans le bouton
     * 2. Save sélectionnée mais invalide (checksum incorrect, triche détectée…)
     *    → suppression du fichier + message d'erreur + rechargement de la liste
     * 3. Save valide → lancement
     */
    private void handleLaunch(Bouton launchButton, Label errorLabel) {
        if (selectedSave == null) {
            // Aucune sélection
            String original = Translations.t("LANCER PARTIE");
            launchButton.setText(Translations.t("SELECTIONNEZ UNE PARTIE"));
            errorLabel.setVisible(false);
            new Thread(() -> {
                try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
                javafx.application.Platform.runLater(() -> launchButton.setText(original));
            }).start();
            return;
        }

        // Re-lecture du fichier pour valider l'état actuel (pas la version en mémoire)
        Save fresh = Save.getSave(selectedSave.getUsername(), selectedSave.getSavename());

        if (fresh == null || !fresh.isValid()) {
            // Sauvegarde corrompue ou modifiée manuellement
            if (fresh == null) {
                // getSave() a déjà supprimé le fichier si invalide
                selectedSave.delete(); // au cas où
            }
            errorLabel.setText(Translations.t("SAUVEGARDE_INVALIDE"));
            errorLabel.setVisible(true);
            selectedSave = null;

            // Rechargement de la liste pour retirer la save supprimée
            // Le scrollPane est l'enfant [4] de la colonne gauche, mais on
            // n'a pas de référence directe ici → on force un show() complet
            show();
            return;
        }

        // Save valide : on lance
        errorLabel.setVisible(false);
        new NewGameScreen(stage, minecraftFont, this::show, onBack, save, pauseManager)
                .launchExistingSave(fresh);
    }

    // ─── Helpers labels ──────────────────────────────────────────────────────

    /** Label d'en-tête : fond doré, texte sombre, centré. */
    private Label makeHeaderLabel(String text, double width) {
        Label l = new Label(text);
        l.setTextFill(Color.web("#101010"));
        if (minecraftFont != null) l.setFont(Font.font(minecraftFont.getFamily(), 13));
        l.setPrefWidth(width);
        l.setMinWidth(width);
        l.setPadding(new Insets(0, 0, 0, 0));
        // Centrage horizontal : le titre est centré dans la largeur de la colonne
        // ce qui le place visuellement au centre du contenu item sous lui
        l.setAlignment(Pos.CENTER);
        return l;
    }

    private Label makeHeaderLabelGrow(String text) {
        Label l = new Label(text);
        l.setTextFill(Color.web("#101010"));
        if (minecraftFont != null) l.setFont(Font.font(minecraftFont.getFamily(), 13));
        HBox.setHgrow(l, Priority.ALWAYS);
        l.setMinWidth(80);
        l.setPadding(new Insets(0, 0, 0, 0));
        l.setAlignment(Pos.CENTER);
        return l;
    }


    /** Label d'en-tête aligné à gauche (pour les colonnes texte long). */
    private Label makeHeaderLabelLeft(String text, double width) {
        Label l = new Label(text);
        l.setTextFill(Color.web("#101010"));
        if (minecraftFont != null) l.setFont(Font.font(minecraftFont.getFamily(), 13));
        l.setPrefWidth(width);
        l.setMinWidth(width);
        l.setPadding(new Insets(0, 0, 0, 0));
        l.setAlignment(Pos.CENTER_LEFT);
        return l;
    }

    /** Label d'item aligné à gauche (pour les colonnes texte long). */
    private Label makeItemLabelLeft(String text, double width) {
        Label l = new Label(text);
        l.setTextFill(Color.web("#101010"));
        if (minecraftFont != null) l.setFont(Font.font(minecraftFont.getFamily(), 13));
        l.setPrefWidth(width);
        l.setMinWidth(width);
        l.setAlignment(Pos.CENTER_LEFT);
        return l;
    }

    /** Label d'item avec léger indent gauche — titre reste à gauche, données légèrement décalées. */
    private Label makeItemLabelIndent(String text, double width) {
        Label l = new Label(text);
        l.setTextFill(Color.web("#101010"));
        if (minecraftFont != null) l.setFont(Font.font(minecraftFont.getFamily(), 13));
        l.setPrefWidth(width);
        l.setMinWidth(width);
        l.setPadding(new Insets(0, 0, 0, 8)); // indent de 8px pour un look aéré
        l.setAlignment(Pos.CENTER_LEFT);
        return l;
    }

    /** Label d'item : même largeur que le header correspondant, centré. */
    private Label makeItemLabel(String text, double width) {
        Label l = new Label(text);
        l.setTextFill(Color.web("#101010"));
        if (minecraftFont != null) l.setFont(Font.font(minecraftFont.getFamily(), 13));
        l.setPrefWidth(width);
        l.setMinWidth(width);
        l.setAlignment(Pos.CENTER);
        return l;
    }

    private Label makeItemLabelGrow(String text) {
        Label l = new Label(text);
        l.setTextFill(Color.web("#101010"));
        if (minecraftFont != null) l.setFont(Font.font(minecraftFont.getFamily(), 13));
        HBox.setHgrow(l, Priority.ALWAYS);
        l.setMinWidth(80);
        l.setAlignment(Pos.CENTER);
        return l;
    }

    // ─── Utilitaires ─────────────────────────────────────────────────────────

    private String toEnDiff(String displayed) {
        if (displayed == null) return "All";
        String en = Translations.toEN(displayed);
        if (en != null && !en.equals(displayed)) return en;
        String lower = displayed.toLowerCase();
        if (lower.contains("all")  || lower.contains("tout"))  return "All";
        if (lower.contains("easy") || lower.contains("facile")) return "Easy";
        if (lower.contains("normal"))                           return "Normal";
        if (lower.contains("hard") || lower.contains("diffi")) return "Hard";
        return "All";
    }

    private void sortByPlayerName(Save[] arr) {
        for (int i = 0; i < arr.length - 1; i++)
            for (int j = 0; j < arr.length - 1 - i; j++)
                if (arr[j].getUsername().compareToIgnoreCase(arr[j+1].getUsername()) > 0) {
                    Save tmp = arr[j]; arr[j] = arr[j+1]; arr[j+1] = tmp;
                }
    }
}