package java_dungeon.map;

public class GameMap {
    // Tilemaps are a 2D array of strings
    private final String[][] map;

    private final int width, height;

    public GameMap() {
        this.width = 64;
        this.height = 64;

        this.map = new String[this.height][this.width];
        //  Using BSP for now for the most indoor dungeon-like maps
        DungeonGenerator generator = new DungeonGeneratorBSP(6, 1);
//        this.generator = new DungeonGeneratorWalk(1000, 5, 0.4);
//        this.generator = new DungeonGeneratorCellular(0.5);
        generator.generate(this.map, this.width, this.height);
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
}
