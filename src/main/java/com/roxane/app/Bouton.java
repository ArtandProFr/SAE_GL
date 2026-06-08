package com.roxane.app;

import javafx.animation.ScaleTransition;
import javafx.scene.control.Button;
import javafx.scene.media.AudioClip;
import javafx.util.Duration;

public class Bouton extends Button {

    private static AudioClip clickSound;

    static {
        try {
            // Vérifie que le fichier est bien dans src/main/resources/sounds/click.mp3
            String path = Bouton.class.getResource("/sounds/click.mp3").toExternalForm();
            clickSound = new AudioClip(path);
        } catch (Exception e) {
            System.err.println("Impossible de charger le son du bouton : " + e.getMessage());
        }
    }

    public Bouton(String texte) {
        super(texte);
        getStyleClass().add("menu-button"); // Applique le style par défaut
        setFocusTraversable(false);
        
        ScaleTransition stHover = new ScaleTransition(Duration.millis(120), this);
        stHover.setToX(1.04);
        stHover.setToY(1.04);

        ScaleTransition stNormal = new ScaleTransition(Duration.millis(120), this);
        stNormal.setToX(1.0);
        stNormal.setToY(1.0);

        ScaleTransition stPress = new ScaleTransition(Duration.millis(80), this);
        stPress.setToX(0.97);
        stPress.setToY(0.97);

        setOnMouseEntered(event -> {
            stHover.stop();
            stNormal.stop();
            stHover.playFromStart();
        });
        setOnMouseExited(event -> {
            stHover.stop();
            stNormal.stop();
            stNormal.playFromStart();
        });
        
        setOnMousePressed(event -> {
            stPress.stop();
            stPress.playFromStart();
        });
        
        setOnMouseReleased(event -> {
            if (isHover()) {
                stHover.playFromStart();
            } else {
                stNormal.playFromStart();
            }
            
            // Jouer le son au relâchement du clic
            if (clickSound != null) {
                double volumeGlobal = Settings.getInstance().getVolume();
                // Si ton système utilise 0-100 : 
                volumeGlobal = volumeGlobal / 100.0;
                clickSound.setVolume(volumeGlobal);
                clickSound.play();
            }
        });
    }
}