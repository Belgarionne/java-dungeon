package java_dungeon.map;

import java_dungeon.main.AssetManager;
import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.Random;

// "Binary Space Partition" Dungeon Generator
// Useful for making artificial room and corridor like dungeons
public class DungeonGeneratorBSP implements DungeonGenerator {
    public static class BSPNode {
        public int x, y, w, h;

        // Could be split top/bottom as well
        public BSPNode left;
        public BSPNode right;

        public Room room;

        public BSPNode(int x, int y, int w, int h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.left = null;
            this.right = null;
            this.room = null;
        }
    }

    public static class Room {
        public int x, y, w, h;

        public Room(int x, int y, int w, int h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        public int getCenterX() {
            return x + w / 2;
        }

        public int getCenterY() {
            return y + h / 2;
        }
    }

    private final int minRoomSize;
    private final int corridorSize;
    private final Random rand;

    private final ArrayList<Room> rooms;
    private final ArrayList<Room> corridors; // corridors are just long, thin "rooms"

    public DungeonGeneratorBSP(int minSize, int corridorSize) {
        this.minRoomSize = minSize;
        this.corridorSize = corridorSize;
        rand = new Random();
        this.rooms = new ArrayList<>();
        this.corridors = new ArrayList<>();
    }

    @Override
    public void setSeed(long seed) {
        rand.setSeed(seed);
    }

    @Override
    public DungeonData generate(int width, int height, int level) {
        DungeonData data = new DungeonData(width, height);
        String[][] map = data.getTiles();

        // Fill with walls
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                map[y][x] = "Wall";
            }
        }

        // Create BSP root node
        BSPNode root = new BSPNode(0, 0, width, height);
        splitNode(root);

        generateRooms(root); // Generate rooms
        generateCorridors(root); // Generate corridors

        // Draw rooms
        for (Room room : rooms) {
            drawRoom(room, map);
        }
        // Draw corridors
        for (Room corridor : corridors) {
            drawCorridor(corridor, map);
        }

        // Pick a random player start
        int startRoomIndex = rand.nextInt(rooms.size());
        data.setPlayerStart(randomEmptyPosInRoom(rooms.get(startRoomIndex), map, false));

        // Pick a random exit point
        int endRoomIndex = -1;
        while (endRoomIndex < 0) {
            int randIndex = rand.nextInt(rooms.size());
            // Don't place the end right at the start
            if (randIndex != startRoomIndex) {
                endRoomIndex = randIndex;
            }
        }
        data.setExitPoint(randomEmptyPosInRoom(rooms.get(endRoomIndex), map, false));

        // Add 1 enemy to each room
        String[] enemyTypes = { "Slime", "Skeleton", "Cultist" };
        double[] enemySpawnWeights = { 0.6, 0.3, 0.1 }; // Spawn weights for each enemy type

        for (int i = 0; i < rooms.size(); i++) {
            // Don't add an enemy to the starting room
            if (i == startRoomIndex) { continue; }

            String type = weightedRandom(enemyTypes, enemySpawnWeights);
            Point2D spawnPoint = randomEmptyPosInRoom(rooms.get(i), map, false);

            int hp = rand.nextInt(2 + level * 3, 10 + level * 3);
            int dmg = rand.nextInt(1 + level * 2, 3 + level * 2);
            int def = rand.nextInt(0, 1 + level * 2);

            // The name is also the name of the sprite image
            data.getEnemySpawns().add(new EnemySpawnData(type, spawnPoint, type, hp, dmg, def, (level + 1) * 3));

        }

        // Add items to random rooms
        String[] itemIds = AssetManager.getItemFactory().getItemIds();

        for (int i = 0; i < rand.nextInt((int)(rooms.size() * 0.2), (int)(rooms.size() * 0.6)); i++) {
            int roomIndex = rand.nextInt(rooms.size());
            Point2D spawnPoint = randomEmptyPosInRoom(rooms.get(roomIndex), map, false);
            String id = itemIds[rand.nextInt(itemIds.length)];
            int itemLvl = rand.nextInt(level, level + 3);

            data.getItemSpawns().add(new ItemSpawnData(spawnPoint, id, itemLvl));
        }

