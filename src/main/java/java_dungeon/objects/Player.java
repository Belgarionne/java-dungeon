package java_dungeon.objects;

import javafx.geometry.Point2D;

public class Player {
    private Point2D position;

    public Player(double x, double y) {
        this.position = new Point2D(x, y);
    }

    public Point2D getPosition() {
        return position;
    }

    public void setPosition(Point2D position) {
        this.position = position;
    }

    public void move(Point2D movement) {
        position = position.add(movement);
    }
}
