package java_dungeon.items;

public class Equipment extends Item {
    public enum EquipSlot {
        WEAPON, ARMOR;

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }

    private final EquipSlot slot;
    private final int damage;
    private final int defense;

    public Equipment(String id, String name, String sprite, int level, EquipSlot slot, int dmg, int def) {
        super(id, name, sprite, level);

        this.slot = slot;
        this.damage = dmg;
        this.defense = def;
    }

    public EquipSlot getSlot() {
        return slot;
    }
    public int getDamage() {
        return damage;
    }
    public int getDefense() {
        return defense;
    }
}
