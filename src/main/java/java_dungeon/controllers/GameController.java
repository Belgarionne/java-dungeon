package java_dungeon.controllers;

import java_dungeon.gui.ItemView;
import java_dungeon.items.Equipment;
import java_dungeon.items.Item;
import java_dungeon.main.AssetManager;
import java_dungeon.main.Globals;

import java_dungeon.gui.AutoScalingCanvas;
import java_dungeon.map.DungeonGenerator;
import java_dungeon.map.DungeonGeneratorBSP;
import java_dungeon.map.GameMap;
import java_dungeon.objects.*;
import java_dungeon.objects.Character;

import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class GameController extends ControllerBase {
    // FXML controls
    @FXML
    private Label atkLbl;
    @FXML
    private Label defLbl;
    @FXML
    private Label enemiesLbl;
    @FXML
    private Label hpLbl;
    @FXML
    private GridPane inventoryGrid;
    @FXML
    private Label nameLbl;
    @FXML
    private BorderPane root;
    @FXML
    private TextArea logText;
    @FXML
    private Label xpLbl;

    // Game canvas
    private AutoScalingCanvas canvas;
    private GraphicsContext ctx;

    // Game properties
    private final GameMap map;
    private final Player player;
    private final ArrayList<Enemy> enemies;
    private final ArrayList<ItemPickup> itemPickups;
    private int enemyCount;

    private Point2D cameraPos;

    public GameController() {
        this.map = new GameMap();
        this.player = new Player(new Point2D(0, 0));
        this.cameraPos = new Point2D(0, 0);
        this.enemies = new ArrayList<>();
        this.itemPickups = new ArrayList<>();
    }

    @FXML
    void initialize() {
        // Set up the canvas
        canvas = new AutoScalingCanvas(Globals.SCREEN_WIDTH, Globals.SCREEN_HEIGHT);
        ctx = canvas.getGraphicsContext2D();
        ctx.setImageSmoothing(false);

        for (int y = 0; y < inventoryGrid.getRowCount(); y++) {
            for (int x = 0; x < inventoryGrid.getColumnCount(); x++) {
                ItemView view = new ItemView();

                int finalIndex = y * inventoryGrid.getColumnCount() + x;
                view.setOnMouseClicked(e -> onItemViewClicked(finalIndex, e)); // Event to use/equip item in the view
                inventoryGrid.add(view, x, y);
            }
        }

        // Set up the game
        Globals.logger.setLogPanel(logText);
        generateLevel(0);
        renderGame();

        // Redraw the game if the canvas is resized (window resizing)
        canvas.scalingProperty().addListener((obs) -> renderGame());

        root.setCenter(canvas);
    }

    @Override
    public void initializeScene(Scene scene) {
        super.initializeScene(scene);
        scene.addEventFilter(KeyEvent.KEY_PRESSED, this::onKeyPressed);
    }

    private void onKeyPressed(KeyEvent event) {
        // Handle input controls
        // Movement: Arrow keys and WASD
        // Pickup items: F
        switch (event.getCode()) {
            case KeyCode.LEFT, KeyCode.A -> movePlayer(new Point2D(-1, 0));
            case RIGHT, KeyCode.D -> movePlayer(new Point2D(1, 0));
            case UP, KeyCode.W -> movePlayer(new Point2D(0, -1));
            case DOWN, KeyCode.S -> movePlayer(new Point2D(0, 1));
            case F -> pickupItem();
        }
    }

    private void onItemViewClicked(int index, MouseEvent e) {
        Item itemInSlot = player.getItem(index);
        if (itemInSlot == null) { return; } // Can't do anything if there is no item in the clicked slot

        // Right-click drops items
        if (e.getButton() == MouseButton.SECONDARY) {
            Globals.logger.logMessage(String.format("Dropping item %s.", player.getItem(index).getName()));

            // Unequip the item if it is equipped
            if (itemInSlot instanceof Equipment equipment && player.isEquipped(equipment)) {
                player.unequip(equipment.getSlot());
            }

            ItemPickup droppedPickup = new ItemPickup(player.removeItem(index), player.getPosition());
            itemPickups.add(droppedPickup);

            // Refresh the game to show the pickup
            updateInventoryUI();
            renderGame();
            return;
        }

        // Equip equipment
        if (itemInSlot instanceof Equipment equipment) {
            boolean isEquipped = player.isEquipped(equipment);

            // Equip or un-equip the item depending on whether it is already equipped
            if (!isEquipped) {
                player.equip(equipment);
                Globals.logger.logMessage(String.format("Equipping %s, %s.", equipment.getSlot(), equipment.getName()));
            }
            else {
                player.unequip(equipment.getSlot());
                Globals.logger.logMessage(String.format("Unequipping %s, %s.", equipment.getSlot(), equipment.getName()));
            }
        }

        // Refresh the game / update UI
        updateInventoryUI();
        renderGame();
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

        // Cancel picking up items or moving if movement was stopped
        if (!stopMovement) {
            // Show for possible item pickups
            for (ItemPickup pickup: itemPickups) {
                if (map.inSameTile(newPos, pickup.getPosition())) {
                    Globals.logger.logMessage(String.format("You see here a %s.", pickup.getItem().getName()));
                }
            }

            // Check for collision
            if (!map.checkCollisionAt(newX, newY)) {
                player.move(move);
                centerCamera();
            }
        }

        // Update and re-render the game
        updateGame();
        renderGame();
    }

    private void pickupItem() {
        // Pickup first item at player's location
        for (ItemPickup pickup: itemPickups) {
            if (map.inSameTile(player.getPosition(), pickup.getPosition())) {
                // Try to pick up the item (-1 is the fail result)
                int itemIndex = player.addItem(pickup.getItem());
                if (itemIndex < 0) {
                    Globals.logger.logMessage(String.format("Cannot pick up %s, inventory is full.", pickup.getItem().getName()));
                    return; // Can't pick up any more items if inventory is full
                }

                Globals.logger.logMessage(String.format("Picking up %s.", pickup.getItem().getName()));
                itemPickups.remove(pickup);

                // Refresh the game
                updateInventoryUI();
                updateGame();
                renderGame();
                return; // Stop after picking up the first item
            }
        }
    }

    private void updateGame() {
        // Remove dead enemies
        List<Enemy> deadEnemies = enemies.stream().filter(Character::isDead).toList();
        enemies.removeAll(deadEnemies);

        // Update each AI
        for (Enemy enemy : enemies) {
            enemy.updateAI(map, player);
        }
    }

    private void generateLevel(int level) {
        // Generate the dungeon
        DungeonGenerator generator = new DungeonGeneratorBSP(6, 1);
        DungeonGenerator.DungeonData data = generator.generate(map.getWidth(), map.getHeight());

        // Set player position
        player.setPosition(data.getPlayerStart());
        centerCamera();

        // Add enemies
        for (Point2D spawnPoint: data.getEnemyPoints()) {
            enemies.add(new ChaseEnemy(spawnPoint));
        }
        enemyCount = enemies.size();

        // Add item pickups
        for (Point2D pickupPoint : data.getItemPoints()) {
            itemPickups.add(new ItemPickup(AssetManager.getItemFactory().createRandomItem(level + (int)(Math.random() * 2)), pickupPoint));
        }

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
        ctx.translate(-cameraPos.getX() * Globals.TILE_SIZE, -cameraPos.getY() * Globals.TILE_SIZE);

        renderTiles(); // Draw the tilemap
        renderPickups(); // Draw the item pickups
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
                    tileset.getFrame(tile).getMinX(), tileset.getFrame(tile).getMinY(), Globals.TILE_SIZE, Globals.TILE_SIZE,
                    x * Globals.TILE_SIZE, y * Globals.TILE_SIZE, Globals.TILE_SIZE, Globals.TILE_SIZE
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
            player.getPosition().getX() * Globals.TILE_SIZE, player.getPosition().getY() * Globals.TILE_SIZE, frame.getWidth(), frame.getHeight()
        );
    }

    private void renderEnemies() {
        // Enemies are frame of the tileset image for now
        AssetManager.AtlasImage imageSheet = AssetManager.getImages().get("Tileset");

        for (Enemy enemy : enemies) {
            Rectangle2D frame = imageSheet.getFrame(enemy.getTileName());
            ctx.drawImage(imageSheet.getImg(),
                frame.getMinX(), frame.getMinY(), frame.getWidth(), frame.getHeight(),
                enemy.getPosition().getX() * Globals.TILE_SIZE, enemy.getPosition().getY() * Globals.TILE_SIZE, frame.getWidth(), frame.getHeight()
            );
        }
    }

    private void renderPickups() {
        // Pickups are frame of the tileset image
        AssetManager.AtlasImage imageSheet = AssetManager.getImages().get("Tileset");

        for (ItemPickup pickup : itemPickups) {
            Rectangle2D frame = imageSheet.getFrame(pickup.getTileName());
            ctx.drawImage(imageSheet.getImg(),
                frame.getMinX(), frame.getMinY(), frame.getWidth(), frame.getHeight(),
                pickup.getPosition().getX() * Globals.TILE_SIZE, pickup.getPosition().getY() * Globals.TILE_SIZE, frame.getWidth(), frame.getHeight()
            );
        }
    }

    private void renderUI() {
        nameLbl.setText(String.format("%s Level %d", player.getName(), player.getLevel()));
        xpLbl.setText(String.format("EXP: %d/%d", player.getExperience(), player.getExperienceToLevel()));
        hpLbl.setText(String.format("Hp: %d/%d", player.getHealth(), player.getMaxHealth()));
        enemiesLbl.setText(String.format("Enemies: %d/%d", enemies.size(), enemyCount));
        atkLbl.setText(String.format("Attack: %d", player.getDamage()));
        defLbl.setText(String.format("Defense: %d", player.getDefense()));
    }

    private void updateInventoryUI() {
        for (int i = 0; i < inventoryGrid.getChildren().size(); i++) {
            ItemView view = (ItemView)inventoryGrid.getChildren().get(i);
            Item itemInSlot = player.getItem(i);

            view.setHeldItem(itemInSlot);

            // Check if the item is equipped
            if (itemInSlot instanceof Equipment equipment && player.isEquipped(equipment)) {
                view.setEquipState(true);
            }
        }
    }
}
