package java_dungeon.objects;

import javafx.geometry.Point2D;

public class Enemy {
    private Point2D position;
    private final double sightDistance;
    private Point2D targetPoint;

    public Enemy(double x, double y) {
        this.position = new Point2D(x, y);
        this.targetPoint = null;
        this.sightDistance = 10.0;
    }

    public Point2D getPosition() {
        return position;
    }
    public void setPosition(Point2D position) {
        this.position = position;
    }

    public double getSightDistance() {
        return sightDistance;
    }

    public Point2D getTargetPoint() {
        return targetPoint;
    }
    public void setTargetPoint(Point2D targetPoint) {
        this.targetPoint = targetPoint;
    }

    public void move(Point2D movement) {
        position = position.add(movement);
    }
}
