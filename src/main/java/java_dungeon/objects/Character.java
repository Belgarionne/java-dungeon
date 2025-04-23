package java_dungeon.objects;

import java_dungeon.Globals;
import javafx.geometry.Point2D;

public abstract class Character {
    protected Point2D position;
    protected int health;
    protected int maxHealth;
    protected int damage;

    public Character(Point2D position, int hp, int dmg) {
        this.position = new Point2D(position.getX(), position.getY());
        this.health = hp;
        this.maxHealth = hp;
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

    public int getMaxHealth() {
        return maxHealth;
    }
    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
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
    }

    public void attack(Character target) {
        // Just use class names for now
        String name = this.getClass().getSimpleName();
        String otherName = target.getClass().getSimpleName();

        // Just a basic attack
        target.takeDamage(damage);

        // Print the action
        Globals.logger.logMessage(String.format("%s attacks %s for %d damage. %s", name, otherName, damage, target.getHealthMessage()));
    }

    public String getHealthMessage() {
        String name = this.getClass().getSimpleName();
        return (isDead()) ? String.format("%s is dead.", name) : String.format("%s's health is now %d.", name, health);
    }
}
