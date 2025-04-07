package java_dungeon.map;

import java_dungeon.util.Vector2;

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
    public DungeonData generate(int width, int height) {
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
            drawRoom(corridor, map);
        }

        // Pick a random player start
        int startRoomIndex = rand.nextInt(rooms.size());
        data.setPlayerStart(new Vector2(rooms.get(startRoomIndex).getCenterX(), rooms.get(startRoomIndex).getCenterY()));

        // Add 1 enemy to each room
        for (int i = 0; i < rooms.size(); i++) {
            // Don't add an enemy to the starting room
            if (i != startRoomIndex) {
                data.getEnemyPoints().add(new Vector2(rooms.get(i).getCenterX(), rooms.get(i).getCenterY()));
            }
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

            // ToDo: Maybe load rooms from a file and place a random one here
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
        // ToDo: decorate rooms
        for (int y = room.y; y < (room.y + room.h); y++) {
            for (int x = room.x; x < (room.x + room.w); x++) {
                map[y][x] = "Ground";
            }
        }
    }
}
