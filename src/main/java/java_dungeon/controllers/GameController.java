package java_dungeon.controllers;

import java_dungeon.AssetManager;
import java_dungeon.Globals;

import java_dungeon.gui.AutoScalingCanvas;
import java_dungeon.map.DungeonGenerator;
import java_dungeon.map.DungeonGeneratorBSP;
import java_dungeon.map.GameMap;
import java_dungeon.objects.Character;
import java_dungeon.objects.Enemy;
import java_dungeon.objects.Player;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class GameController extends ControllerBase {
    // FXML controls
    @FXML
    private Label enemiesLbl;
    @FXML
    private Label hpLbl;
    @FXML
    private BorderPane root;
    @FXML
    private TextArea logText;

    // Game canvas
    private AutoScalingCanvas canvas;
    private GraphicsContext ctx;

    // Game properties
    private final GameMap map;
    private final Player player;
    private final ArrayList<Enemy> enemies;
    private int enemyCount;

    private Point2D cameraPos;

    public GameController() {
        this.map = new GameMap();
        this.player = new Player(new Point2D(0, 0));
        this.cameraPos = new Point2D(0, 0);
        this.enemies = new ArrayList<>();
    }

    @FXML
    void initialize() {
        assert enemiesLbl != null : "fx:id=\"enemiesLbl\" was not injected: check your FXML file 'game.fxml'.";
        assert hpLbl != null : "fx:id=\"hpLbl\" was not injected: check your FXML file 'game.fxml'.";
        assert logText != null : "fx:id=\"logText\" was not injected: check your FXML file 'game.fxml'.";
        assert root != null : "fx:id=\"root\" was not injected: check your FXML file 'game.fxml'.";

        // Set up the canvas
        canvas = new AutoScalingCanvas(Globals.SCREEN_WIDTH, Globals.SCREEN_HEIGHT);
        ctx = canvas.getGraphicsContext2D();
        ctx.setImageSmoothing(false);

        // Set up the game
//        logText.appendText("\nExtra line");
        logText.appendText("");
        Globals.logger.setLogPanel(logText);
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

        root.setCenter(canvas);
    }

    @Override
    public void initializeScene(Scene scene) {
        super.initializeScene(scene);
        scene.addEventFilter(KeyEvent.KEY_PRESSED, this::onKeyPressed);
//        scene.setOnKeyPressed(this::onKeyPressed);
    }

    private void onKeyPressed(KeyEvent event) {
        // Check if the key pressed was one of the arrow keys
        switch (event.getCode().getName()) {
            case "Left" -> movePlayer(new Point2D(-1, 0));
            case "Right" -> movePlayer(new Point2D(1, 0));
            case "Up" -> movePlayer(new Point2D(0, -1));
            case "Down" -> movePlayer(new Point2D(0, 1));
        }
    }

    private void movePlayer(Point2D move) {
        // Get the new position
        int newX = (int)(player.getPosition().getX() + move.getX());
        int newY = (int)(player.getPosition().getY() + move.getY());
        Point2D newPos = new Point2D(newX, newY);

        boolean stopMovement = false; // Flag to cancel movement

        // Check for combat
        for (Enemy enemy: enemies) {
            if (map.inSameTile(newPos, enemy.getPosition())) {
                stopMovement = true; // Stop moving if there is an enemy in the way
                player.attack(enemy);
            }
        }

        // Check for collision
        if (!stopMovement && !map.checkCollisionAt(newX, newY)) {
            player.move(move);
            centerCamera();
        }

        // Update and re-render the game
        updateGame();
        renderGame();
    }

    private void updateGame() {
        // Remove dead enemies
        List<Enemy> deadEnemies = enemies.stream().filter(Character::isDead).toList();
        enemies.removeAll(deadEnemies);

        // ToDo: Extract into method for different enemies
        for (Enemy enemy : enemies) {
            double distToPlayer = enemy.getPosition().distance(player.getPosition());

            // Update the enemy's target if the player is in view
            if (distToPlayer <= enemy.getSightDistance() && !map.linecast(enemy.getPosition(), player.getPosition())) {
                enemy.setTargetPoint(player.getPosition());
            }

            // Only move if the enemy has a target (or remove the target if it was reached)
            if (enemy.getTargetPoint() == null || map.inSameTile(enemy.getPosition(), enemy.getTargetPoint())) {
                enemy.setTargetPoint(null);
                continue;
            }

            // Get the direction to move towards the target
            Point2D toTarget = enemy.getTargetPoint().subtract(enemy.getPosition());
            Point2D moveDirection = map.getDirectionOnGrid(toTarget);

            // Try to slide around walls
            if (map.checkCollisionAt((int)(enemy.getPosition().getX() + moveDirection.getX()), (int)(enemy.getPosition().getY() + moveDirection.getY()))) {
                double dx = Math.signum(toTarget.getX());
                double dy = Math.signum(toTarget.getY());

                // Move in the opposite axis
                if (moveDirection.getX() != 0) {
                    moveDirection = new Point2D(0, dy);
                }
                else if (moveDirection.getY() != 0) {
                    moveDirection = new Point2D(dx, 0);
                }
            }

            // Move
            moveEnemy(enemy, moveDirection);
        }
    }

    private void moveEnemy(Enemy enemy, Point2D move) {
        // Get the new position
        int newX = (int)(enemy.getPosition().getX() + move.getX());
        int newY = (int)(enemy.getPosition().getY() + move.getY());
        Point2D newPos = new Point2D(newX, newY);

        // Check for combat
        if (map.inSameTile(enemy.getPosition(), player.getPosition()) || map.inSameTile(newPos, player.getPosition())) {
            enemy.attack(player);
        }
        // Check for collision
        else if (!map.checkCollisionAt(newX, newY)) {
            enemy.move(move);
        }
    }

    private void generateLevel() {
        // Generate the dungeon
        DungeonGenerator generator = new DungeonGeneratorBSP(6, 1);
        DungeonGenerator.DungeonData data = generator.generate(map.getWidth(), map.getHeight());

        // Set player position
        player.setPosition(data.getPlayerStart());
        centerCamera();

        // Add enemies
        for (Point2D spawnPoint: data.getEnemyPoints()) {
            enemies.add(new Enemy(spawnPoint));
        }
        enemyCount = enemies.size();

        // Set the map tiles
        map.setTiles(data.getTiles());
    }

    private void centerCamera() {
        // Extra blank space for if the map is smaller than the screen (center the smaller map in the middle of the screen)
        double extraSpacingX = Math.max(Globals.SCREEN_TILE_WIDTH - map.getWidth(), 0.0) * 0.5;
        double extraSpacingY = Math.max(Globals.SCREEN_TILE_HEIGHT - map.getHeight(), 0.0) * 0.5;

        // Center the camera on the player, bounded by the map
        double camX = Math.clamp(
                player.getPosition().getX() - Globals.SCREEN_TILE_WIDTH * 0.5 + 0.5,
                -extraSpacingX, map.getWidth() - Globals.SCREEN_TILE_WIDTH + extraSpacingX
        );
        double camY = Math.clamp(
                player.getPosition().getY() - Globals.SCREEN_TILE_HEIGHT * 0.5 + 0.5,
                -extraSpacingY, map.getHeight() - Globals.SCREEN_TILE_HEIGHT + extraSpacingY
        );

        cameraPos = new Point2D(camX, camY);
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
        renderUI(); // Render/update the UI

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
        int endX = (int)Math.min(Math.ceil(cameraPos.getX() + Globals.SCREEN_TILE_WIDTH), map.getWidth());
        int endY = (int)Math.min(Math.ceil(cameraPos.getY() + Globals.SCREEN_TILE_HEIGHT), map.getHeight());
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
        Rectangle2D frame = imageSheet.getFrame(player.getTileName());

        ctx.drawImage(imageSheet.getImg(),
                frame.getMinX(), frame.getMinY(), frame.getWidth(), frame.getHeight(),
                player.getPosition().getX() * AssetManager.TILE_SIZE, player.getPosition().getY() * AssetManager.TILE_SIZE, frame.getWidth(), frame.getHeight()
        );
    }

    private void renderEnemies() {
        // Enemies are frame of the tileset image for now
        AssetManager.AtlasImage imageSheet = AssetManager.getImages().get("Tileset");

        for (Enemy enemy : enemies) {
            Rectangle2D frame = imageSheet.getFrame(enemy.getTileName());
            ctx.drawImage(imageSheet.getImg(),
                    frame.getMinX(), frame.getMinY(), frame.getWidth(), frame.getHeight(),
                    enemy.getPosition().getX() * AssetManager.TILE_SIZE, enemy.getPosition().getY() * AssetManager.TILE_SIZE, frame.getWidth(), frame.getHeight()
            );
        }
    }

    private void renderUI() {
        updateHpLbl(player.getHealth(), player.getMaxHealth());
        enemiesLbl.setText(String.format("Enemies: %d/%d", enemies.size(), enemyCount));
    }

    private void updateHpLbl(int current, int max) {
        hpLbl.setText(String.format("Hp: %d/%d", current, max));
    }
}
