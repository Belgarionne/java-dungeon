package java_dungeon.map;

import java_dungeon.util.Vector2;

import java.util.ArrayList;

// Base dungeon generator interface to allow multiple generation types
public interface DungeonGenerator {
    // Data created from dungeon generation
    class DungeonData {
        private String[][] tiles;
        private Vector2 playerStart;
        private ArrayList<Vector2> enemyPoints;

        public DungeonData(int width, int height) {
            this.tiles = new String[height][width];
            this.playerStart = new Vector2(0, 0);
            this.enemyPoints = new ArrayList<>();
        }

        public String[][] getTiles() {
            return tiles;
        }
        public void setTiles(String[][] tiles) {
            this.tiles = tiles;
        }

        public Vector2 getPlayerStart() {
            return playerStart;
        }

        public void setPlayerStart(Vector2 playerStart) {
            this.playerStart = playerStart;
        }

        public ArrayList<Vector2> getEnemyPoints() {
            return enemyPoints;
        }

        public void setEnemyPoints(ArrayList<Vector2> enemyPoints) {
            this.enemyPoints = enemyPoints;
        }
    }

    DungeonData generate(int width, int height);
}
