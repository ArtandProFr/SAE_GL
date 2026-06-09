package com.sae.enigmas;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.sae.core.Save;

/**
 * Phase 3.2 — "Ordinateur de Louis".
 * Le code est un nombre à 4 chiffres trouvé dans la chambre (par défaut 4791).
 */
public class OrdiLouisUI extends EnigmaDialog {

    private static final int W = 540;
    private static final int H = 360;
    public  static final String CODE_LOUIS = "4691";
    private String difficulty;

    private final JTextField field;
    private final Rectangle btnValider = new Rectangle(W/2 - 90, 230, 180, 40);

    public OrdiLouisUI(Window parent, Save save) {
        super(parent, "Ordinateur de Louis - Verrouillé", W, H);

        this.difficulty = save.getDifficulty();
        field = new JTextField();
        field.setBounds(W/2 - 100, 170, 200, 38);
        field.setFont(new Font("Monospaced", Font.BOLD, 22));
        field.setHorizontalAlignment(JTextField.CENTER);
        field.setBackground(new Color(28, 30, 36));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) tenter();
            }
        });
        // Le canvas est en BorderLayout.CENTER ; on bricole un null layout pour superposer.
        SwingUtilities.invokeLater(() -> {
            canvas.setLayout(null);
            canvas.add(field);
            canvas.revalidate();
            canvas.repaint();
            field.requestFocusInWindow();
        });

        setStatus("Saisissez le code à 4 chiffres affiché sur le post-it de Louis, puis validez.",
                new Color(41, 128, 185));
    }

    private void tenter() {
        String s = field.getText().trim();
        if (s.equals(CODE_LOUIS)) {
            setStatus("Accès autorisé. Vous lisez l'écran...", new Color(46, 204, 113));
            markSolvedAndClose();
        } else {
            setStatus("Code refusé.", new Color(192, 57, 43));
        }
    }

    @Override
    protected void onMousePressed(Vec2 p) {
        if (btnValider.contains((int) p.x, (int) p.y)) tenter();
    }

    @Override
    protected boolean isCursorInteractive(Vec2 p) {
        return btnValider.contains((int) p.x, (int) p.y);
    }

    @Override
    protected void paintEnigme(Graphics2D g, int w, int h) {
        // Fond / écran factice
        Draw.rect(g, 30, 30, w - 60, 120, new Color(22, 24, 30));
        Draw.rectOutline(g, 30, 30, w - 60, 120, new Color(60, 70, 85), 2);
        g.setColor(new Color(46, 204, 113));
        g.setFont(new Font("Monospaced", Font.BOLD, 16));
        g.drawString("Louis@INSA:~$ login --code", 50, 70);
        g.drawString("> ENTRER LE CODE D'ACCÈS...", 50, 100);
        if (this.difficulty.equals("Easy")){
            g.drawString("> Indice : post-it", 50, 130);
        }

        // Bouton valider
        g.setColor(new Color(41, 128, 185));
        g.fillRoundRect(btnValider.x, btnValider.y, btnValider.width, btnValider.height, 14, 14);
        g.setStroke(new BasicStroke(2));
        g.setColor(Color.WHITE);
        g.drawRoundRect(btnValider.x, btnValider.y, btnValider.width, btnValider.height, 14, 14);
        g.setFont(new Font("SansSerif", Font.BOLD, 15));
        String t = "Valider";
        int tw = g.getFontMetrics().stringWidth(t);
        g.drawString(t, btnValider.x + (btnValider.width - tw) / 2, btnValider.y + 26);
    }
}
