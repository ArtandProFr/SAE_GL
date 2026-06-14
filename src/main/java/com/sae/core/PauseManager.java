package com.sae.core;

public class PauseManager {
    public boolean isPaused = false;
    public PauseManager(){
        isPaused = false;
    }
    public void setStatus(boolean state){
        isPaused = state;
    }
    public void switchStatus(){
        isPaused = !isPaused;
    }
}
