module application {
    requires javafx.controls;
    requires javafx.fxml;

    opens java_dungeon to javafx.fxml;
    exports java_dungeon;
}