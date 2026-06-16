package com.roxane.app;

import java.util.HashMap;
import java.util.Map;

/**
 * Système de traduction du jeu.
 * <p>
 * Deux mécanismes coexistent :
 * <ul>
 *   <li>Les anciennes entrées de menu ({@link #EN}/{@link #FR}) où la clé EST
 *       le texte français (conservées pour compatibilité).</li>
 *   <li>La table {@link #GAME} où chaque clé est <b>abstraite</b>
 *       (ex. {@code "Message_Cinematique_Paul_1"}) et pointe vers un couple
 *       {@code [français, anglais]}. C'est le mécanisme à privilégier pour tout
 *       nouveau texte visible par l'utilisateur.</li>
 * </ul>
 * La langue par défaut est le français (voir {@link Settings}).
 */
public class Translations {

    /** Table principale : clé abstraite -> [FR, EN]. */
    private static final Map<String, String[]> GAME = new HashMap<>();

    private static void put(String key, String fr, String en) { GAME.put(key, new String[]{fr, en}); }

    private static boolean isEnglish() {
        return "English".equals(Settings.getInstance().getLanguage());
    }

    static {
        // ─────────────────────────── INTERFACE / OVERLAYS ───────────────────────────
        put("HINT_ESCAPE_PAUSE",
            "Appuyez sur ÉCHAP pour accéder au menu PAUSE",
            "Press ESC to open the PAUSE menu");
        put("VERSION_LABEL", "Version", "Version");
        put("RELEASE_NOTES_TITLE", "Note de version", "Release notes");
        put("RELEASE_NOTES_BUGS_HEADER", "Bugs connus & solutions", "Known bugs & fixes");

        // Bugs (note de version affichée dans le menu PAUSE)
        put("BUG_1_TITLE",
            "Images rétrécies sur fond blanc",
            "Shrunk images over a white background");
        put("BUG_1_FIX",
            "En sortant d'un zoom, l'affichage peut se figer (images réduites, fond blanc). "
            + "Solution : déplacez légèrement la fenêtre du jeu pour forcer le rafraîchissement.",
            "When leaving a zoom, the display can freeze (shrunk images, white background). "
            + "Fix: move the game window slightly to force a refresh.");
        put("BUG_2_TITLE",
            "Pop-up cachée derrière la fenêtre du jeu",
            "Pop-up hidden behind the game window");
        put("BUG_2_FIX",
            "Une fenêtre d'information ou d'avertissement peut s'ouvrir derrière la fenêtre "
            + "principale, le jeu semble alors bloqué. Solution : faites Windows + Tab "
            + "(ou Alt + Tab) et sélectionnez la fenêtre pop-up.",
            "An info or warning window may open behind the main window, making the game look "
            + "frozen. Fix: press Windows + Tab (or Alt + Tab) and pick the pop-up window.");

        // ─────────────────────────── SALLES / MINI-CARTE ────────────────────────────
        put("ROOM_SALON", "Salon", "Living room");
        put("ROOM_PIERRE", "Chambre de Pierre", "Pierre's bedroom");
        put("ROOM_LOUIS", "Chambre de Louis", "Louis's bedroom");
        put("ROOM_JACQUES", "Chambre de Jacques", "Jacques's bedroom");
        put("ROOM_SDB", "Salle de bain", "Bathroom");
        put("ROOM_THOMAS", "Chambre de Thomas", "Thomas's bedroom");
        put("ROOM_PAUL", "Chambre de Paul", "Paul's bedroom");
        put("MAP_PIERRE", "Pierre", "Pierre");
        put("MAP_LOUIS", "Louis", "Louis");
        put("MAP_SDB", "Salle de bain", "Bathroom");
        put("MAP_JACQUES", "Jacques", "Jacques");
        put("MAP_PAUL", "Paul", "Paul");
        put("MAP_THOMAS", "Thomas", "Thomas");
        put("MAP_VOUS", "(vous)", "(you)");
        put("MAP_SALON", "SALON", "LIVING ROOM");

        put("SALON_CORPSE",
            "Salon | Louis est allongé sur le canapé... Il ne bouge plus.",
            "Living room | Louis is lying on the couch... He isn't moving.");
        put("VERRES_HUNT_FMT",
            "Fouille l'appartement : trouve les 5 verres rouges (%d/5)",
            "Search the flat: find the 5 red glasses (%d/5)");

        // ─────────────────────────── MESSAGES D'ACCÈS REFUSÉ ────────────────────────
        put("ACCES_SAVOIR_PASSE",
            "Je dois d'abord savoir ce qu'il s'est passé...",
            "I first need to find out what happened...");
        put("ACCES_THOMAS",
            "Pourquoi aurais-je besoin de fouiller ma chambre ?",
            "Why would I need to search my own room?");
        put("ACCES_PAUL",
            "Rien ne le suspecte à présent, d'autant qu'il n'était pas là cette nuit...",
            "Nothing points to him for now, especially since he wasn't here last night...");
        put("ACCES_TROUVER_SUSPECT",
            "Je dois d'abord trouver qui est suspect...",
            "I first need to figure out who is a suspect...");
        put("ACCES_INDICES_CORPS",
            "Il doit y avoir des indices plus importants à côté de son corps...",
            "There must be more important clues next to his body...");
        put("ACCES_PIERRE_CACHE",
            "Voyons d'abord ce que cache Pierre...",
            "Let's first see what Pierre is hiding...");
        put("ACCES_DEVERROUILLER_PORTE",
            "Je dois d'abord déverrouiller la porte.",
            "I first need to unlock the door.");
        put("ACCES_LOUIS_INDICES",
            "La chambre de Louis doit contenir des indices...",
            "Louis's bedroom must hold some clues...");
        put("ACCES_SDB_INDICES",
            "Il doit y avoir des indices dans la salle de bain...",
            "There must be clues in the bathroom...");
        put("ACCES_TEL_PLUS_TARD",
            "Le téléphone sonne, j'y enquêterai plus tard...",
            "The phone is ringing, I'll investigate later...");
        put("ACCES_TEL_REPONDRE",
            "Le téléphone sonne, je ferais mieux de répondre...",
            "The phone is ringing, I'd better answer...");
        put("ACCES_PAS_MAINTENANT", "Pas maintenant.", "Not now.");

        // ─────────────────────────── INDICES / INFOS ────────────────────────────────
        put("INDICE_POSTIT",
            "Un post-it sur le mur : « Quatre colocs te cherchent depuis six heures, mais à neuf heures, il n'en restera qu'un. ».",
            "A sticky note on the wall: \"Four flatmates have been looking for you since six, but by nine only one will remain.\".");
        put("INDICE_LAMPE_UV",
            "Vous récupérez la lampe UV posée près de la baignoire.",
            "You pick up the UV lamp left near the bathtub.");
        put("INFO_TACHE_SERVIETTE",
            "Sous la lampe UV, une tache fluorescente apparaît sur la serviette de Jacques (Ja.). Une preuve troublante...",
            "Under the UV lamp, a fluorescent stain appears on Jacques's towel (Ja.). A troubling piece of evidence...");
        put("INDICE_SERVIETTES_INCOMPLET",
            "Vous quittez la salle de bain sans avoir balayé toutes les serviettes... mais l'enquête doit avancer.",
            "You leave the bathroom without scanning every towel... but the investigation must go on.");
        put("INDICE_SERVIETTES_LISTE",
            "5 serviettes brodées d'initiales (Ja., Pa., Pi., Lo., Th.)",
            "5 towels embroidered with initials (Ja., Pa., Pi., Lo., Th.)");
        put("INDICE_SERVIETTES_SOMBRE_SUFFIX",
            " — il fait trop sombre pour distinguer d'éventuelles traces. Une lampe UV serait précieuse.",
            " — it's too dark to make out any traces. A UV lamp would be precious.");
        put("INFO_EMPREINTE_PIERRE",
            "Empreinte de Pierre confirmée. Direction sa chambre !",
            "Pierre's fingerprint confirmed. Off to his bedroom!");
        put("AVERT_COUPURE",
            "Vous êtes connecté à la session de Louis. Mais soudain... Le courant est coupé.",
            "You log into Louis's session. But suddenly... the power goes out.");
        put("INFO_COURANT_RETABLI", "Le courant est rétabli !", "The power is back on!");
        put("INFO_TIROIR_FIOLE",
            "Le tiroir s'ouvre. Une fiole de poison vide est dissimulée à l'intérieur... Voilà l'arme du crime !",
            "The drawer opens. An empty vial of poison is hidden inside... There's the murder weapon!");

        // Titres de pop-ups
        put("TITRE_INFO", "Info", "Info");
        put("TITRE_INDICE", "Indice", "Clue");
        put("TITRE_INDICE_REVELE", "Indice révélé", "Clue revealed");
        put("TITRE_OBJET_TROUVE", "Objet trouvé", "Item found");
        put("TITRE_INDICE_SUSPECT", "Indice suspect", "Suspicious clue");
        put("TITRE_ARMOIRE_VERROUILLEE", "Armoire verrouillée", "Cabinet locked");
        put("TITRE_COUPURE", "Coupure de courant", "Power outage");

        // ─────────────────────────── DIALOGUES (CINÉMATIQUES) ───────────────────────
        // Discussion Louis / Pierre (3.6)
        put("Message_Discussion_1",
            "Une conversation est ouverte sur l'ordinateur, entre Louis et Pierre.",
            "A conversation is open on the computer, between Louis and Pierre.");
        put("Message_Discussion_2",
            "Louis : « Tu ne peux pas continuer à prendre ces cachets comme ça, Pierre. »",
            "Louis: \"You can't keep taking those pills like this, Pierre.\"");
        put("Message_Discussion_3",
            "Pierre : « Tu ne sais rien de ce que je vis. Lâche-moi. »",
            "Pierre: \"You know nothing about what I'm going through. Leave me alone.\"");
        put("Message_Discussion_4",
            "Louis : « Si tu ne m'écoutes pas, j'en parle à Jacques demain. »",
            "Louis: \"If you won't listen, I'm telling Jacques tomorrow.\"");
        // Indice ajouté à la SORTIE du PC (mène au livre de chimie)
        put("Info_Cherche_Livre_Chimie",
            "Cette dispute parlait des cachets de Pierre... Louis gardait un livre de chimie dans sa chambre. "
            + "Je devrais l'ouvrir pour savoir si ces médicaments sont vraiment dangereux.",
            "That argument was about Pierre's pills... Louis kept a chemistry book in his bedroom. "
            + "I should open it to find out whether those meds are really dangerous.");

        // Livre de chimie (3.7) — disculpe Pierre
        put("Message_Chimie_1",
            "Un livre de chimie est posé sur l'étagère de Louis.",
            "A chemistry book lies open on Louis's shelf.");
        put("Message_Chimie_2",
            "Une page est annotée : les cachets retrouvés chez Pierre ne sont que des anxiolytiques, inoffensifs à dose normale.",
            "A page is annotated: the pills found in Pierre's room are merely anxiety meds, harmless at a normal dose.");
        put("Message_Chimie_3",
            "Ce n'est donc pas ce type de poison qui a tué Louis... Pierre n'est pas le coupable.",
            "So these aren't the poison that killed Louis... Pierre isn't the culprit.");
        put("Message_Chimie_4",
            "Mais alors, d'où vient le vrai poison ? La salle de bain mérite une fouille...",
            "But then, where does the real poison come from? The bathroom deserves a search...");

        // Téléphone / répondeur (4.x)
        put("Message_Tel_1",
            "Vous décrochez. C'est un message vocal en différé.",
            "You pick up. It's a delayed voicemail.");
        put("Message_Tel_2",
            "Jacques : « Paul t'en veut, on en parle quand je serai rentré. »",
            "Jacques: \"Paul is angry with you, we'll talk about it when I'm back.\"");
        put("Message_Tel_3", "Une piste de plus...", "One more lead...");
        put("Message_Tel_4", "Direction la salle de bain.", "Time to check the bathroom.");

        // Paul rentre (5.1)
        put("Message_Cinematique_Paul_1",
            "Paul rentre enfin à l'appartement.",
            "Paul finally comes back to the flat.");
        put("Message_Cinematique_Paul_2",
            "Paul : « J'ai eu le message de Jacques... Oui, on s'est disputés, mais je n'ai rien à voir avec la mort de Louis. »",
            "Paul: \"I got Jacques's message... Yes, we argued, but I have nothing to do with Louis's death.\"");
        put("Message_Cinematique_Paul_3",
            "Paul : « Si quelqu'un cache quelque chose, c'est dans la chambre de Jacques. Allons la fouiller. »",
            "Paul: \"If anyone is hiding something, it's in Jacques's bedroom. Let's go search it.\"");

        // Révélations finales (6.1) — 8 répliques (ne pas changer le nombre)
        put("Message_Revelation_1",
            "Jacques rentre dans la chambre.",
            "Jacques walks into the room.");
        put("Message_Revelation_2",
            "Jacques : « Ça suffit. C'est moi qui ai parlé à Paul de Louis. »",
            "Jacques: \"Enough. I'm the one who told Paul about Louis.\"");
        put("Message_Revelation_3",
            "Viens, on va s'expliquer dans le salon, tout le monde est rentré.",
            "Come on, let's sort this out in the living room, everyone is back.");
        put("Message_Revelation_4",
            "Mais... pourquoi mentir sur le poison ?",
            "But... why lie about the poison?");
        put("Message_Revelation_5",
            "Vous repensez à la nuit dernière. À cette dispute avec Louis.",
            "You think back to last night. To that argument with Louis.");
        put("Message_Revelation_6",
            "Au verre que vous avez préparé. Au geste que vous avez refusé d'admettre.",
            "To the glass you prepared. To the act you refused to admit.");
        put("Message_Revelation_7",
            "Vous comprenez. C'est vous qui l'avez fait.",
            "You understand. You are the one who did it.");
        put("Message_Revelation_8",
            "Le silence retombe.",
            "Silence falls again.");

        // Divers jeu
        put("DIALOGUE_CONTINUER", "[ Cliquer pour continuer > ]", "[ Click to continue > ]");
        put("HINT_VOIR_LOUIS",
            "Je devrais d'abord aller voir ce qu'a Louis sur le canapé...",
            "I should first go check what's wrong with Louis on the couch...");

        // Crédits
        put("CREDITS_SUB", "— Crédits —", "— Credits —");
        put("CREDITS_RETURN", "[ Cliquer pour revenir au menu ]", "[ Click to return to the menu ]");
        put("CREDITS_INFO_1",
            "INSA Hauts-de-France — Sciences et Humanités pour l'Ingénieur (2A)",
            "INSA Hauts-de-France — Sciences and Humanities for Engineering (2nd year)");
        put("CREDITS_INFO_2",
            "SAE Génie Logiciel — Année 2025-2026",
            "Software Engineering Project — Year 2025-2026");
        put("REALISATION & DEVELOPPEMENT",
            "RÉALISATION & DÉVELOPPEMENT", "DESIGN & DEVELOPMENT");

        // Placeholders de pièces
        put("PH_SDB_SUB",
            "Recherchez la lampe UV et examinez les serviettes.",
            "Look for the UV lamp and examine the towels.");
        put("PH_THOMAS_SUB",
            "(Pourquoi aurais-je besoin de fouiller ma chambre ?)",
            "(Why would I need to search my own room?)");
        put("PH_PAUL_SUB",
            "(Rien ne le suspecte à présent...)",
            "(Nothing points to him for now...)");
        put("PH_DEFAULT_TITLE", "Salle", "Room");
        put("PH_LAMPE_PRISE", "Lampe UV (prise)", "UV lamp (taken)");
        put("PH_LAMPE", "Lampe UV", "UV lamp");
        put("PH_SERVIETTES_VUES", "Serviettes examinées", "Towels examined");
        put("PH_SERVIETTES", "Serviettes", "Towels");

        // ─────────────────────────── EnigmeVerre ────────────────────────────────────
        put("VERRE_LOUIS_1",
            "Louis... ? Oh non, il ne respire plus. Son corps est déjà froid...",
            "Louis...? Oh no, he's not breathing. His body is already cold...");
        put("VERRE_LOUIS_2",
            "Regarde ses lèvres... elles ont une étrange teinte bleutée. Un empoisonnement ? C'est impensable...",
            "Look at his lips... they have a strange bluish tint. Poisoning? That's unthinkable...");
        put("VERRE_LOUIS_3",
            "Je n'ai pas le choix. Je dois fouiller l'appartement et trouver les indices qui mèneront au coupable.",
            "I have no choice. I must search the flat and find the clues that lead to the culprit.");
        put("VERRE_IND_0",
            "Une trace de lèvres grasse... Ce verre appartient à Jacques.",
            "A greasy lip mark... This glass belongs to Jacques.");
        put("VERRE_IND_1",
            "Ce verre sent fortement le soda tiède. C'est le mien.",
            "This glass strongly smells of warm soda. It's mine.");
        put("VERRE_IND_2",
            "Ce verre de jus est intact et propre. Paul n'y a pas touché.",
            "This glass of juice is clean and untouched. Paul didn't drink from it.");
        put("VERRE_IND_3",
            "De la poudre blanche s'est déposée au fond... C'est le verre empoisonné de Louis !",
            "White powder has settled at the bottom... This is Louis's poisoned glass!");
        put("VERRE_IND_4",
            "Des empreintes digitales très nettes entourent ce verre... Ce sont celles de Pierre.",
            "Very clear fingerprints surround this glass... They are Pierre's.");

        // ─────────────────────────── Chambre de Pierre ──────────────────────────────
        put("PIERRE_ARMOIRE_VERROUILLEE",
            "L'armoire à pharmacie est verrouillée par un cadenas.\nIl me faut une clé pour l'ouvrir.",
            "The medicine cabinet is locked with a padlock.\nI need a key to open it.");
        put("PIERRE_CLE_TROUVEE",
            "Vous avez récupéré la clé de l'armoire à pharmacie !",
            "You picked up the key to the medicine cabinet!");
        put("PIERRE_MEDICAMENTS",
            "Des boîtes de médicaments... L'étiquette indique « anxiolytiques — un surdosage peut être mortel ».\n"
            + "Pierre cacherait-il quelque chose ? Il faudrait en savoir plus avant de l'accuser.",
            "Boxes of medication... The label reads \"anxiety meds — an overdose can be fatal\".\n"
            + "Is Pierre hiding something? I should learn more before accusing him.");

        // ─────────────────────────── Descriptions de phases ─────────────────────────
        put("PHASE_DESC_0", "Réveil sans Louis.", "Waking up without Louis.");
        put("PHASE_DESC_1", "Un empoisonnement ?", "A poisoning?");
        put("PHASE_DESC_2", "Pierre, que peut-il bien cacher ?", "Pierre, what could he be hiding?");
        put("PHASE_DESC_31", "Trouver comment entrer dans la chambre de Louis.", "Find a way into Louis's bedroom.");
        put("PHASE_DESC_32", "Ordinateur de Louis.", "Louis's computer.");
        put("PHASE_DESC_33", "Coupure de courant.", "Power outage.");
        put("PHASE_DESC_34", "1/2 Remettre le courant.", "1/2 Restore the power.");
        put("PHASE_DESC_35", "2/2 Remettre le courant.", "2/2 Restore the power.");
        put("PHASE_DESC_36", "Discussion.", "Conversation.");
        put("PHASE_DESC_37", "Un peu de chimie.", "A bit of chemistry.");
        put("PHASE_DESC_41", "Téléphone.", "Phone.");
        put("PHASE_DESC_42", "Répondeur.", "Voicemail.");
        put("PHASE_DESC_43", "Enquête dans la salle de bain.", "Investigate the bathroom.");
        put("PHASE_DESC_51", "Paul rentre à l'appartement.", "Paul comes back to the flat.");
        put("PHASE_DESC_52", "Chambre de Jacques.", "Jacques's bedroom.");
        put("PHASE_DESC_61", "Révélations.", "Revelations.");
        put("PHASE_DESC_71", "Crédits", "Credits");

        // ─────────────────────────── Énigmes (titres / statuts / boutons) ───────────
        put("BTN_VALIDER", "Valider", "Confirm");
        put("BTN_CONTINUER", "Continuer", "Continue");

        // OrdiLouisUI
        put("ORDI_TITLE", "Ordinateur de Louis - Verrouillé", "Louis's computer - Locked");
        put("ORDI_STATUS",
            "Saisissez le code à 4 chiffres affiché sur le post-it de Louis, puis validez.",
            "Enter the 4-digit code shown on Louis's sticky note, then confirm.");
        put("ORDI_ACCES_OK", "Accès autorisé. Vous lisez l'écran...", "Access granted. You read the screen...");
        put("ORDI_CODE_REFUSE", "Code refusé.", "Code rejected.");
        put("ORDI_TERM_1", "Louis@INSA:~$ login --code", "Louis@INSA:~$ login --code");
        put("ORDI_TERM_2", "> ENTRER LE CODE D'ACCÈS...", "> ENTER ACCESS CODE...");
        put("ORDI_TERM_INDICE", "> Indice : post-it", "> Hint: sticky note");

        // RotaryDialUI
        put("ROTARY_TITLE", "Verrou rotatif - Porte de Louis", "Rotary lock - Louis's door");
        put("ROTARY_STATUS", "Activez tous les boutons.", "Activate every button.");
        put("ROTARY_OK", "Porte déverrouillée !", "Door unlocked!");
        put("ROTARY_ERROR_LOAD", "Erreur : énigme non chargée.", "Error: puzzle not loaded.");
        put("ROTARY_HINT_EASY",
            "Cliquez sur un bouton du cadran pour avancer. Cliquer sur un autre bouton pour réinitialiser.",
            "Click a dial button to advance. Click another button to reset.");

        // OndesUI
        put("ONDES_TITLE", "Décrocher - Analyse du signal", "Answer - Signal analysis");
        put("ONDES_STATUS_FMT",
            "Caler le signal du joueur (bleu) sur la cible (rouge). Difficulté : %s",
            "Match the player signal (blue) to the target (red). Difficulty: %s");
        put("ONDES_OK", "Signal synchronisé. Vous décrochez : « Allô ? »", "Signal synced. You answer: \"Hello?\"");
        put("ONDES_FAIL", "Le signal ne correspond pas encore.", "The signal doesn't match yet.");
        put("ONDES_BTN", "Décrocher (tester le signal)", "Answer (test the signal)");
        put("ONDES_CIBLE", "● Cible", "● Target");
        put("ONDES_VOUS", "● Vous", "● You");
        put("ONDES_DIFF_FMT", "Difficulté : %s  —  %d potard(s)", "Difficulty: %s  —  %d knob(s)");
        put("ONDES_SIGNAL", "Signal", "Signal");
        put("ONDES_ONDE_FMT", "Onde %d", "Wave %d");

        // MovingLightsUI
        put("LIGHTS_TITLE", "Tableau électrique - Ampoules", "Fuse box - Light bulbs");
        put("LIGHTS_STATUS", "Déplacez les ampoules sur les rails.", "Slide the bulbs along the rails.");
        put("LIGHTS_OK", "Toutes les lampes sont alimentées : courant rétabli !", "Every bulb is powered: power restored!");
        put("LIGHTS_HINT_EASY",
            "Glissez-déposez les ampoules. Celles adjacentes de couleurs différentes s'éteignent.",
            "Drag and drop the bulbs. Adjacent bulbs of different colors switch off.");

        // LogicGateUI
        put("LOGIC_TITLE", "Tableau électrique - Portes logiques", "Fuse box - Logic gates");
        put("LOGIC_STATUS", "Basculez les interrupteurs pour ALLUMER la lampe finale.", "Flip the switches to turn ON the final lamp.");
        put("LOGIC_OK", "Lampe FINALE allumée : courant rétabli !", "FINAL lamp lit: power restored!");
        put("LOGIC_RESET", "Tout remettre à OFF", "Reset everything to OFF");
        put("LOGIC_FINAL_FMT", "Lampe finale : %s", "Final lamp: %s");

        // MovingBallsUI
        put("BALLS_TITLE", "Verrou à billes - Tiroir de Jacques", "Marble lock - Jacques's drawer");
        put("BALLS_STATUS",
            "Cliquez sur les boutons périphériques pour faire glisser les billes jusqu'aux cibles.",
            "Click the outer buttons to slide the marbles onto the targets.");
        put("BALLS_HINT",
            "Toutes les billes doivent reposer sur une case « objectif ».",
            "Every marble must rest on a \"goal\" tile.");

        // UVLampUI
        put("UVLAMP_TITLE", "Lampe UV - Plafond de Jacques", "UV lamp - Jacques's ceiling");
        put("UVLAMP_STATUS", "Vous balayez le plafond avec la lampe UV...", "You scan the ceiling with the UV lamp...");
        put("UVLAMP_REVEAL", "Marquage révélé : tiroir B4.", "Marking revealed: drawer B4.");

        // UVLampServiettesUI
        put("SERV_TITLE", "Lampe UV - Serviettes de la salle de bain", "UV lamp - Bathroom towels");
        put("SERV_STATUS", "Balayez les serviettes avec la lampe UV...", "Scan the towels with the UV lamp...");
        put("SERV_OK", "Tache identifiée sur la serviette de Jacques (Ja.).", "Stain identified on Jacques's towel (Ja.).");
        put("SERV_QUIT", "Vous quittez la salle de bain.", "You leave the bathroom.");
        put("SERV_TACHE", "Tache suspecte", "Suspicious stain");
        put("SERV_BTN_VU", "J'ai vu — Continuer", "I saw it — Continue");

        // EnigmaDialog
        put("BTN_QUITTER_ENIGME", "Quitter l'énigme", "Quit puzzle");

        // EnigmeEmpreinteUI
        put("EMP_TITLE", "Laboratoire d'Analyse Criminelle - Empreintes", "Forensic Analysis Lab - Fingerprints");
        put("EMP_HEADER",
            "STATION D'ANALYSE : RECONSTITUER L'ÉCHANTILLON PUIS CHERCHER LA CORRESPONDANCE",
            "ANALYSIS STATION: REASSEMBLE THE SAMPLE THEN FIND THE MATCH");
        put("EMP_VERRE_TITLE", " ÉCHANTILLON : VERRE (À RECONSTITUER) ", " SAMPLE: GLASS (TO REASSEMBLE) ");
        put("EMP_FICHIER_SEL", " FICHIER SÉLECTIONNÉ ", " SELECTED FILE ");
        put("EMP_LOCKED", "Verrouillé : Reconstituez d'abord le puzzle.", "Locked: reassemble the puzzle first.");
        put("EMP_DB", "BASE DE DONNÉES :", "DATABASE:");
        put("EMP_FICHIER_FMT", "Fichier : %s", "File: %s");
        put("EMP_CONFIRM", "CONFIRMER ACCUSATION", "CONFIRM ACCUSATION");
        put("EMP_RECONSTITUTED",
            "Structure de l'échantillon reconstituée avec succès !\nBase de données déverrouillée.",
            "Sample structure successfully reassembled!\nDatabase unlocked.");
        put("EMP_ANALYZER", "Analyseur d'Empreintes", "Fingerprint Analyzer");
        put("EMP_VERRE_DONE", " ÉCHANTILLON RECONSTITUÉ ", " SAMPLE REASSEMBLED ");
        put("EMP_SELECT_SUSPECT", "Sélectionnez un suspect à comparer", "Select a suspect to compare");
        put("EMP_DB_UNLOCKED", " BASE DÉVERROUILLÉE ", " DATABASE UNLOCKED ");
        put("EMP_RELEVE_FMT", " RELEVÉ : %s ", " RECORD: %s ");
        put("EMP_NO_SELECT", "Veuillez d'abord sélectionner un fichier de suspect.", "Please select a suspect file first.");
        put("EMP_NO_SELECT_TITLE", "Analyse impossible", "Analysis not possible");
        put("EMP_FAIL",
            "ANALYSE : Correspondance négative.\nLes minuties et bifurcations des lignes ne correspondent pas.",
            "ANALYSIS: Negative match.\nThe minutiae and ridge bifurcations do not match.");
        put("EMP_FAIL_TITLE", "Échec de l'alignement", "Alignment failed");
    }

