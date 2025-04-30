package java_dungeon.map;

import javafx.geometry.Point2D;

import java.util.ArrayList;

// Base dungeon generator interface to allow multiple generation types
public interface DungeonGenerator {
    record EnemySpawnData(String name, Point2D point, String sprite, int hp, int dmg, int def, int xp) {}
    record ItemSpawnData(Point2D point, String id, int level) {}

    // Data created from dungeon generation
    class DungeonData {
        private final String[][] tiles;
        private final ArrayList<EnemySpawnData> enemySpawns;
        private final ArrayList<ItemSpawnData> itemSpawns;
        private Point2D playerStart;
        private Point2D exitPoint;

        public DungeonData(int width, int height) {
            this.tiles = new String[height][width];
            this.playerStart = new Point2D(0, 0);
            this.exitPoint = new Point2D(0, 0);
            this.enemySpawns = new ArrayList<>();
            this.itemSpawns = new ArrayList<>();
        }

        public String[][] getTiles() {
            return tiles;
        }

        public Point2D getPlayerStart() {
            return playerStart;
        }

        public Point2D getExitPoint() {
            return exitPoint;
        }

        public ArrayList<EnemySpawnData> getEnemySpawns() {
            return enemySpawns;
        }

        public ArrayList<ItemSpawnData> getItemSpawns() {
            return itemSpawns;
        }

        public void setPlayerStart(Point2D playerStart) {
            this.playerStart = playerStart;
        }

        public void setExitPoint(Point2D exitPoint) {
            this.exitPoint = exitPoint;
        }
    }

    void setSeed(long seed);
    DungeonData generate(int width, int height, int level);
}
