package java_dungeon.map;

import javafx.scene.image.Image;

import java.util.Random;

public class GameMap {
    public static int TILE_SIZE = 16;

    private double originX;
    private double originY;

    public static class Tile {
        public final int x, y;

        public Tile(int xIndex, int yIndex) {
            this.x = xIndex * TILE_SIZE;
            this.y = yIndex * TILE_SIZE;
        }
    }

    private Tile[] tileset;
    private Image tilesetImage;

    private int[][] renderMap;

    public GameMap() {
        loadTileset();
        generateTilemap();

        this.originX = 0.0;
        this.originY = 7.0;
    }

    public Image getTilesetImage() {
        return tilesetImage;
    }

    public Tile[] getTileset() {
        return tileset;
    }

    public int[][] getRenderMap() {
        return renderMap;
    }

    public double getOriginX() {
        return originX;
    }
    public double getOriginY() {
        return originY;
    }

    public void setOriginX(double originX) {
        this.originX = originX;
    }
    public void setOriginY(double originY) {
        this.originY = originY;
    }

    private void loadTileset() {
        // The tileset is 5x3 tiles
        tilesetImage = new Image("dungeon-tiles.png");

        int tilesPerRow = 5;
        int tilesPerCol = 3;
        tileset = new Tile[tilesPerRow * tilesPerCol];

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 5; x++) {
                tileset[y * tilesPerRow + x] = new Tile(x, y);
            }
        }
    }

    private void generateTilemap() {
        // ToDo: Add better dungeon generation
        // 16x14 tiles tilemap (screen size)
        int w = 16;
        int h = 14;

        renderMap = new int[h][w];

        // Outer wall
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int tile = 6;

                if (x == 0 && y == 0) {
                    tile = 0;
                }
                else if (x == (w - 1) && y == 0) {
                    tile = 2;
                }
                else if (x == 0 && y == (h - 1)) {
                    tile = 10;
                }
                else if (x == (w - 1) && y == (h - 1)) {
                    tile = 12;
                }
                else if (x == 0) {
                    tile = 5;
                }
                else if (x == (w - 1)) {
                    tile = 7;
                }
                else if (y == 0) {
                    tile = 1;
                }
                else if (y == (h - 1)) {
                    tile = 11;
                }
                renderMap[y][x] = tile;
            }
        }
        // Random blocks
        Random rand = new Random();
        for (int i = 0; i < 10; i++) {
            this.renderMap[rand.nextInt(0, h)][rand.nextInt(0, w)] = 14;
        }
    }
}
