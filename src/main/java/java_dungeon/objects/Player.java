package java_dungeon.objects;

import java_dungeon.items.Equipment;
import java_dungeon.items.Item;
import java_dungeon.main.Globals;
import javafx.geometry.Point2D;

public class Player extends Character implements Drawable {
    private static final int XP_GROWTH = 5;
    private static final int HP_GROWTH = 5;

    private int level;
    private int expToLevel;
    private int currentXp;

    private int equipmentDmg;
    private int equipmentDef;

    private final Item[] inventory;
    private final Equipment[] equippedItems;

    public Player(Point2D position) {
        super("Player", position, 15, 1, 0);

        this.expToLevel = 10;
        this.level = 1;
        this.currentXp = 0;

        this.equipmentDmg = 0;
        this.equipmentDef = 0;

        this.inventory = new Item[24]; // 24 slot inventory
        this.equippedItems = new Equipment[Equipment.EquipSlot.values().length];
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

            currentXp -= expToLevel;
            expToLevel += XP_GROWTH;
            calculateStats(); // Recalculate stats
            health = maxHealth; // Reset hp
        }
    }

    public int getLevel() {
        return level;
    }
    public void setLevel(int level) {
        this.level = level;
    }

    public int getExperience() {
        return currentXp;
    }
    public void setExperience(int xp) {
        this.currentXp = xp;
    }

    public int getExperienceToLevel() {
        return expToLevel;
    }
    public void setExperienceToLevel(int xp) {
        this.expToLevel = xp;
    }

    public Item getItem(int index)
    {
        return inventory[index];
    }
    public Item[] getInventory() { return inventory; }

    public int addItem(Item item) {
        // Add the item to the first available/empty space
        for (int i = 0; i < inventory.length; i++) {
            if (inventory[i] == null) {
                inventory[i] = item;
                return i; // Return the index that it was put at
            }
        }

        // No empty spaces, so inventory is full (-1 meaning failed)
        return -1;
    }
    public Item removeItem(int index) {
        Item removedItem = inventory[index];
        inventory[index] = null;
        return removedItem;
    }

    public void equip(Equipment newItem) {
        equippedItems[newItem.getSlot().ordinal()] = newItem;
        calculateStats(); // Re-calculate stats
    }

    public void unequip(Equipment.EquipSlot slot) {
        equippedItems[slot.ordinal()] = null;
        calculateStats(); // Re-calculate stats
    }

    public boolean isEquipped(Equipment item) {
        if (item == null || equippedItems[item.getSlot().ordinal()] == null) { return false; }
        return equippedItems[item.getSlot().ordinal()].equals(item);
    }

    public void calculateStats() {
        maxHealth = 15 + (level - 1) * HP_GROWTH;
        // Calculate damage from equipment
        equipmentDmg = 0;
        equipmentDef = 0;

        for (Equipment item : equippedItems) {
            if (item == null) { continue; } // No extra stats from empty slots
            equipmentDmg += item.getDamage();
            equipmentDef += item.getDefense();
        }

        damage = level + equipmentDmg;
        defense = equipmentDef;
    }
}
