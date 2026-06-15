package com.sae;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.roxane.app.NewGameScreen;
import com.sae.core.Phase;
import com.sae.core.Save;
import com.sae.game.Jeu;

import javafx.stage.Stage;

/**
 * Vérifie que {@link Jeu} peut être instancié et que son {@code JPanel}
 * interne est correctement initialisé — sans aucune fenêtre visible.
 * C'est exactement ce que fait JavaFX via {@code SwingNode.setContent()}.
 *
 * Exécution : java -Djava.awt.headless=true -cp ... com.sae.JeuPanelSmokeTest
 *
 * Le constructeur réel de {@link Jeu} requiert {@code (Save, Phase, Stage,
 * NewGameScreen)} ; pour rester un simple smoke test sans environnement
 * JavaFX, on passe {@code null} pour {@code Stage} et {@code NewGameScreen}.
 * Le code de {@link Jeu} sait gérer un {@link Save} nul (cf.
 * {@code restaurerEtatProgression()} qui retourne immédiatement dans ce cas).
 */
public class JeuPanelSmokeTest {

    public static void main(String[] args) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<JPanel> panelRef = new AtomicReference<>();
        AtomicReference<Throwable> errorRef = new AtomicReference<>();

        SwingUtilities.invokeLater(() -> {
            try {
                Save save = null;                 // non persisté pour le smoke test
                Phase phase = new Phase(0.1);
                Stage stage = null;               // JavaFX non démarré ici
                NewGameScreen parent = null;      // pas de parent JavaFX

                Jeu jeu = new Jeu(save, phase, stage, parent);
                panelRef.set(jeu.getGamePanel());
            } catch (Throwable t) {
                errorRef.set(t);
            } finally {
                latch.countDown();
            }
        });

        if (!latch.await(15, TimeUnit.SECONDS)) {
            System.err.println("FAIL Timeout lors de la creation de Jeu");
            System.exit(1);
        }
        if (errorRef.get() != null) {
            System.err.println("FAIL Exception lors de la creation de Jeu :");
            errorRef.get().printStackTrace();
            System.exit(1);
        }
        if (panelRef.get() == null) {
            System.err.println("FAIL Jeu.getGamePanel() a retourne null");
            System.exit(1);
        }
        System.out.println("OK   Jeu instancie, JPanel pret pour SwingNode : "
                + panelRef.get().getClass().getName()
                + " (composants: " + panelRef.get().getComponentCount() + ")");
    }
}
