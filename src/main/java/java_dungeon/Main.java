package java_dungeon;

import java_dungeon.map.DungeonGenerator;
import java_dungeon.map.DungeonGeneratorBSP;
import java_dungeon.map.GameMap;
import java_dungeon.objects.Enemy;
import java_dungeon.objects.Player;

import java_dungeon.util.Vector2;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java_dungeon.gui.AutoScalingCanvas;

import java.util.ArrayList;

public class Main extends Application {
    // SNES rendering size
    private static final int SCREEN_WIDTH = 256;
    private static final int SCREEN_HEIGHT = 224;
    private static final double SCREEN_TILE_WIDTH = SCREEN_WIDTH / (double)AssetManager.TILE_SIZE;
    private static final double SCREEN_TILE_HEIGHT = SCREEN_HEIGHT / (double)AssetManager.TILE_SIZE;

    private GameMap map;
    private Player player;
    private ArrayList<Enemy> enemies;

    private AutoScalingCanvas canvas;
    private GraphicsContext ctx;

    private Vector2 cameraPos;

    public Main() {
        this.map = new GameMap();
        this.player = new Player(0, 0);
        this.cameraPos = new Vector2(0, 0);
        this.enemies = new ArrayList<>();
    }

    @Override
    public void start(Stage stage) {
        StackPane root = new StackPane();
        canvas = new AutoScalingCanvas(SCREEN_WIDTH, SCREEN_HEIGHT);
        root.getChildren().add(canvas);

        ctx = canvas.getGraphicsContext2D();
        ctx.setImageSmoothing(false);

        AssetManager.initialize();
        generateLevel();
        renderGame();

        // Redraw the game if the canvas is resized (window resizing)
        canvas.scalingProperty().addListener((obs) -> renderGame());

        // Debug update loop (Moves the camera 1 unit to the left every second)
//        AnimationTimer animator = new AnimationTimer() {
//            @Override
//            public void handle(long currentTime) {
//                camX -= 0.5/60.0;
//                renderGame();
//            }
//        };
//        animator.start();

        // Start the window at 3x size
        Scene scene = new Scene(root, SCREEN_WIDTH * 3, SCREEN_HEIGHT * 3);
        scene.setFill(Color.BLACK); // Black background color
        scene.setOnKeyPressed(this::onKeyPressed);

        stage.setScene(scene);
        stage.setTitle("Java Dungeon");
        stage.show();
    }

    private void onKeyPressed(KeyEvent event) {
        switch (event.getCode().getName()) {
            case "Left" -> movePlayer(new Vector2(-1, 0));
            case "Right" -> movePlayer(new Vector2(1, 0));
            case "Up" -> movePlayer(new Vector2(0, -1));
            case "Down" -> movePlayer(new Vector2(0, 1));
        }
    }

    private void movePlayer(Vector2 move) {
        // Check for collision
        // ToDo: Add more complicated collision casting to catch movement > 1
        if (!map.checkCollisionAt((int)(player.getPosition().getX() + move.getX()), (int)(player.getPosition().getY() + move.getY()))) {
            player.move(move);
            centerCamera();
            renderGame();
        }
    }

    private void generateLevel() {
        DungeonGenerator generator = new DungeonGeneratorBSP(6, 1);
        DungeonGenerator.DungeonData data = generator.generate(map.getWidth(), map.getHeight());

        player.setPosition(data.getPlayerStart());
        centerCamera();

        // Add enemies
        for (Vector2 spawn : data.getEnemyPoints()) {
            enemies.add(new Enemy(spawn.getX(), spawn.getY()));
        }

        map.setTiles(data.getTiles());
    }

    private void centerCamera() {
        // Extra blank space for if the map is smaller than the screen (center the smaller map in the middle of the screen)
        double extraSpacingX = Math.max(SCREEN_TILE_WIDTH - map.getWidth(), 0.0) * 0.5;
        double extraSpacingY = Math.max(SCREEN_TILE_HEIGHT - map.getHeight(), 0.0) * 0.5;

        // Center the camera on the player, bounded by the map
        cameraPos.setX(Math.clamp(
            player.getPosition().getX() - SCREEN_TILE_WIDTH * 0.5 + 0.5,
            -extraSpacingX, map.getWidth() - SCREEN_TILE_WIDTH + extraSpacingX
        ));
        cameraPos.setY(Math.clamp(
            player.getPosition().getY() - SCREEN_TILE_HEIGHT * 0.5 + 0.5,
            -extraSpacingY, map.getHeight() - SCREEN_TILE_HEIGHT + extraSpacingY
        ));
    }

