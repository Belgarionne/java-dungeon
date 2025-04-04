package java_dungeon.objects;

public class Player {
    private double x;
    private double y;

    public Player(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void move(double xMovement, double yMovement) {
        this.x += xMovement;
        this.y += yMovement;
    }
}
