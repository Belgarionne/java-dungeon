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
        if (logPanel == null) { return; }

        logPanel.appendText((logPanel.getText().isEmpty()) ? msg : "\n" + msg); // Print to the log panel

        // Trim the log lines
        String[] lines = logPanel.getText().split("\n");
        if (lines.length > Globals.MAX_LOG_LINES) {
            // Add the latest lines
            StringBuilder trimmedText = new StringBuilder();
            for (int i = Globals.MAX_LOG_LINES; i > 0; i--) {
                // Only add "\n" to the start of the 2nd or later lines
                trimmedText.append((i < Globals.MAX_LOG_LINES) ? "\n" : "").append(lines[lines.length - i]);
            }

            // Update the text
            logPanel.setText(trimmedText.toString());
        }
    }
}
