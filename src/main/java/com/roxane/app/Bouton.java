package com.roxane.app;

import javafx.animation.ScaleTransition;
import javafx.scene.control.Button;
import javafx.util.Duration;

public class Bouton extends Button {

    public Bouton(String texte) {
        super(texte);
        getStyleClass().add("menu-button");
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
        });
    }
}
