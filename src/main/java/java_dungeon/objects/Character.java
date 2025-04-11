package java_dungeon.objects;

import javafx.geometry.Point2D;

public abstract class Character {
    protected Point2D position;
    protected int health;
    protected int damage;

    public Character(Point2D position, int hp, int dmg) {
        this.position = new Point2D(position.getX(), position.getY());
        this.health = hp;
        this.damage = dmg;
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

    public int getHealth() {
        return health;
    }
    public void setHealth(int health) {
        this.health = Math.max(health, 0); // Health should never be less than 0
    }
    public boolean isDead() {
        return health <= 0;
    }

    public int getDamage() {
        return damage;
    }
    public void setDamage(int damage) {
        this.damage = damage;
    }
    public void takeDamage(int damage) {
        health = Math.max(health - damage, 0); // Health should never be negative

        // ToDo: Add combat log to UI
        // Display damage and status
        System.out.printf("%s takes %d damage.\n", this.getClass().getSimpleName(), damage);

        if (!isDead()) {
            System.out.printf("%s's health is now %d.\n", this.getClass().getSimpleName(), getHealth());
        }
        else {
            System.out.printf("%s is dead.\n", this.getClass().getSimpleName());
        }
    }

    public void attack(Character target) {
        // Just use class names for now
        String name = this.getClass().getSimpleName();
        String otherName = target.getClass().getSimpleName();

        // Print the action
        System.out.printf("%s attacks %s!\n", name, otherName);
        target.takeDamage(damage);
    }
}
