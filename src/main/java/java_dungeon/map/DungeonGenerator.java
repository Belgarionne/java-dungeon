package java_dungeon.map;

import javafx.geometry.Point2D;

import java.util.ArrayList;

// Base dungeon generator interface to allow multiple generation types
public interface DungeonGenerator {
    // Data created from dungeon generation
    class DungeonData {
        private final String[][] tiles;
        private final ArrayList<Point2D> enemyPoints;
        private Point2D playerStart;

        public DungeonData(int width, int height) {
            this.tiles = new String[height][width];
            this.playerStart = new Point2D(0, 0);
            this.enemyPoints = new ArrayList<>();
        }

        public String[][] getTiles() {
            return tiles;
        }

        public Point2D getPlayerStart() {
            return playerStart;
        }

        public ArrayList<Point2D> getEnemyPoints() {
            return enemyPoints;
        }

        public void setPlayerStart(Point2D playerStart) {
            this.playerStart = playerStart;
        }
    }

    DungeonData generate(int width, int height);
}
