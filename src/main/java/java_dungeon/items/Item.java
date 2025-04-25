package java_dungeon.items;

public class Item {
    private final String id;
    private final String name;
    private final String sprite;
    private final int level;

    public Item(String id, String name, String sprite, int level) {
        this.id = id;
        this.name = name;
        this.sprite = sprite;
        this.level = level;
    }

    public String getId() {
        return id;
    }
    public String getName() {
        return name + " +" + level;
    }
    public String getSprite() {
        return sprite;
    }
    public int getLevel() {
        return level;
    }
}
