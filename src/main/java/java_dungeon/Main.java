package java_dungeon;

import java_dungeon.map.GameMap;
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

    private GameMap map;
    private AutoScalingCanvas canvas;
    private GraphicsContext ctx;

    private double ppu = 16; // Pixels per unit, 16 pixels per game unit
    private double camX;
    private double camY;

    public Main() {
        this.map = new GameMap();
        camX = 0.0;
        camY = 0.5;
    }

    @Override
    public void start(Stage stage) {
        StackPane root = new StackPane();
        canvas = new AutoScalingCanvas(SCREEN_WIDTH, SCREEN_HEIGHT);
        root.getChildren().add(canvas);

        ctx = canvas.getGraphicsContext2D();
        ctx.setImageSmoothing(false);

        renderGame();

        // Redraw the game if the canvas is resized (window resizing)
        canvas.scalingProperty().addListener((o, oldScale, newScale) -> renderGame());

        // Basic update loop (Moves the camera 1 unit to the left every second)
        AnimationTimer animator = new AnimationTimer() {
            @Override
            public void handle(long currentTime) {
                camX -= 1.0/60.0;
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
        ctx.translate(-camX * ppu, -camY * ppu);

        // Draw the tilemap
        renderTiles();

        ctx.restore();
    }

    private void renderTiles() {
        int[][] renderMap = map.getRenderMap();

        // Assume the map width and height from the first row
        int w = renderMap[0].length;
        int h = renderMap.length;

        // Start drawing from the map position
        ctx.translate(map.getOriginX() * ppu, map.getOriginY() * ppu);

        // Only draw the visible tiles of the tilemap
        // Upper left corner of the screen in tile coordinates (bounded by the tilemap)
        int startX = (int)Math.max(Math.floor(((camX - map.getOriginX()) * ppu) / GameMap.TILE_SIZE), 0);
        int startY = (int)Math.max(Math.floor(((camY - map.getOriginY()) * ppu) / GameMap.TILE_SIZE), 0);

        // Bottom right corner of the screen in tilemap coordinates (bounded by the tilemap)
        int endX = (int)Math.min(Math.floor(((camX - map.getOriginX()) * ppu + SCREEN_WIDTH) / GameMap.TILE_SIZE) + 1, w);
        int endY = (int)Math.min(Math.floor(((camY - map.getOriginY()) * ppu + SCREEN_HEIGHT) / GameMap.TILE_SIZE) + 1, h);

        // Draw the tiles
        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                ctx.drawImage(
                    map.getTilesetImage(),
                    map.getTileset()[renderMap[y][x]].x, map.getTileset()[renderMap[y][x]].y, GameMap.TILE_SIZE, GameMap.TILE_SIZE,
                    x * GameMap.TILE_SIZE, y * GameMap.TILE_SIZE, GameMap.TILE_SIZE, GameMap.TILE_SIZE
                );
            }
        }

        // Reset the map translation
        ctx.translate(-map.getOriginX() * ppu, map.getOriginY() * ppu);
    }

    public static void main(String[] args) {
        launch();
    }
}
