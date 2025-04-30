package java_dungeon.controllers;

import java_dungeon.gui.ItemView;
import java_dungeon.items.Equipment;
import java_dungeon.items.Item;
import java_dungeon.main.App;
import java_dungeon.main.AssetManager;
import java_dungeon.main.Globals;

import java_dungeon.gui.AutoScalingCanvas;
import java_dungeon.map.DungeonGenerator;
import java_dungeon.map.DungeonGeneratorBSP;
import java_dungeon.map.GameMap;
import java_dungeon.objects.*;
import java_dungeon.objects.Character;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
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
import javafx.util.Duration;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GameController {
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
    private BorderPane gamePane;
    @FXML
    private BorderPane gameoverPane;
    @FXML
    private TextArea logText;
    @FXML
    private Label xpLbl;

    private boolean freezeGame; // Pauses the game when true (stops input and updates)

    // Game canvas
    private AutoScalingCanvas canvas;
    private GraphicsContext ctx;

    FadeTransition gameoverFade;

    // Game properties
    private final GameMap map;
    private final Player player;
    private final ArrayList<Enemy> enemies;
    private final ArrayList<ItemPickup> itemPickups;
    private Point2D exitPoint;
    private int enemyCount;
    private int currentFloor;
    private long currentDungeonSeed;

    private Point2D cameraPos;

    public GameController() {
        this.freezeGame = false;

        this.map = new GameMap();
        this.player = new Player(new Point2D(0, 0));
        this.cameraPos = new Point2D(0, 0);
        this.enemies = new ArrayList<>();
        this.itemPickups = new ArrayList<>();
        this.exitPoint = new Point2D(0, 0);
        this.currentFloor = 0;

        this.currentDungeonSeed = System.currentTimeMillis();
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

        // Continue from the save if one exists
        if (saveExists()) {
            loadSave();
        }
        else {
            generateLevel(0, System.currentTimeMillis());
            writeSave();
        }

        updateInventoryUI();
        renderGame();

        // Redraw the game if the canvas is resized (window resizing)
        canvas.scalingProperty().addListener((obs) -> renderGame());

        // Add the game canvas
        gamePane.setCenter(canvas);

        // Hide the game over screen
        gameoverPane.setVisible(false);

        // Make the game over fade transition (to use when the game ends)
        gameoverFade = new FadeTransition(Duration.seconds(2), gameoverPane);
        gameoverFade.setFromValue(0.0);
        gameoverFade.setToValue(1.0);

        // Init input once the scene has been set
        Platform.runLater(() -> gamePane.getScene().addEventFilter(KeyEvent.KEY_PRESSED, this::onKeyPressed));
    }

    @FXML
    void returnToMenu() {
        System.out.println("Return to menu...");
        App.loadScene("start");
    }

    private void onKeyPressed(KeyEvent event) {
        if (freezeGame) { return; } // Don't handle input or update when the game is paused

        // Handle input controls
        // Movement: Arrow keys and WASD
        // Pickup items: F
        switch (event.getCode()) {
            case KeyCode.LEFT, KeyCode.A -> movePlayer(new Point2D(-1, 0));
            case RIGHT, KeyCode.D -> movePlayer(new Point2D(1, 0));
            case UP, KeyCode.W -> movePlayer(new Point2D(0, -1));
            case DOWN, KeyCode.S -> movePlayer(new Point2D(0, 1));
            case F -> interact();
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

    private void endGame() {
        freezeGame = true;
        gameoverPane.setVisible(true);
        gameoverFade.playFromStart();

        // Delete the old save file (saves are deleted on death)
        File saveFile = new File("saveFile.txt");
        if (saveFile.exists() && saveFile.delete()) {
            System.out.println("Deleting save file...");
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

        // Cancel picking up items or moving if movement was stopped
        if (!stopMovement) {
            // Show possible item pickups
            for (ItemPickup pickup: itemPickups) {
                if (map.inSameTile(newPos, pickup.getPosition())) {
                    Globals.logger.logMessage(String.format("You see a %s here.", pickup.getItem().getName()));
                }
            }

            // Show exit point message
            if (map.inSameTile(newPos, exitPoint)) {
                Globals.logger.logMessage(String.format("You see stairs to level %d here.", currentFloor + 2));
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

    private void interact() {
        // Pickup first item at player's location
        for (ItemPickup pickup: itemPickups) {
            if (map.inSameTile(player.getPosition(), pickup.getPosition())) {
                // Try to pick up the item (-1 is the fail result)
                int itemIndex = player.addItem(pickup.getItem());
                if (itemIndex < 0) {
                    Globals.logger.logMessage(String.format("Cannot pick up %s. Inventory is full.", pickup.getItem().getName()));
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

        // Check for floor exit
        if (map.inSameTile(player.getPosition(), exitPoint)) {
            // Move to the next level
            moveToNextLevel();

            // Refresh the screen
            renderGame();
        }
    }

    private void updateGame() {
        // Remove dead enemies
        List<Enemy> deadEnemies = enemies.stream().filter(Character::isDead).toList();
        enemies.removeAll(deadEnemies);

        // Save the player's level if it is a new high score
        if (player.getLevel() > Globals.playerLvlHigh) {
            Globals.playerLvlHigh = player.getLevel();
            Globals.logger.logMessage("New highest player level reached!");
            AssetManager.saveRecords();
        }

        // Update each AI
        for (Enemy enemy : enemies) {
            enemy.updateAI(map, player);
        }

        // Game over
        if (player.isDead()) {
            Globals.logger.logMessage("GAME OVER...");
            endGame(); // End the game
        }
    }

    private void moveToNextLevel() {
        // Increase the level
        currentFloor++;
        Globals.logger.logMessage(String.format("Moving to level %d....", currentFloor + 1));

        // Check for a new high score
        if (currentFloor > Globals.floorHigh) {
            Globals.logger.logMessage("New highest dungeon level reached!");
            Globals.floorHigh = currentFloor;
            AssetManager.saveRecords();
        }

        // Generate the new dungeon
        generateLevel(currentFloor, System.currentTimeMillis());

        // Save the current game state
        writeSave();
    }

    private void generateLevel(int level, long seed) {
        // Clear data from previous floors
        enemies.clear();
        itemPickups.clear();

        // Generate the dungeon (set the seed specifically so it can be saved)
        DungeonGenerator generator = new DungeonGeneratorBSP(6, 1);
        generator.setSeed(seed);
        currentDungeonSeed = seed;

        DungeonGenerator.DungeonData data = generator.generate(map.getWidth(), map.getHeight(), level);

        // Set player position
        player.setPosition(data.getPlayerStart());
        centerCamera();

        // Add enemies
        for (DungeonGenerator.EnemySpawnData enemyData : data.getEnemySpawns()) {
            enemies.add(new ChaseEnemy(enemyData.name(), enemyData.point(), enemyData.sprite(), enemyData.hp(), enemyData.dmg(), enemyData.def(), enemyData.xp()));
        }
        enemyCount = enemies.size();

        // Add item pickups
        for (DungeonGenerator.ItemSpawnData itemData : data.getItemSpawns()) {
            itemPickups.add(
                new ItemPickup(AssetManager.getItemFactory().createItem(itemData.id(), itemData.level()), itemData.point())
            );
        }

        // Set the exit point
        exitPoint = data.getExitPoint();
        System.out.printf("Exit = [%.2f, %.2f]\n", exitPoint.getX(), exitPoint.getY()); // Show the exit location for debugging

        // Set the map tiles
        map.setTiles(data.getTiles());

        // Set the exit tile separately
        map.setTile((int)exitPoint.getX(), (int)exitPoint.getY(), "Stairs");
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
        // Updates each item view to show the corresponding item
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

    private void loadSave() {
        // Clear data from previous floors
        enemies.clear();
        itemPickups.clear();

        Scanner reader;

        try {
            reader = new Scanner(new File("saveFile.txt"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Read all the lines of the file
        while (reader.hasNextLine()) {
            String type = reader.nextLine(); // Data types are "DUNGEON_DATA" and "PLAYER_DATA"

            // Skip empty or commented out lines (// means commented out)
            if (type.equalsIgnoreCase("") || type.substring(0, 2).equalsIgnoreCase("//")) {
                continue;
            }

            // Data properties
            String[] properties = reader.nextLine().split("\\|"); // Properties are seperated by |

            switch (type) {
                case "DUNGEON_DATA":
                    // Dungeon generation data
                    for (String prop : properties) {
                        String[] values = prop.split(":"); // Property values are seperated by :
                        switch (values[0]) {
                            case "seed" -> currentDungeonSeed = Long.parseLong(values[1]);
                            case "level" -> currentFloor = Integer.parseInt(values[1]);
                        }
                    }
                    break;
                case "PLAYER_DATA":
                    // Stat data
                    for (String prop : properties) {
                        String[] values = prop.split(":"); // Property values are seperated by :
                        switch (values[0]) {
                            case "level" -> player.setLevel(Integer.parseInt(values[1]));
                            case "xp" -> player.setExperience(Integer.parseInt(values[1]));
                            case "xpToLevel" -> player.setExperienceToLevel(Integer.parseInt(values[1]));
                            case "hp" -> player.setHealth(Integer.parseInt(values[1]));
                        }
                    }

                    // No items
                    if (!reader.hasNextLine()) {
                        break;
                    }

                    // Inventory data
                    String itemData = reader.nextLine();
                    // Loop until a empty line is reached
                    while (!itemData.equalsIgnoreCase("")) {
                        System.out.printf("Line = %s\n", itemData);
                        String[] itemProps = itemData.split(":")[1].split("\\|"); // Properties are seperated by |
                        int itemIndex = player.addItem(AssetManager.getItemFactory().createItem(itemProps[0], Integer.parseInt(itemProps[1])));

                        // Is the item equipped?
                        if (Boolean.parseBoolean(itemProps[2])) {
                            player.equip((Equipment)player.getItem(itemIndex));
                        }

                        // Stop if there are no more lines
                        if (!reader.hasNextLine()) { break; }

                        itemData = reader.nextLine();
                    }
                    break;
            }
        }

        reader.close();

        // Finally, generate the dungeon
        generateLevel(currentFloor, currentDungeonSeed);
    }

    private void writeSave() {
        try {
            FileWriter writer = new FileWriter("saveFile.txt");
            // Dungeon data section
            writer.write("DUNGEON_DATA\n");
            writer.write(String.format("seed:%d|level:%d\n", currentDungeonSeed, currentFloor));

            // Player data section
            writer.write("PLAYER_DATA\n");
            writer.write(String.format(
                "level:%d|xp:%d|xpToLevel:%d|hp:%d\n",
                player.getLevel(), player.getExperience(), player.getExperienceToLevel(), player.getHealth()
            ));

            // Write all the items
            for (Item item : player.getInventory()) {
                if (item == null) { continue; }
                writer.write(String.format(
                    "item:%s|%d|%b\n",
                    item.getId(), item.getLevel(), item instanceof Equipment equipment && player.isEquipped(equipment)
                ));
            }

            writer.close();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean saveExists() {
        File saveFile = new File("saveFile.txt");
        return saveFile.exists();
    }
}

