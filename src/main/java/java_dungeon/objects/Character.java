package java_dungeon.objects;

import java_dungeon.main.Globals;
import javafx.geometry.Point2D;

public abstract class Character extends GameObject {
    protected final String name;
    protected int health;
    protected int maxHealth;

    protected int damage; // Adds to damage done
    protected int defense; // Subtracts from damage taken

    public Character(String name, Point2D position, int hp, int dmg, int def) {
        super(position);
        this.name = name;

        this.health = hp;
        this.maxHealth = hp;

        this.damage = dmg;
        this.defense = def;
    }

    public String getName() {
        return name;
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

    public int takeDamage(int damage) {
        int mitigatedDamage = Math.max(damage - defense, 0);
        health = Math.max(health - mitigatedDamage, 0); // Health should never be negative
        return mitigatedDamage; // Return the actual amount of damage that was done
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

    public int getDefense() {
        return defense;
    }
    public void setDefense(int defense) {
        this.defense = defense;
    }

    // Default attack behavior
    public void attack(Character target) {
        // Just a basic attack
        int dmgDone = target.takeDamage(damage);

        // Print the action
        Globals.logger.logMessage(String.format("%s attacks %s for %d damage. %s", getName(), target.getName(), dmgDone, target.getHealthMessage()));
    }

    // Returns details about the hp of the character
    public String getHealthMessage() {
        return (isDead()) ? String.format("%s is dead.", getName()) : String.format("%s's health is now %d.", getName(), health);
    }
}
