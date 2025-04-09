package java_dungeon.map;

import javafx.geometry.Point2D;

public class GameMap {
    // Tilemaps are a 2D array of strings
    private final String[][] tiles;

    private final int width, height;

    public GameMap() {
        this.width = 64;
        this.height = 64;

        this.tiles = new String[this.height][this.width];

        // Fill with walls (tiles should be eventually set from DungeonData)
        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                this.tiles[y][x] = "Wall";
            }
        }
    }

    public int getWidth() {
        return width;
    }
    public int getHeight() {
        return height;
    }

    public void setTiles(String[][] newTiles) {
        // Make sure the number of new tiles is >= the size of the current map (can be bigger, because this will just copy a portion of it)
        if (newTiles.length * newTiles[0].length < height * width) {
            throw new RuntimeException(String.format(
                "Failed to set GameMap tiles, cannot set tiles to a smaller array. current size = [%d, %d], new size = [%d, %d]",
                width, height, newTiles[0].length, newTiles.length
            ));
        }

        // Deep copy tiles
        for (int y = 0; y < height; y++) {
            if (width >= 0) {
                System.arraycopy(newTiles[y], 0, tiles[y], 0, width);
            }
        }
    }

    public String getTile(int x, int y) {
        return tiles[y][x];
    }

    public boolean checkCollisionAt(int x, int y) {
        // No collision outside the tiles
        if (x < 0 || y < 0 || x >= width || y >= height) { return false; }

        String tile = tiles[y][x];

        return tile.equalsIgnoreCase("Wall")
            || tile.equalsIgnoreCase("Door")
            || tile.equalsIgnoreCase("Boss-Door");
    }

    public boolean linecast(Point2D a, Point2D b) {
        // Bresenham’s line algorithm
        // First convert double point coordinates to tile coordinates (floor to int)
        int x0 = (int)a.getX();
        int x1 = (int)b.getX();
        int y0 = (int)a.getY();
        int y1 = (int)b.getY();

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        dy = -dy;

        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;

        int err = dx + dy;

        // Loops until the line reaches the end point
        while (true) {
            if (checkCollisionAt(x0, y0)) {
                return true; // Line is blocked
            }

            if (x0 == x1 && y0 == y1) {
                break; // Reached the end point
            }

            int e2 = err << 1; // << 1 is equal to *2
            if (e2 >= dy) {
                if (x0 == x1) { break; }
                err += dy;
                x0 += sx;
            }
            if (e2 <= dx) {
                if (y0 == y1) { break; }
                err += dx;
                y0 += sy;
            }
        }

        // Didn't hit anything
        return false;
    }

    // Checks if 2 points are in the same tile
    public boolean inSameTile(Point2D a, Point2D b) {
        return (int)a.getX() == (int)b.getX() && (int)a.getY() == (int)b.getY();
    }
}
