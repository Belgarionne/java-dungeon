package java_dungeon.map;

import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.Random;

// "Random Walk" Dungeon Generator
// Useful for making sprawling cave like dungeons
public class DungeonGeneratorWalk implements DungeonGenerator {
    private final int steps;
    private final int maxRuns;
    private final double fillPercent;
    private final Random rand;

    public DungeonGeneratorWalk(int steps, int maxRuns, double fillPercent) {
        this.steps = steps;
        this.maxRuns = maxRuns;
        this.fillPercent = fillPercent;

        rand = new Random();
    }

    @Override
    public void setSeed(long seed) {
        rand.setSeed(seed);
    }

    @Override
    public DungeonData generate(int width, int height, int level) {
        DungeonData data = new DungeonData(width, height);
        String[][] map = data.getTiles();

        // Fill with walls
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                map[y][x] = "Wall";
            }
        }

        // Possible move directions (x is the first element, y is the second)
        int[][] directions = new int[][]{{0, -1}, {1, 0}, {0, 1}, {-1, 0}};
        ArrayList<int[]> possibleMoves = new ArrayList<>(4);

        int mapArea = width * height; // Total area of the map
        int placedCellCount = 0; // The number of cells that have been placed

        // Start at a random position (avoiding the 1 tile edge)
        int x = rand.nextInt(1, width - 1);
        int y = rand.nextInt(1, height - 1);

        // Only run for a certain number of attempts (or until a certain percent of the area has been filled)
        // Multiple runs helps vary the walk, instead of it clustering around 1 area
        for (int runs = 0; runs < maxRuns; runs++) {
            // Run this attempt for a certain number of steps
            for (int i = 0; i < steps; i++) {
                // Get the valid move directions
                possibleMoves.clear();
                for (int[] dir : directions) {
                    int newX = x + dir[0];
                    int newY = y + dir[1];

                    // Avoid a 1 tile border around the edge of the map
                    if (newX > 0 && newY > 0 && newX < (width - 1) && newY < (height - 1)) {
                        possibleMoves.add(dir);
                    }
                }

                int[] move = possibleMoves.get(rand.nextInt(possibleMoves.size()));

                // Only updated the placed cells count if the map is changed
                if (map[y][x].equalsIgnoreCase("Wall")) {
                    map[y][x] = "Ground";
                    placedCellCount++;

                    // Stop walk if a certain percent of the map has been filled
                    if (placedCellCount >= (mapArea * fillPercent)) {
                        break;
                    }
                }

                // Stop walk if a certain percent of the map has been filled
                if (placedCellCount >= (mapArea * fillPercent)) {
                    break;
                }

                x += move[0];
                y += move[1];
            }

            // Pick a new position (only on tiles that have been set to ground to make each step connected)
            do {
                x = rand.nextInt(1, width);
                y = rand.nextInt(1, height);
            } while (!map[y][x].equalsIgnoreCase("Ground"));
        }

        // Pick a random start location (that is not a wall)
        int startX = rand.nextInt(1, width - 1);
        int startY = rand.nextInt(1, height - 1);

        while (!map[startY][startX].equalsIgnoreCase("Ground")) {
            startX = rand.nextInt(1, width - 1);
            startY = rand.nextInt(1, height - 1);
        }

        data.setPlayerStart(new Point2D(startX, startY));

        return data;
    }
}
