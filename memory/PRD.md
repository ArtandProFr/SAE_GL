# Escape Game — PRD (Branche `final_struct`)

## 1. Énoncé du problème (verbatim utilisateur)
> Créer une nouvelle branche sur GitHub (nommée `final_struct`) et rassembler/fusionner
> tout le code des branches `Thomas`, `roxanedmd-menu`, et `main` pour obtenir une
> structure unifiée. Résoudre les doublons en gardant la version la plus récente.
> Adapter les éléments visuels pour qu'ils soient tous compatibles et facilement
> utilisables ensemble. Utiliser l'architecture et l'outil de build Maven.

## 2. Stack technique
- **Java 17**
- **Maven** (build unique pour tout le projet)
- **JavaFX 21** (écrans de menu : MainApp, GameScreen, NewGameScreen, Parametres)
- **Swing** (cœur du jeu : `com.sae.game.Jeu`)
- **Pont JavaFX ↔ Swing** : `javafx-swing` / `SwingNode`

## 3. Architecture du projet
```
/app/
├── pom.xml                        # Maven unifié (profils auto Linux/Mac/Win + aarch64)
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/
    │   │       ├── roxane/app/    # JavaFX UI : MainApp, GameScreen, NewGameScreen,
    │   │       │                  #             ParametresScreen, Bouton, Settings,
    │   │       │                  #             Translations, RotaryDial, Slider
    │   │       └── sae/
    │   │           ├── core/      # FileManager, Save, Time
    │   │           ├── enigmas/   # Slide, Vec2
    │   │           └── game/      # Jeu (JFrame/JPanel principal du jeu Swing)
    │   └── resources/
    │       ├── assets/            # background.png, etc.
    │       ├── fonts/             # Minecraft.ttf
    │       ├── icons/, images/    # images/Thomas/* utilisé par le jeu Swing
    │       ├── musics/, sounds/
    │       └── style.css
    └── test/
        └── java/com/sae/
            ├── ResourcesSmokeTest.java
            └── JeuPanelSmokeTest.java
```

## 4. Décisions clés
- **Chemins ressources** : tout passe par le **classpath** (`getResource…`).
  Plus de `new File("…")` avec chemins relatifs codés en dur.
- **Sauvegardes utilisateur** : `FileManager.userDir = ~/.sae_game/`
  (dossier créé automatiquement au chargement de la classe via un bloc `static`).
  C'est inscriptible sur Linux/macOS/Windows, et indépendant du JAR.
- **Intégration JavaFX ↔ Swing** : le jeu Swing s'affiche **dans la même
  fenêtre JavaFX** (choix utilisateur explicite). On utilise `SwingNode`
  alimenté par `Jeu.getGamePanel()`, avec création sur l'EDT via
  `SwingUtilities.invokeLater(...)`. Un bouton overlay JavaFX permet le
  retour au menu.
- **Cycle de vie Swing** : `Jeu` utilise `DISPOSE_ON_CLOSE` (et non plus
  `EXIT_ON_CLOSE`) pour ne pas tuer l'application JavaFX.

## 5. Changelog (cette session — 2026-02)
- 🟢 Refactor `FileManager.java` : `userDir` → `~/.sae_game/`, création du
  dossier `Saves` au démarrage.
- 🟢 `Jeu.java` : ajout `getGamePanel()` (pour SwingNode), passage en
  `DISPOSE_ON_CLOSE`.
- 🟢 `NewGameScreen.java` : remplacement du `setVisible(true)` (fenêtre
  séparée) par `launchSwingGameInScene(...)` qui embarque le jeu dans la
  scène JavaFX courante via `SwingNode` + bouton "RETOUR MENU".
- 🟢 Nettoyage : suppression de `resources/images/IMG_1007.jpeg` (orphelin).
- 🟢 Tests smoke : `ResourcesSmokeTest` (7 assets + dossier sauvegardes),
  `JeuPanelSmokeTest` (instanciation du JPanel sous Xvfb).
- 🟢 Validation runtime : `mvn javafx:run` lancé sous Xvfb sans crash de
  chargement de ressources, police Minecraft chargée correctement.

## 6. Comment exécuter
```bash
# Compilation
mvn -q compile

# Lancement
mvn javafx:run

# Tests smoke (depuis /app)
mvn -q test-compile
CP=$(cat /tmp/cp.txt 2>/dev/null || mvn -q dependency:build-classpath -Dmdep.outputFile=/tmp/cp.txt && cat /tmp/cp.txt)
java -cp "target/classes:target/test-classes:$CP" com.sae.ResourcesSmokeTest
DISPLAY=:99 java -cp "target/classes:target/test-classes:$CP" com.sae.JeuPanelSmokeTest
```

## 7. Backlog
### P1 — Améliorations
- Sons/musiques : vérifier que `musics/` et `sounds/` sont effectivement
  utilisés et chargés via classpath. (Pas de référence trouvée actuellement
  dans le code Java — peut-être prévu pour un futur sprint.)
- Dossiers `resources/images/Menu/` et `resources/images/Ingame/` sont
  présents mais non référencés. À intégrer ou supprimer selon l'évolution
  de la roadmap visuelle.

### P2 — Polish
- Internationalisation : `Translations` est utilisée pour les libellés ;
  vérifier la couverture des nouveaux écrans (notamment le bouton
  "RETOUR MENU" en surimpression du jeu).
- Ajouter un raccourci clavier (Échap) pour revenir au menu depuis le jeu.

## 8. Workflow Git
- Tout est sur la branche locale **`final_struct`**.
- Pour publier sur GitHub : utiliser la fonctionnalité **"Save to Github"**
  d'Emergent (ne pas pousser depuis l'agent).
