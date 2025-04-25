package java_dungeon.objects;

import java_dungeon.map.GameMap;
import javafx.geometry.Point2D;

public class Enemy extends Character implements Drawable {
    private final int xpReward;

    public Enemy(Point2D position) {
        super("Enemy", position, 1, 1, 0);
        this.xpReward = 3;
    }

    public int getXpReward() {
        return xpReward;
    }

    // Override this in subclasses to make different types of AI
    public void updateAI(GameMap map, Player player) {}

    @Override
    public String getTileName() {
        return "Slime";
    }
}
