package java_dungeon.objects;

import java_dungeon.map.GameMap;
import javafx.geometry.Point2D;

public class Enemy extends Character implements Drawable {
    private final int xpReward;
    private final String sprite;

    public Enemy(String name, Point2D position, String sprite, int hp, int dmg, int def, int xp) {
        super(name, position, hp, dmg, def);
        this.sprite = sprite;
        this.xpReward = xp;
    }

    public int getXpReward() {
        return xpReward;
    }

    // Override this in subclasses to make different types of AI
    public void updateAI(GameMap map, Player player) {}

    @Override
    public String getTileName() {
        return sprite;
    }
}
