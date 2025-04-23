package java_dungeon.objects;

import javafx.geometry.Point2D;

public class Enemy extends Character implements Drawable {
    private final double sightDistance;
    private Point2D targetPoint;

    private final int xpReward;

    public Enemy(Point2D position) {
        super(position, 1, 1);
        this.sightDistance = 10.0;
        this.targetPoint = null;
        this.xpReward = 3;
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

    public int getXpReward() {
        return xpReward;
    }

    @Override
    public String getTileName() {
        return "Enemy";
    }
}
