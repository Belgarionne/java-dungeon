module java_dungeon {
    requires javafx.controls;
    requires javafx.fxml;

    opens java_dungeon.controllers to javafx.fxml;
    exports java_dungeon.main;
    exports java_dungeon.items; // Because AssetManager has a public static getItemFactory
}