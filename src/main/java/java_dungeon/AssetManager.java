package java_dungeon;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;

import java.util.HashMap;

public class AssetManager {
    public static class AtlasImage {
        private final Image img;
        private final HashMap<String, Rectangle2D> frames;

        public AtlasImage(Image img) {
            this.img = img;
            this.frames = new HashMap<String, Rectangle2D>();
        }

        public Image getImg() {
            return img;
        }

        public void addFrame(String name, Rectangle2D rect) {
            this.frames.put(name, rect);
        }

        public Rectangle2D getFrame(String name) {
            return this.frames.get(name);
        }
    }
    public static int TILE_SIZE = 16;

    private static final HashMap<String, AtlasImage> images = new HashMap<String, AtlasImage>();

    public static HashMap<String, AtlasImage> getImages() {
        return images;
    }

    public static void initialize() {
        AtlasImage tileset = loadAtlas("Tileset", "dungeon-tiles-3.png");

        // Add all the tiles
        String[] tileNames = {
            "Wall",
            "Ground",
            "Door",
            "Boss-Door",
            "Unused-1",
            "Unused-2",
            "Player",
            "Unused-3",
            "Unused-4"
        };
        int tilesPerRow = (int)(tileset.getImg().getWidth() / TILE_SIZE);
        int tilesPerCol = (int)(tileset.getImg().getHeight() / TILE_SIZE);

        for (int y = 0; y < tilesPerCol; y++) {
            for (int x = 0; x < tilesPerRow; x++) {
                int nameIndex = y * tilesPerRow + x;

                // Any unspecified tile is called UNTITLED-<overflow amount>
                tileset.addFrame(
                    (nameIndex < tileNames.length) ? tileNames[nameIndex] : "UNTITLED-" + (nameIndex - tileNames.length),
                    new Rectangle2D(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE)
                );
            }
        }
    }

    public static AtlasImage loadAtlas(String name, String path) {
        AtlasImage atlas = new AtlasImage(new Image(path));
        images.put(name, atlas);
        return atlas;
    }
}
