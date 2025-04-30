package java_dungeon.controllers;

import java_dungeon.main.App;
import java_dungeon.main.Globals;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.io.File;

public class StartController {
    @FXML
    private Button continueBtn;
    @FXML
    private Label dungeonLvlHigh;
    @FXML
    private Label playerLvlHigh;

    @FXML
    void initialize() {
        File saveFile = new File("saveFile.txt");
        if (saveFile.exists()) {
            continueBtn.setDisable(false);
        }

        dungeonLvlHigh.setText(String.format("Highest Floor: %d", Globals.floorHigh + 1));
        playerLvlHigh.setText(String.format("Highest Player Level: %d", Globals.playerLvlHigh));
    }



    @FXML
    void continueGame() {
        App.loadScene("game");
    }

    @FXML
    void newGame() {
        // Delete the save file if it exists
        File saveFile = new File("saveFile.txt");
        if (saveFile.exists() && saveFile.delete()) {
            System.out.println("Deleting old save file and starting a new game...");
        }

        App.loadScene("game");
    }
}
