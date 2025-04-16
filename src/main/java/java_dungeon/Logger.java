package java_dungeon;

import javafx.scene.control.TextArea;

public class Logger {
    private TextArea logPanel;

    public TextArea getLogPanel() {
        return logPanel;
    }
    public void setLogPanel(TextArea panel) {
        logPanel = panel;
    }

    public void logMessage(String msg) {
        System.out.printf("%s\n", msg); // Print to the console

        // Can't log to the log panel if it doesn't exist yet (not in the game scene yet)
        if (logPanel != null) {
            logPanel.appendText(msg + "\n"); // Print to the log panel
        }
    }
}
