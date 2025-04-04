package java_dungeon;

import java_dungeon.map.GameMap;
import java_dungeon.objects.Player;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java_dungeon.gui.AutoScalingCanvas;

public class Main extends Application {
    // SNES rendering size
    private static final int SCREEN_WIDTH = 256;
    private static final int SCREEN_HEIGHT = 224;
    private static final double SCREEN_TILE_WIDTH = SCREEN_WIDTH / (double)AssetManager.TILE_SIZE;
    private static final double SCREEN_TILE_HEIGHT = SCREEN_HEIGHT / (double)AssetManager.TILE_SIZE;

    private GameMap map;
    private Player player;

    private AutoScalingCanvas canvas;
    private GraphicsContext ctx;

    private double camX;
    private double camY;

    public Main() {
        this.map = new GameMap();
        this.player = new Player(5, 5);

        camX = 0.0;
        camY = 0.0;
    }

    @Override
    public void start(Stage stage) {
        StackPane root = new StackPane();
        canvas = new AutoScalingCanvas(SCREEN_WIDTH, SCREEN_HEIGHT);
        root.getChildren().add(canvas);

        ctx = canvas.getGraphicsContext2D();
        ctx.setImageSmoothing(false);

        AssetManager.initialize();

        renderGame();

        // Redraw the game if the canvas is resized (window resizing)
        canvas.scalingProperty().addListener((obs) -> renderGame());

        // Basic update loop (Moves the camera 1 unit to the left every second)
        AnimationTimer animator = new AnimationTimer() {
            @Override
            public void handle(long currentTime) {
                camX -= 0.5/60.0;
                renderGame();
            }
        };
        animator.start();

        // Start the window at 3x size
        Scene scene = new Scene(root, SCREEN_WIDTH * 3, SCREEN_HEIGHT * 3);
        scene.setFill(Color.BLACK); // Black background color
        stage.setScene(scene);
        stage.setTitle("Java Dungeon");
        stage.show();
    }

    private void renderGame() {
        // Clear the screen
        ctx.setFill(Color.BLACK);
        ctx.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Apply camera transformations (including render scaling)
        double renderScale = canvas.scalingProperty().get();
        ctx.save();
        ctx.scale(renderScale, renderScale);
        ctx.translate(-camX * AssetManager.TILE_SIZE, -camY * AssetManager.TILE_SIZE);

        renderTiles(); // Draw the tilemap
        renderPlayer(); // Draw the player

        ctx.restore();
    }

    private void renderTiles() {
        String[][] renderMap = map.getRenderMap();

        // Only draw the visible tiles of the tilemap
        // Upper left corner of the screen in tile coordinates (bounded by the tilemap)
        int startX = (int)Math.max(Math.floor(camX), 0);
        int startY = (int)Math.max(Math.floor(camY), 0);

        // Bottom right corner of the screen in tile coordinates (bounded by the tilemap)
        int endX = (int)Math.min(Math.ceil(camX + SCREEN_TILE_WIDTH), map.getWidth());
        int endY = (int)Math.min(Math.ceil(camY + SCREEN_TILE_HEIGHT), map.getHeight());

        AssetManager.AtlasImage tileset = AssetManager.getImages().get("Tileset");

        // Draw the tiles
        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                String tile = renderMap[y][x];
                ctx.drawImage(
                    tileset.getImg(),
                    tileset.getFrame(tile).getMinX(), tileset.getFrame(tile).getMinY(), AssetManager.TILE_SIZE, AssetManager.TILE_SIZE,
                    x * AssetManager.TILE_SIZE, y * AssetManager.TILE_SIZE, AssetManager.TILE_SIZE, AssetManager.TILE_SIZE
                );
            }
        }
    }

    private void renderPlayer() {
        AssetManager.AtlasImage imageSheet = AssetManager.getImages().get("Tileset");
        var playerRect = imageSheet.getFrame("Player");

        ctx.drawImage(imageSheet.getImg(),
            playerRect.getMinX(), playerRect.getMinY(), playerRect.getWidth(), playerRect.getHeight(),
        player.getX() * AssetManager.TILE_SIZE, player.getY() * AssetManager.TILE_SIZE, playerRect.getWidth(), playerRect.getHeight()
        );
    }

    public static void main(String[] args) {
        launch();
    }
}
