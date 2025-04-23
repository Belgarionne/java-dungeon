package java_dungeon.main;

import java_dungeon.controllers.ControllerBase;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {
    private static Stage stage;

    @Override
    public void start(Stage stage) throws IOException {
        App.stage = stage;

        // Initialize assets
        AssetManager.initialize();

        // Load the game scene
        loadScene("game");

        // Set up the window
        stage.setTitle("Java Dungeon");
        stage.show();
        stage.requestFocus();
    }

    public static Scene loadScene(String fxml) throws IOException {
        // Make the fxml loader
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getClassLoader().getResource(fxml + ".fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        // Set up the controller after the scene has been created
        ControllerBase controller = fxmlLoader.getController();
        controller.initializeScene(scene);

        // Update the current scene
        App.stage.setScene(scene);

        return scene;
    }

    public static void main(String[] args) {
        launch();
    }
}