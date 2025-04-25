package java_dungeon.items;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class ItemFactory {
    private static class ItemDefinition {
        public String id = "";
        public String name = "";
        public String sprite = "";
    }

    private static class EquipmentDefinition extends ItemDefinition {
        public Equipment.EquipSlot slot = Equipment.EquipSlot.ARMOR;
        public double damageMult = 0.0;
        public double defenseMult = 0.0;
    }

    private final HashMap<String, ItemDefinition> items = new HashMap<>();
    private final Random rand = new Random();

    public void initialize() {
        // Load items
        InputStream fstream = getClass().getResourceAsStream("/itemsDatabase.txt");
        assert fstream != null;

        Scanner itemsReader = new Scanner(fstream);

        // Items are stored in the file in the form,
        // TYPE
        // property_name:value|property_name:value
        while (itemsReader.hasNextLine()) {
            String type = itemsReader.nextLine();

            // Skip empty or commented out lines (// means commented out)
            if (type.equalsIgnoreCase("") || type.substring(0, 2).equalsIgnoreCase("//")) {
                continue;
            }

            ItemDefinition def;
            String[] properties = itemsReader.nextLine().split("\\|"); // Properties are seperated by |

            switch (type) {
                case "EQUIPMENT": // Properties specific to equipment
                    def = new EquipmentDefinition();
                    for (String prop : properties) {
                        String[] values = prop.split(":"); // Property values are seperated by :
                        switch (values[0]) {
                            case "slot" -> ((EquipmentDefinition)def).slot = Equipment.EquipSlot.valueOf(values[1]);
                            case "dmg" -> ((EquipmentDefinition)def).damageMult = Double.parseDouble(values[1]);
                            case "def" -> ((EquipmentDefinition)def).defenseMult = Double.parseDouble(values[1]);
                        }
                    }
                    break;
                default: // Everything else is a base Item and doesn't have any extra properties
                    def = new ItemDefinition();
                    break;
            }

            // Common Item properties
            for (String prop : properties) {
                String[] values = prop.split(":");
                switch (values[0]) {
                    case "id" -> def.id = values[1];
                    case "name" -> def.name = values[1];
                    case "sprite" -> def.sprite = values[1];
                }
            }
            items.put(def.id, def); // Put the item into the database
        }

        itemsReader.close(); // Close the file
    }

    public Item createItem(String id, int level) {
        ItemDefinition def = items.get(id);

        // Invalid id
        if (def == null) {
            throw new RuntimeException("Invalid item ID: " + id);
        }

        return createFromDefinition(def, level);
    }

    public Item createRandomItem(int level) {
        List<ItemDefinition> itemsList = items.values().stream().toList();
        int randIndex = rand.nextInt(itemsList.size());

        return createFromDefinition(itemsList.get(randIndex), level);
    }

    private Item createFromDefinition(ItemDefinition def, int level) {
        if (def instanceof EquipmentDefinition equipDef) {
            return new Equipment(def.id, def.name, def.sprite, level, equipDef.slot, (int)(equipDef.damageMult * (level + 1)), (int)(equipDef.defenseMult * (level + 1)));
        }

        return new Item(def.id, def.name, def.sprite, level);
    }
}
