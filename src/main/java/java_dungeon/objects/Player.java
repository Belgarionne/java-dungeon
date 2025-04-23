package java_dungeon.objects;

import java_dungeon.main.Globals;
import javafx.geometry.Point2D;

public class Player extends Character implements Drawable {
    private static final double XP_GROWTH_RATE = 1.5;

    private int level;
    private int expToLevel;
    private int currentXp;

    public Player(Point2D position) {
        super("Player", position, 10, 1, 0);

        expToLevel = 10;
        level = 1;
        currentXp = 0;
    }

    @Override
    public int takeDamage(int damage) {
        int totalDmg = super.takeDamage(damage);

        // ToDo: Add game over logic here
        if (isDead()) {
            System.out.println("GAME OVER...");
        }

        return totalDmg;
    }

    @Override
    public void attack(Character target) {
        super.attack(target);

        // Gain experience
        if (target.isDead() && target instanceof Enemy enemy) {
            currentXp += enemy.getXpReward();
            checkForLevelUp();
        }
    }

    @Override
    public String getTileName() {
        return "Player";
    }

    private void checkForLevelUp() {
        // Keep gaining levels until below the xp to level
        while (currentXp >= expToLevel) {
            level++;
            Globals.logger.logMessage(String.format("%s leveled up! Player is now level %d.", name, level));

            expToLevel = (int)(expToLevel * XP_GROWTH_RATE);
            currentXp -= expToLevel;
            calculateStats(); // Recalculate stats
        }
    }

    private void calculateStats() {
        damage = level;
    }
}
