# BLURRED — Escape Game en Java / INSA HdF SHpI 2A

**BLURRED** est un jeu vidéo de type **Escape Game / enquête policière** développé en Java (JavaFX + Swing) dans le cadre de la SAE Génie Logiciel.

Le joueur incarne **Thomas**, un colocataire qui découvre un matin le corps de **Louis**. À travers une série d'énigmes et de scènes de dialogue, il fouille l'appartement pièce par pièce pour démasquer le coupable... avant de comprendre une terrible vérité.

---

## 🎮 Pour les joueurs

### Lancer le jeu
1. Installez **Java JDK 17 ou plus récent** (le projet est compilé en `release 17`).
2. Depuis la racine du projet, lancez :
   ```bash
   mvn clean javafx:run
   ```
   ou exécutez le `.jar` généré si vous en avez packagé un.
   Il vous est aussi possible de passer par la branche Executable qui contient un .jar compatible x64 (Windows, Linux, Mac).

### But du jeu
Trouvez les indices qui mènent au coupable en résolvant les énigmes de chaque pièce. La progression est linéaire : chaque phase débloque la suivante.

### Contrôles
| Action | Touche / Souris |
|--------|-----------------|
| Interagir (objets, portes, indices) | Clic gauche |
| Avancer dans un dialogue | Clic gauche sur la zone de dialogue |
| Changer de vue dans une pièce | Flèches gauche / droite à l'écran |
| **Ouvrir / fermer le menu PAUSE** | **Touche ÉCHAP** |
| Se déplacer entre les pièces | Mini-carte en bas à gauche, clic gauche sur les portes |

Un rappel **« Appuyez sur ÉCHAP pour accéder au menu PAUSE »** est affiché en permanence en bas à droite pendant le jeu.

### Menu PAUSE
Le menu PAUSE (touche ÉCHAP) permet de :
- Reprendre la partie,
- Ouvrir les paramètres (langue, luminosité, son),
- Revenir au menu principal,
- Consulter la **Note de version** (encadré sur le côté) qui indique la version du jeu et la liste des **bugs connus & solutions**.

### Difficultés
- **Facile** : indices supplémentaires affichés dans les énigmes.
- **Normale** : expérience par défaut.
- **Difficile** : énigmes plus complexes, moins d'aides.

### Langues
Le jeu est entièrement disponible en **Français** et en **Anglais** (à changer dans les paramètres). La langue par défaut est le français.

---

## 🧩 Déroulement (scénario)

> ⚠️ **Spoilers** — à ne lire qu'après avoir joué.

- **Phase 1 — Salon** : découverte du corps, recherche du verre empoisonné, énigme d'empreintes (l'empreinte est celle de **Pierre**).
- **Phase 2 — Chambre de Pierre** : on trouve la clé d'une armoire à pharmacie contenant des **médicaments** (anxiolytiques). L'étiquette prévient qu'un surdosage peut être mortel : Pierre devient suspect.
- **Phase 3 — Chambre de Louis** : déverrouillage de la porte (cadran rotatif), code de l'ordinateur (post-it `4691`), coupure de courant (portes logiques + lumières), discussion Louis/Pierre, puis **livre de chimie**. Ce livre **disculpe Pierre** : ses médicaments ne sont pas le poison.
- **Phase 4 — Salle de bain** : téléphone (correspondance d'ondes), répondeur (*« Paul t'en veut, on en parle quand je serai rentré. »*), lampe UV révélant une tache sur la serviette de **Jacques**.
- **Phase 5 — Chambre de Jacques** : Paul rentre et s'explique, puis énigme des billes pour ouvrir le tiroir contenant une **fiole de poison vide**.
- **Phase 6 — Révélations** : confrontation finale. On comprend que **c'est Thomas (le joueur) le meurtrier**.
- **Phase 7 — Crédits**.

---

## 🛠️ Pour les développeurs

### Prérequis
- **JDK** : 17 (LTS) ou plus récent (`maven.compiler.release = 17`).
- **Maven** : 3.8+ (testé avec 3.8.7 / 3.9.x).
- **JavaFX** : 21 (récupéré automatiquement par Maven, avec détection de plateforme via les profils du `pom.xml` : Linux x64/aarch64, macOS x64/aarch64, Windows).

