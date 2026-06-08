package com.sae.enigmas;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.WindowConstants;

/**
 * Base commune à toutes les fenêtres d'énigmes : un dialog modal contenant
 * un canvas (panneau) qui appelle paintEnigme(g, w, h) et qui transmet
 * la position de la souris + clics via les méthodes onMouse...().
 * <p>
 * Les sous-classes doivent appeler markSolvedAndClose() lorsque l'énigme
 * est résolue : isReussite() renverra alors true.
 */
public abstract class EnigmaDialog extends JDialog {

    protected boolean reussite = false;
    protected Vec2 mousePos = new Vec2(0, 0);
    protected boolean leftPressed = false;
    private boolean leftJustPushed = false;
    private boolean leftJustReleased = false;

    protected final JPanel canvas;
    protected final Timer ticker;
    protected final JLabel statusLabel;

    protected EnigmaDialog(java.awt.Window parent, String titre, int largeur, int hauteur) {
        super(parent, titre, ModalityType.APPLICATION_MODAL);
        setSize(largeur, hauteur);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);

        statusLabel = new JLabel(" ", JLabel.CENTER);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        statusLabel.setOpaque(true);
        statusLabel.setBackground(new Color(28, 30, 36));
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setPreferredSize(new Dimension(largeur, 28));
        add(statusLabel, BorderLayout.NORTH);

        canvas = new Canvas();
        canvas.setBackground(new Color(18, 20, 24));
        add(canvas, BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        bottom.setBackground(new Color(28, 30, 36));
        JButton btnAbandon = new JButton("Quitter l'énigme");
        btnAbandon.setBackground(new Color(192, 57, 43));
        btnAbandon.setForeground(Color.WHITE);
        btnAbandon.setFocusPainted(false);
        btnAbandon.addActionListener(e -> dispose());
        bottom.add(btnAbandon);
        add(bottom, BorderLayout.SOUTH);

        canvas.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    leftPressed = true;
                    leftJustPushed = true;
                    mousePos = new Vec2(e.getX(), e.getY());
                    onMousePressed(mousePos);
                    canvas.repaint();
                }
            }
            @Override public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    leftPressed = false;
                    leftJustReleased = true;
                    onMouseReleased(mousePos);
                    canvas.repaint();
                }
            }
        });
        canvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                mousePos = new Vec2(e.getX(), e.getY());
                onMouseMoved(mousePos);
                updateCursor();
            }
            @Override public void mouseDragged(MouseEvent e) {
                mousePos = new Vec2(e.getX(), e.getY());
                onMouseDragged(mousePos);
                canvas.repaint();
            }
        });

        ticker = new Timer(30, e -> {
            tick();
            leftJustPushed = false;
            leftJustReleased = false;
            canvas.repaint();
        });
    }

    /** Démarrage automatique du tick à la première ouverture. */
    @Override
    public void setVisible(boolean b) {
        if (b) ticker.start();
        else   ticker.stop();
        super.setVisible(b);
    }

    /** À surcharger pour le rendu de l'énigme. */
    protected abstract void paintEnigme(Graphics2D g, int w, int h);

    /** Hook tick (logique animation/IA). Par défaut rien. */
    protected void tick() {}

    /** Hooks souris : sous-classes peuvent surcharger. */
    protected void onMousePressed(Vec2 p) {}
    protected void onMouseReleased(Vec2 p) {}
    protected void onMouseMoved(Vec2 p) {}
    protected void onMouseDragged(Vec2 p) {}

    /** Indique le résultat puis ferme proprement. */
    protected void markSolvedAndClose() {
        reussite = true;
        // Petite tempo pour laisser apparaître l'état résolu
        Timer t = new Timer(550, e -> dispose());
        t.setRepeats(false);
        t.start();
    }

    public boolean isReussite() { return reussite; }

    protected boolean wasJustPushed() { return leftJustPushed; }
    protected boolean wasJustReleased() { return leftJustReleased; }

    protected void updateCursor() {
        canvas.setCursor(Cursor.getPredefinedCursor(
                isCursorInteractive(mousePos) ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
    }

    /** Override pour faire varier le curseur main/normal. */
    protected boolean isCursorInteractive(Vec2 p) { return false; }

    protected void setStatus(String txt, Color bg) {
        statusLabel.setText(txt);
        statusLabel.setBackground(bg != null ? bg : new Color(28, 30, 36));
    }

    protected class Canvas extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            Draw.setupQuality(g2);
            g2.setStroke(new BasicStroke(1f));
            paintEnigme(g2, getWidth(), getHeight());
            g2.dispose();
        }
    }
}
