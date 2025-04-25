package java_dungeon.gui;

import java_dungeon.items.Item;
import java_dungeon.main.AssetManager;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class ItemView extends StackPane {
    private Item heldItem;
    private final ImageView itemImage;
    private final Rectangle highlight;
    private final Label equippedLabel;
    private final Label levelLabel;

    public ItemView() {
        getStyleClass().add("item-view");

        // Inner shadow effect
        InnerShadow innerShadow = new InnerShadow();
        innerShadow.setOffsetX(4);
        innerShadow.setOffsetY(4);
        setEffect(innerShadow);

        itemImage = new ImageView(AssetManager.getImages().get("Tileset").getImg());
        itemImage.setViewport(AssetManager.getImages().get("Tileset").getFrame("Armor"));
        itemImage.setFitWidth(48);
        itemImage.setFitHeight(48);

        highlight = new Rectangle(0, 0, 64, 64);
        highlight.setFill(Color.GREEN);

        equippedLabel = new Label("E");
        equippedLabel.setAlignment(Pos.BOTTOM_RIGHT);
        equippedLabel.setTranslateX(-4); // Small right spacing
        equippedLabel.setMinSize(64, 64);
        equippedLabel.setMaxSize(64, 64);

        levelLabel = new Label("+1");
        levelLabel.setAlignment(Pos.BOTTOM_LEFT);
        levelLabel.setTranslateX(4); // Small left spacing
        levelLabel.setMinSize(64, 64);
        levelLabel.setMaxSize(64, 64);

        getChildren().add(highlight);
        getChildren().add(itemImage);
        getChildren().add(equippedLabel);
        getChildren().add(levelLabel);

        resetUI();
    }

    public Item getHeldItem() {
        return heldItem;
    }
    public void setHeldItem(Item item) {
        this.heldItem = item;
        resetUI();

        // Removing the item
        if (heldItem == null) {
            return;
        }

        itemImage.setVisible(true);
        levelLabel.setVisible(true);

        itemImage.setViewport(AssetManager.getImages().get("Tileset").getFrame(item.getSprite()));
        levelLabel.setText("+" + item.getLevel());
    }

    public void setEquipState(boolean state) {
        highlight.setVisible(state);
        equippedLabel.setVisible(state);
    }

    private void resetUI() {
        itemImage.setVisible(false);
        highlight.setVisible(false);
        equippedLabel.setVisible(false);
        levelLabel.setVisible(false);
    }
}
