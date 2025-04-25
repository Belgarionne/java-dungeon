package java_dungeon.objects;

import javafx.geometry.Point2D;

public class GameObject {
    private Point2D position;

    public GameObject(Point2D position) {
        this.position = position;
    }

    public Point2D getPosition() {
        return position;
    }
    public void setPosition(Point2D position) {
        this.position = position;
    }

    // Used to move in a direction
    public void move(Point2D movement) {
        setPosition(getPosition().add(movement));
    }
}
