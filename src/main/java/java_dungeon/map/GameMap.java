package java_dungeon.map;

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
}
