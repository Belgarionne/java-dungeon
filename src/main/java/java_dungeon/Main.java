package java_dungeon;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java_dungeon.gui.AutoScalingCanvas;

public class Main extends Application {
    private static final int width = 128;
    private static final int height = 128;

    @Override
    public void start(Stage stage) {
        StackPane root = new StackPane();
        AutoScalingCanvas canvas = new AutoScalingCanvas(width, height);

        root.getChildren().add(canvas);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setImageSmoothing(false);
        Image img = new Image("dungeon-tiles.png");
        redraw(img, gc, canvas.scalingProperty().get());

        canvas.scalingProperty().addListener((o, oldScale, newScale) -> redraw(img, gc, newScale.doubleValue()));

        Scene scene = new Scene(root, width * 5, height * 5);
        scene.setFill(Color.BLACK);
        stage.setScene(scene);
        stage.setTitle("Java Dungeon");
        stage.show();
    }

    private void redraw(Image img, GraphicsContext gc, double scaling) {
        gc.save();
        gc.scale(scaling, scaling);

        gc.setFill(Color.DARKGREEN);
        gc.fillRect(0, 0, 256, 256);

        gc.drawImage(img, 0, 0);

        gc.translate(72, 72);
        gc.rotate(45);
        gc.setFill(Color.BLUE);
        gc.fillRect(-8, -8, 16, 16);
        gc.rotate(-45);
        gc.translate(-72, -72);

        gc.setFill(Color.RED);
        gc.fillRect(32, 64, 16, 16);

        gc.restore();
    }

    public static void main(String[] args) {
        launch();
    }
}
