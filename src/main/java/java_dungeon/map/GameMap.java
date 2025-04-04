package java_dungeon.map;

import java.util.Random;

public class GameMap {
    private String[][] renderMap;

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

    public String[][] getRenderMap() {
        return renderMap;
    }

    private void generateTilemap() {
        // ToDo: Add better dungeon generation
        // 16x14 tiles tilemap (screen size)
        this.width = 16;
        this.height = 14;

        renderMap = new String[height][width];
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

                renderMap[y][x] = tile;
            }
        }
        // Random walls
        for (int i = 0; i < 10; i++) {
            this.renderMap[rand.nextInt(0, height)][rand.nextInt(0, width)] = "Wall";
        }
    }
}
