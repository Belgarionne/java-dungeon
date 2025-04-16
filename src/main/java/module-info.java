module java_dungeon {
    requires javafx.controls;
    requires javafx.fxml;

//    opens java_dungeon to javafx.fxml;
    opens java_dungeon.controllers to javafx.fxml;
    exports java_dungeon;
}