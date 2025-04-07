package java_dungeon.map;

import java.util.Random;
// "Cellular Automata" Dungeon Generator
// Useful for making cave like dungeons, with smoother, more organic tunnels (not guaranteed to be connected)
public class DungeonGeneratorCellular implements DungeonGenerator {
    private final double fillPercent;
    private final Random rand;

    public DungeonGeneratorCellular(double fillPercent) {
        this.fillPercent = fillPercent;
        this.rand = new Random();
    }

    @Override
    public void generate(String[][] map, int width, int height) {
        // Temporary grid used to hold randomly filled room
        String[][] tempGrid = new String[height][width];


        // Fill map with random walls and ground
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                tempGrid[y][x] = rand.nextDouble() < fillPercent ? "Wall" : "Ground";
            }
        }

        // 8 possible directions for neighbors
        int[][] directions = new int[][]{ {0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}};

        // Smooth out rooms (4-5 rule)
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int wallNeighbors = 0;
                for (int[] dir : directions) {
                    int dirX = x + dir[0];
                    int dirY = y + dir[1];
                    if (dirX < 0 || dirX >= width || dirY < 0 || dirY >= height) {
                        continue;
                    }
                    wallNeighbors += tempGrid[dirY][dirX].equalsIgnoreCase("Wall") ? 1 : 0;
                }

                String currentCell = tempGrid[y][x];
                // Walls stay a wall if it has >= 4 wall neighbors
                // Ground becomes a wall if it has >= 5 wall neighbors
                int requiredNeighbors = currentCell.equalsIgnoreCase("Wall") ? 4 : 5;
                map[y][x] = wallNeighbors >= requiredNeighbors ? "Wall" : "Ground";
            }
        }
    }
}
