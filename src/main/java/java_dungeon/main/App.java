package java_dungeon.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {
    private static Stage stage;

    @Override
    public void start(Stage stage) {
        App.stage = stage;

        // Initialize assets
        AssetManager.initialize();

        // Load the game scene
        loadScene("start");

        // Set up the window
        stage.setTitle("Java Dungeon");
        stage.show();
        stage.requestFocus();
    }

    public static void loadScene(String fxml) {
        try {
            // Make the fxml loader
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getClassLoader().getResource(fxml + ".fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            // Update the current scene
            App.stage.setScene(scene);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        launch();
    }
}