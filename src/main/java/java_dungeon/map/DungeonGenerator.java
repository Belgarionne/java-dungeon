package java_dungeon.map;

// Base dungeon generator interface to allow multiple generation types
public interface DungeonGenerator {
    void generate(String[][] map, int width, int height);
}