        return data;
    }

    private void splitNode(BSPNode node) {
        if (node.w < minRoomSize * 2 && node.h < minRoomSize * 2) {
            // Stop splitting if the area is too small
            return;
        }

        boolean splitVertically = rand.nextBoolean();

        // Change split direction if the node is too small in the current axis
        if (node.w < minRoomSize) { splitVertically = false; }
        if (node.h < minRoomSize) { splitVertically = true; }

        int max = (splitVertically ? node.w : node.h) - minRoomSize;
        if (max <= minRoomSize) {
            return; // Exit if the node is smaller than the minimum size
        }

        // Pick a random split position
        int split = minRoomSize + rand.nextInt(max - minRoomSize);

        if (splitVertically) {
            node.left = new BSPNode(node.x, node.y, split, node.h);
            node.right = new BSPNode(node.x + split, node.y, node.w - split, node.h);
        } else {
            node.left = new BSPNode(node.x, node.y, node.w, split);
            node.right = new BSPNode(node.x, node.y + split, node.w, node.h - split);
        }

        // Recursively split sub nodes
        splitNode(node.left);
        splitNode(node.right);
    }

    private void generateRooms(BSPNode node) {
        // If this is not a leaf node, generate rooms in children (until a leaf node is reached)
        if (node.left != null || node.right != null) {
            if (node.left != null) { generateRooms(node.left); }
            if (node.right != null) { generateRooms(node.right); }

        }
        else {
            // Leaf node, create a random sized room if the space is large enough
            if (node.w < minRoomSize || node.h < minRoomSize) {
                return; // Too small to make a valid room
            }

            // Make a room with a random size inside the node space
            int roomWidth = minRoomSize - 2 + rand.nextInt(0, node.w - minRoomSize + 1);
            int roomHeight = minRoomSize - 2  + rand.nextInt(0, node.h - minRoomSize + 1);
            int roomX = node.x + 1 + rand.nextInt(node.w - roomWidth - 1);
            int roomY = node.y + 1 + rand.nextInt(node.h - roomHeight - 1);

            // Add the room
            node.room = new Room(roomX, roomY, roomWidth, roomHeight);
            rooms.add(node.room);
        }
    }

    private void generateCorridors(BSPNode node) {
        // Only continue until the parent of leaf nodes
        if (node.left == null || node.right == null) { return; }

        // Get the first room in the left and right nodes (go through the tree until a room is found)
        Room r1 = findRoom(node.left);
        Room r2 = findRoom(node.right);

        // Make sure the rooms exist (a leaf node might not have a room if it is too small)
        if (r1 == null || r2 == null) { return; }

        // Create a corridor between r1 and r2 (from the center)
        int x1 = r1.getCenterX(), y1 = r1.getCenterY();
        int x2 = r2.getCenterX(), y2 = r2.getCenterY();

        // Start with vertical, or horizontal
        if (rand.nextBoolean()) {
            // First horizontal, then vertical
            corridors.add(new Room(Math.min(x1, x2), y1, Math.abs(x1 - x2) + corridorSize, corridorSize));
            corridors.add(new Room(x2, Math.min(y1, y2), corridorSize, Math.abs(y1 - y2) + corridorSize));
        } else {
            // First vertical, then horizontal
            corridors.add(new Room(x1, Math.min(y1, y2), corridorSize, Math.abs(y1 - y2) + corridorSize));
            corridors.add(new Room(Math.min(x1, x2), y2, Math.abs(x1 - x2) + corridorSize, corridorSize));
        }

        // Recursively connect more rooms
        generateCorridors(node.left);
        generateCorridors(node.right);
    }

    private Room findRoom(BSPNode node) {
        if (node.room != null) {
            return node.room;
        }
        if (node.left != null) {
            return findRoom(node.left);
        }
        if (node.right != null) {
            return findRoom(node.right);
        }
        // No room in this part of the tree
        return null;
    }

    private void drawRoom(Room room, String[][] map) {
        // Set all the tiles in the room to ground
        for (int y = room.y; y < (room.y + room.h); y++) {
            for (int x = room.x; x < (room.x + room.w); x++) {
                map[y][x] = "Ground";
            }
        }

        // Add random walls
        int roomArea = room.w * room.h;
        for (int i = 0; i < rand.nextInt((int)(roomArea * 0.05), (int)(roomArea * 0.2)); i++) {
            // Place the wall in a random spot (avoiding a 1 tile border so the room is traversable)
            int x = rand.nextInt(room.x + 1, room.x + room.w - 1);
            int y = rand.nextInt(room.y + 1, room.y + room.h - 1);
            map[y][x] = "Wall";
        }
    }

    private void drawCorridor(Room room, String[][] map) {
        // Set all the tiles in the corridor to ground
        for (int y = room.y; y < (room.y + room.h); y++) {
            for (int x = room.x; x < (room.x + room.w); x++) {
                map[y][x] = "Ground";
            }
        }
    }

    public Point2D randomEmptyPosInRoom(Room room, String[][] map, boolean avoidBorder) {
        int x = room.x + (avoidBorder ? rand.nextInt(1, room.w - 1) : rand.nextInt(0, room.w));
        int y = room.y + (avoidBorder ? rand.nextInt(1, room.h - 1) : rand.nextInt(0, room.h));

        while (map[y][x].equalsIgnoreCase("Wall")) {
            x = room.x + (avoidBorder ? rand.nextInt(1, room.w - 1) : rand.nextInt(0, room.w));
            y = room.y + (avoidBorder ? rand.nextInt(1, room.h - 1) : rand.nextInt(0, room.h));
        }

        return new Point2D(x, y);
    }

    // Generic weighted-random item picker function
    public <T> T weightedRandom(T[] items, double[] weights) {
        // Compute the total weight of all items together.
        // This can be skipped of course if sum is already 1.
        double totalWeight = 0.0;
        for (double weight : weights) {
            totalWeight += weight;
        }

        // Now choose a random item.
        int idx = 0;
        for (double r = Math.random() * totalWeight; idx < items.length - 1; ++idx) {
            r -= weights[idx];
            if (r <= 0.0) break;
        }

        // Return the item
        return items[idx];
    }
}
