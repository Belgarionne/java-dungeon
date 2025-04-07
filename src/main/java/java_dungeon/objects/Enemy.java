package java_dungeon.objects;

import java_dungeon.util.Vector2;

public class Enemy {
    private Vector2 position;

    public Enemy(double x, double y) {
        this.position = new Vector2(x, y);
    }

    public Vector2 getPosition() {
        return position;
    }
    public void setPosition(Vector2 position) {
        this.position = position;
    }
}
