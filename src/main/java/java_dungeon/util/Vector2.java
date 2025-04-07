package java_dungeon.util;

// Utility 2-dimensional vector class (used instead of JavaFX Point2D for clarity and control)
public class Vector2 {
    private double x;
    private double y;

    public Vector2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }

    public void setX(double x) {
        this.x = x;
    }
    public void setY(double y) {
        this.y = y;
    }

    public Vector2 add(Vector2 b) {
        x += b.getX();
        y += b.getY();
        return this;
    }

    public Vector2 sub(Vector2 b) {
        x -= b.getX();
        y -= b.getY();
        return this;
    }

    public Vector2 mul(double b) {
        x *= b;
        y *= b;
        return this;
    }

    public Vector2 div(double b) {
        x /= b;
        y /= b;
        return this;
    }

    public double magnitude() {
        return Math.sqrt(x*x + y*y);
    }

    public double distance(Vector2 b) {
        double xDist = b.getX() - x;
        double yDist = b.getY() - y;
        return Math.sqrt(xDist*xDist + yDist*yDist);
    }

    public Vector2 normalize() {
        // Can't normalize a zero length vector
        if (x == 0 && y == 0) { return this; }

        double scale = 1 / magnitude();
        x *= scale;
        y *= scale;

        return this;
    }
}
