package java_dungeon.objects;

import javafx.geometry.Point2D;

public class Player extends Character implements Drawable {
    public Player(Point2D position) {
        super(position, 10, 1);
    }

    @Override
    public void takeDamage(int damage) {
        super.takeDamage(damage);

        // ToDo: Add game over logic here
        if (isDead()) {
            System.out.println("GAME OVER...");
        }
    }

    @Override
    public String getTileName() {
        return "Player";
    }
}
