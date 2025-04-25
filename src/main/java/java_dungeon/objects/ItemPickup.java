package java_dungeon.objects;

import java_dungeon.items.Item;
import javafx.geometry.Point2D;

public class ItemPickup extends GameObject implements Drawable {
    private final Item item;

    public ItemPickup(Item item, Point2D position) {
        super(position);
        this.item = item;
    }

    public Item getItem() {
        return item;
    }

    @Override
    public String getTileName() {
        return item.getSprite();
    }
}
