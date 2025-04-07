package java_dungeon.map;

public class GameMap {
    // Tilemaps are a 2D array of strings
    private String[][] tiles;

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

    public String[][] getTiles() {
        return tiles;
    }

    public void setTiles(String[][] newTilesRef) {
        // Just update the reference to avoid extra memory copying
        tiles = newTilesRef;
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