    private void renderGame() {
        // Clear the screen
        ctx.setFill(Color.BLACK);
        ctx.fillRect(0, 0, canvas.getCanvas().getWidth(), canvas.getCanvas().getHeight());

        // Apply camera transformations (including render scaling)
        double renderScale = canvas.scalingProperty().get();
        ctx.save();
        ctx.scale(renderScale, renderScale);
        ctx.translate(-cameraPos.getX() * AssetManager.TILE_SIZE, -cameraPos.getY() * AssetManager.TILE_SIZE);

        renderTiles(); // Draw the tilemap
        renderEnemies(); // Draw the enemies
        renderPlayer(); // Draw the player

        ctx.restore();

        // Debug center guidelines
//        ctx.setStroke(Color.RED);
//        ctx.strokeLine(canvas.getCanvas().getWidth() * 0.5, 0, canvas.getCanvas().getWidth() * 0.5, canvas.getCanvas().getHeight());
//        ctx.strokeLine(0, canvas.getCanvas().getHeight() * 0.5, canvas.getCanvas().getWidth(), canvas.getCanvas().getHeight() * 0.5);
    }

    private void renderTiles() {
        // Only draw the visible tiles of the tilemap
        // Upper left corner of the screen in tile coordinates (bounded by the tilemap)
        int startX = (int)Math.max(Math.floor(cameraPos.getX()), 0);
        int startY = (int)Math.max(Math.floor(cameraPos.getY()), 0);
//        int startX = 0;
//        int startY = 0;

        // Bottom right corner of the screen in tile coordinates (bounded by the tilemap)
        int endX = (int)Math.min(Math.ceil(cameraPos.getX() + SCREEN_TILE_WIDTH), map.getWidth());
        int endY = (int)Math.min(Math.ceil(cameraPos.getY() + SCREEN_TILE_HEIGHT), map.getHeight());
//        int endX = map.getWidth();
//        int endY = map.getHeight();

        AssetManager.AtlasImage tileset = AssetManager.getImages().get("Tileset");

        // Draw the tiles
        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                String tile = map.getTile(x, y);
                ctx.drawImage(
                    tileset.getImg(),
                    tileset.getFrame(tile).getMinX(), tileset.getFrame(tile).getMinY(), AssetManager.TILE_SIZE, AssetManager.TILE_SIZE,
                    x * AssetManager.TILE_SIZE, y * AssetManager.TILE_SIZE, AssetManager.TILE_SIZE, AssetManager.TILE_SIZE
                );
            }
        }
    }

    private void renderPlayer() {
        // The player is a frame of the tileset image for now
        AssetManager.AtlasImage imageSheet = AssetManager.getImages().get("Tileset");
        var playerRect = imageSheet.getFrame("Player");

        ctx.drawImage(imageSheet.getImg(),
            playerRect.getMinX(), playerRect.getMinY(), playerRect.getWidth(), playerRect.getHeight(),
        player.getPosition().getX() * AssetManager.TILE_SIZE, player.getPosition().getY() * AssetManager.TILE_SIZE, playerRect.getWidth(), playerRect.getHeight()
        );
    }

    private void renderEnemies() {
        // Enemies are frame of the tileset image for now
        AssetManager.AtlasImage imageSheet = AssetManager.getImages().get("Tileset");

        var enemyRect = imageSheet.getFrame("Enemy");

        for (Enemy enemy : enemies) {
            ctx.drawImage(imageSheet.getImg(),
                enemyRect.getMinX(), enemyRect.getMinY(), enemyRect.getWidth(), enemyRect.getHeight(),
            enemy.getPosition().getX() * AssetManager.TILE_SIZE, enemy.getPosition().getY() * AssetManager.TILE_SIZE, enemyRect.getWidth(), enemyRect.getHeight()
            );
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
