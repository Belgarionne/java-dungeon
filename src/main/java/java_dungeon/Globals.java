package java_dungeon;

public class Globals {
    // SNES rendering size
    public static final int SCREEN_WIDTH = 256;
    public static final int SCREEN_HEIGHT = 224;
    public static final int TILE_SIZE = 16;

    // Number of tiles in the screen
    public static final double SCREEN_TILE_WIDTH = SCREEN_WIDTH / (double)TILE_SIZE;
    public static final double SCREEN_TILE_HEIGHT = SCREEN_HEIGHT / (double)TILE_SIZE;

    // Global logger used to log to the console or a log panel in the UI
    public static final Logger logger = new Logger();
    public static final int MAX_LOG_LINES = 8;
}
