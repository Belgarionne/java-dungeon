package java_dungeon.controllers;

import java_dungeon.main.App;
import javafx.fxml.FXML;

public class StartController {
    @FXML
    void play() {
        App.loadScene("game");
    }
}
