package java_dungeon.objects;

import javafx.geometry.Point2D;

public class Enemy {
    private Point2D position;

    public Enemy(double x, double y) {
        this.position = new Point2D(x, y);
    }

    public Point2D getPosition() {
        return position;
    }
    public void setPosition(Point2D position) {
        this.position = position;
    }
}