    // ─────────────────────────── Anciennes entrées de menu ──────────────────────────
    private static final Map<String, String> EN = Map.ofEntries(
        Map.entry("MENU", "MENU"),
        Map.entry("LANCER UNE PARTIE", "PLAY"),
        Map.entry("PARAMETRES", "SETTINGS"),
        Map.entry("LISTE DES SCORES", "SCOREBOARD"),
        Map.entry("MEILLEURS TEMPS", "BEST TIMES"),
        Map.entry("RETOUR MENU", "BACK TO MENU"),
        Map.entry("RETOUR AU JEU", "BACK TO GAME"),
        Map.entry("RETOUR JEU", "BACK TO GAME"),
        Map.entry("SAUVEGARDES", "SAVES"),
        Map.entry("LANCER UNE NOUVELLE PARTIE", "NEW GAME"),
        Map.entry("NOUVELLE PARTIE", "NEW GAME"),
        Map.entry("NOM_SAUVEGARDE", "SAVE'S NAME"),
        Map.entry("NOM_SAUVEGARDE:", "SAVE'S NAME:"),
        Map.entry("DATE", "DATE"),
        Map.entry("TEMPS", "TIME"),
        Map.entry("NOM", "NAME"),
        Map.entry("LUMINOSITE", "BRIGHTNESS"),
        Map.entry("SON", "SOUND"),
        Map.entry("LANGUES", "LANGUAGE"),
        Map.entry("CREER", "CREATE"),
        Map.entry("RETOUR LISTE", "BACK TO LIST"),
        Map.entry("DIFFICULTE", "DIFFICULTY"),
        Map.entry("Normale", "Normal"),
        Map.entry("Facile", "Easy"),
        Map.entry("Difficile", "Hard"),
        Map.entry("MA_PARTIE", "MY_GAME"),
        Map.entry("JOUEUR", "PLAYER"),
        Map.entry("NOM_JOUEUR", "PLAYER_NAME"),
        Map.entry("NOM_JOUEUR:", "PLAYER_NAME:"),
        Map.entry("INFORMATIONS_INVALIDES", "INVALID_INFORMATIONS"),
        Map.entry("SAUVEGARDE_EXISTE", "SAVE_EXISTS"),
        Map.entry("SAUVEGARDE_INVALIDE", "INVALID_SAVE"),
        Map.entry("LANCER PARTIE", "LAUNCH GAME"),
        Map.entry("TRIER PAR", "SORT BY"),
        Map.entry("AUCUNE SAUVEGARDE", "NO SAVE"),
        Map.entry("SELECTIONNEZ UNE PARTIE", "SELECT A GAME"),
        Map.entry("MON PROPRE ENNEMI", "MY OWN ENEMY"),
        Map.entry("Mon Propre Ennemi", "My Own Enemy"),
        Map.entry("TOUTES", "ALL"),
        Map.entry("RECHERCHER...", "SEARCH..."),
        Map.entry("AUCUNE SELECTION", "NO SELECTION"),
        Map.entry("AUCUN SELECTION", "NO SELECTION"),
        Map.entry("SUPPRIMER PARTIE", "DELETE SAVE"),
        Map.entry("OUVERT", "OPENED"),
        Map.entry("TIROIR", "DRAWER")
    );
    private static final Map<String, String> FR = Map.ofEntries(
        Map.entry("ALL", "TOUTES"),
        Map.entry("Easy", "Facile"),
        Map.entry("Normal", "Normale"),
        Map.entry("Hard", "Difficile"),
        Map.entry("MY OWN ENEMY", "MON PROPRE ENNEMI"),
        Map.entry("My Own Enemy", "Mon Propre Ennemi")
    );

    public static String toEN(String key){
        return EN.getOrDefault(key, key);
    }

    /**
     * Traduit une clé. Cherche d'abord dans la table {@link #GAME} (clés
     * abstraites), puis retombe sur les anciennes tables de menu.
     */
    public static String t(String key) {
        if (key == null) return "";
        String[] g = GAME.get(key);
        if (g != null) {
            return isEnglish() ? g[1] : g[0];
        }
        if (isEnglish()) {
            return EN.getOrDefault(key, key);
        }
        return FR.getOrDefault(key, key);
    }
}
