package com.sae;

import com.sae.game.Jeu;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Vérifie que {@link Jeu} peut être instancié et que son {@code JPanel}
 * interne est correctement initialisé — sans aucune fenêtre visible.
 * C'est exactement ce que fait JavaFX via {@code SwingNode.setContent()}.
 *
 * Exécution : java -Djava.awt.headless=true -cp ... com.sae.JeuPanelSmokeTest
 */
public class JeuPanelSmokeTest {

    public static void main(String[] args) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<JPanel> panelRef = new AtomicReference<>();
        AtomicReference<Throwable> errorRef = new AtomicReference<>();

        SwingUtilities.invokeLater(() -> {
            try {
                Jeu jeu = new Jeu(null);
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
