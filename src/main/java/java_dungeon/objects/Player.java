package java_dungeon.objects;

import java_dungeon.util.Vector2;

public class Player {
    private Vector2 position;

    public Player(double x, double y) {
        this.position = new Vector2(x, y);
    }

    public Vector2 getPosition() {
        return position;
    }

    public void setPosition(Vector2 position) {
        this.position = position;
    }

    public void move(Vector2 movement) {
        position.add(movement);
    }
}
