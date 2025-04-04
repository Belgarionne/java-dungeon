package java_dungeon.map;

import java.util.Random;

public class GameMap {
    // Tilemaps are a 2D array of strings
    private String[][] map;

    private int width, height;

    public GameMap() {
        this.width = 0;
        this.height = 0;

        generateTilemap();
    }

    public int getWidth() {
        return width;
    }
    public int getHeight() {
        return height;
    }

    public String[][] getMap() {
        return map;
    }

    public String getTile(int x, int y) {
        return map[y][x];
    }

    public boolean checkCollisionAt(int x, int y) {
        // No collision outside the map
        if (x < 0 || y < 0 || x >= width || y >= height) { return false; }

        String tile = map[y][x];

        return tile.equalsIgnoreCase("Wall")
            || tile.equalsIgnoreCase("Door")
            || tile.equalsIgnoreCase("Boss-Door");
    }

    private void generateTilemap() {
        // ToDo: Add better dungeon generation
        // 16x14 tiles tilemap (screen size)
        this.width = 24;
        this.height = 24;

        map = new String[height][width];
        Random rand = new Random();

        // Outer wall
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                String tile = "Ground";

                // Doors
                if ((x == 0 && y == 7) || (x == (width - 1) && y == 7) || (y == 0 && x == 8) || (y == (height - 1) && x == 8)) {
                    // Random door or boss door
                    tile = rand.nextBoolean() ? "Door" : "Boss-Door";
                }
                // Outside walls
                else if (x == 0 || y == 0 || x == (width - 1) || y == (height - 1)) {
                    tile = "Wall";
                }

                map[y][x] = tile;
            }
        }
        // Random walls
        for (int i = 0; i < 10; i++) {
            this.map[rand.nextInt(0, height)][rand.nextInt(0, width)] = "Wall";
        }
    }
}
