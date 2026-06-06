package com.roxane.app;

import javafx.scene.Node;
import javafx.scene.effect.ColorAdjust;

public class Settings {
    private static final Settings INSTANCE = new Settings();

    private double brightness = 1.0;
    private double volume = 50.0;
    private String language = "Francais";
    private String difficulty = "Normal";

    private Settings() {}

    public static Settings getInstance() { return INSTANCE; }

    public double getBrightness() { return brightness; }
    public void setBrightness(double v) { brightness = v; }

    public double getVolume() { return volume; }
    public void setVolume(double v) { volume = v; }

    public String getLanguage() { return language; }
    public void setLanguage(String v) { language = v; }

    public String getDifficulty(){ return difficulty; }
    public void setDifficulty(String difficulty){ this.difficulty = difficulty; }

    public void applyBrightness(Node root) {
        ColorAdjust effect = new ColorAdjust();
        effect.setBrightness(brightness - 1.0);
        root.setEffect(effect);
    }
}