### Commandes utiles
```bash
mvn clean compile      # compiler
mvn javafx:run         # lancer le jeu (mainClass : com.roxane.app.MainApp)
mvn test               # lancer les tests "smoke" (ressources, panneau de jeu)
```

### Architecture des paquets
```
src/main/java/
├── com/roxane/app/      # Couche JavaFX : menus, écrans, paramètres, traductions
│   ├── MainApp.java         # Point d'entrée JavaFX
│   ├── GameScreen.java      # Liste des sauvegardes
│   ├── NewGameScreen.java   # Création de partie + intégration du jeu Swing + menu PAUSE
│   ├── ParametresScreen.java
│   ├── Settings.java        # Réglages globaux (langue, luminosité, son, difficulté)
│   └── Translations.java    # Système de traduction (clés abstraites FR/EN)
├── com/sae/core/        # Modèle de jeu
│   ├── GameInfos.java       # Nom, VERSION, RELEASE_NOTES (bugs), crédits
│   ├── Phase.java           # Découpage du scénario en phases pondérées
│   ├── Save.java            # Sauvegardes (sérialisation + intégrité)
│   ├── PauseManager.java
│   └── Time.java
├── com/sae/enigmas/     # Énigmes (UI Swing) : cadran rotatif, portes logiques,
│   │                    # lumières, ondes, billes, empreintes, lampe UV, verres...
└── com/sae/game/
    └── Jeu.java             # Boucle de jeu Swing : rendu, clics, navigation, scénario
```

### Système de traduction
Tout texte visible par l'utilisateur passe par `Translations.t("CLE")`.
- La table `GAME` (dans `Translations.java`) associe une **clé abstraite**
  (ex. `Message_Cinematique_Paul_1`) à un couple `[français, anglais]`.
- Les anciennes entrées de menu (où la clé est le texte français) restent
  prises en charge pour compatibilité.

**Pour ajouter un texte :** ajoutez une entrée `put("MA_CLE", "fr", "en");` dans
le bloc statique de `Translations.java`, puis utilisez `Translations.t("MA_CLE")`
dans le code (jamais de chaîne « en dur »).

### Versioning
La version courante est définie par `GameInfos.VERSION` (actuellement **`Alpha`**).
Les notes de version (bugs connus) sont listées dans `GameInfos.RELEASE_NOTES`
sous forme de couples de clés `{titre, solution}` traduites, et affichées dans
l'encadré du menu PAUSE.

---

## 🐞 Bugs connus & solutions

Certains bugs d'affichage proviennent de l'intégration Swing dans JavaFX et du
gestionnaire de fenêtres du système. Ils n'affectent pas la sauvegarde et se
contournent facilement :

| Bug | Symptôme | Solution |
|-----|----------|----------|
| **Images rétrécies sur fond blanc** | En sortant d'un zoom, l'affichage peut se figer : les images apparaissent réduites sur un fond blanc. | **Déplacez légèrement la fenêtre du jeu** (clic-glisser sur la barre de titre) pour forcer un rafraîchissement du rendu. |
| **Pop-up cachée derrière la fenêtre** | Une fenêtre d'information / d'avertissement (indice, énigme) s'ouvre *derrière* la fenêtre principale : le jeu semble bloqué et ne répond plus aux clics. | **Faites `Windows + Tab` (ou `Alt + Tab`)** et sélectionnez la fenêtre pop-up pour la ramener au premier plan, puis fermez-la normalement. |

> Ces deux contournements sont aussi rappelés en jeu, dans l'encadré
> **Note de version** du menu PAUSE.

### Remarques de conception (non bloquantes)
- Si une sauvegarde reprend sur une étape qui commence par une énigme, celle-ci
  est **réinitialisée** : le joueur doit donc sortir de l'énigme pour pouvoir se
  déplacer dans la maison.
- Les chambres de **Thomas** et de **Paul** ne sont jamais accessibles
  (volontaire, justifié par le scénario).

---

## 👥 Crédits
**Réalisation & développement — Groupe 2 :**
- DWORNICZAK Arthur
- GAMON Thomas
- OUERHANI Farès
- PERROT Roxane

*INSA Hauts-de-France — Sciences et Humanités pour l'Ingénieur (2A)*
*SAE Génie Logiciel — Responsable : M. Kolski — Année 2025-2026*
