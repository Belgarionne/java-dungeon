package java_dungeon.controllers;

import javafx.scene.Scene;

public abstract class ControllerBase {
    protected Scene scene;

    // Used mainly to initialize input after the scene has been created
    public void initializeScene(Scene scene) {
        this.scene = scene;
    }
}
