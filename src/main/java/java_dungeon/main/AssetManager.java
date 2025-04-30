package java_dungeon.main;

import java_dungeon.items.ItemFactory;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

public class AssetManager {
    public static class AtlasImage {
        private final Image img;
        private final HashMap<String, Rectangle2D> frames;

        public AtlasImage(Image img) {
            this.img = img;
            this.frames = new HashMap<>();
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
    private static final HashMap<String, AtlasImage> images = new HashMap<>();
    private static final ItemFactory itemFactory = new ItemFactory();

    public static HashMap<String, AtlasImage> getImages() {
        return images;
    }
    public static ItemFactory getItemFactory() {
        return itemFactory;
    }

    public static void initialize() {
        AtlasImage tileset = loadAtlas("Tileset", "dungeon-tiles.png");

        // Add all the tiles
        String[] tileNames = {
            "Wall",
            "Ground",
            "Door",
            "Boss-Door",
            "Stairs",
            "Weapon",
            "Armor",
            "Potion",
            "Player",
            "Cultist",
            "Slime",
            "Skeleton"
        };
        int tilesPerRow = (int)(tileset.getImg().getWidth() / Globals.TILE_SIZE);
        int tilesPerCol = (int)(tileset.getImg().getHeight() / Globals.TILE_SIZE);

        for (int y = 0; y < tilesPerCol; y++) {
            for (int x = 0; x < tilesPerRow; x++) {
                int nameIndex = y * tilesPerRow + x;

                // Any unspecified tile is called UNTITLED-<overflow amount>
                tileset.addFrame(
                    (nameIndex < tileNames.length) ? tileNames[nameIndex] : "UNTITLED-" + (nameIndex - tileNames.length),
                    new Rectangle2D(x * Globals.TILE_SIZE, y * Globals.TILE_SIZE, Globals.TILE_SIZE, Globals.TILE_SIZE)
                );
            }
        }

        itemFactory.initialize();

        // Load the highscore
        loadRecords();
    }

    public static AtlasImage loadAtlas(String name, String path) {
        AtlasImage atlas = new AtlasImage(new Image(path));
        images.put(name, atlas);
        return atlas;
    }

    public static void saveRecords() {
        try {
            FileWriter writer = new FileWriter("records.txt");
            // Dungeon data section
            writer.write("HIGHSCORE\n");
            writer.write(String.format("floor:%d|playerLvl:%d\n", Globals.floorHigh, Globals.playerLvlHigh));

            writer.close();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void loadRecords() {
        File recordsFile = new File("records.txt");
        Scanner reader;
        try {
            reader = new Scanner(recordsFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        while (reader.hasNextLine()) {
            String line = reader.nextLine();

            // Skip empty or commented out lines (// means commented out)
            if (line.equalsIgnoreCase("") || line.substring(0, 2).equalsIgnoreCase("//")) {
                continue;
            }

            String[] properties = reader.nextLine().split("\\|"); // Properties are seperated by |

            switch (line) {
                case "HIGHSCORE":
                    for (String prop : properties) {
                        String[] values = prop.split(":"); // Property values are seperated by :
                        switch (values[0]) {
                            case "floor" -> Globals.floorHigh = Integer.parseInt(values[1]);
                            case "playerLvl" -> Globals.playerLvlHigh = Integer.parseInt(values[1]);
                        }
                    }
                    break;
            }
        }

        reader.close();
    }
}
