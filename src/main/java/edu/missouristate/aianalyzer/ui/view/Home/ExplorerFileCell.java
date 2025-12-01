package edu.missouristate.aianalyzer.ui.view.Home;

import edu.missouristate.aianalyzer.model.database.FileRecord;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.awt.Desktop;
import java.io.File;
import java.nio.file.Paths;
import java.util.Locale;

public class ExplorerFileCell extends ListCell<FileRecord> {

    @Override
    protected void updateItem(FileRecord item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
            setStyle("-fx-background-color: transparent; -fx-padding: 2;");
            return;
        }

        // --- 1. Status Indicator Logic ---
        // Check if AI has actually run on this file
        boolean isScanned = item.getAiAnalyzedUnix() != null && item.getAiAnalyzedUnix() > 0;
        String label = item.getTypeLabel() != null ? item.getTypeLabel() : "Unclassified";

        Label statusDot = new Label();
        String color = "#808080"; // Default Grey (Pending)
        String tooltipText = "Pending Analysis";

        if (isScanned) {
            statusDot.setText("●"); // Solid dot for Scanned
            tooltipText = "Scanned: " + label;

            // Color coding based on result
            String l = label.toLowerCase(Locale.ROOT);
            if (l.contains("safe")) {
                statusDot.getStyleClass().add("custom-status-dot-safe");
            } else if (l.contains("suspicious")) {
                statusDot.getStyleClass().add("custom-status-dot-suspicious");
            } else if (l.contains("malicious")) {
                statusDot.getStyleClass().add("custom-status-dot-malicious");
            } else {
                statusDot.getStyleClass().add("custom-status-dot-unclassified");
            }

        } else {
            statusDot.setText("○"); // Hollow circle for Unscanned
            statusDot.getStyleClass().add("custom-status-dot-pending");
        }

        statusDot.setTooltip(new Tooltip(tooltipText));

        // --- 2. Title & Path ---
        String name = item.getPath() != null ? Paths.get(item.getPath()).getFileName().toString() : "Unknown";
        Label title = new Label(name);
        title.getStyleClass().add("custom-file-cell-title");

        // Group Dot + Title
        HBox titleRow = new HBox(8, statusDot, title);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label path = new Label(item.getPath());
        path.getStyleClass().add("custom-file-cell-path");
        path.setTooltip(new Tooltip(item.getPath()));

        VBox text = new VBox(2, titleRow, path);
        HBox.setHgrow(text, Priority.ALWAYS);

        // --- 3. Size & Action ---
        Label size = new Label(formatBytes(item.getSizeBytes()));
        size.getStyleClass().add("custom-file-cell-size");
        size.setMinWidth(60);
        size.setAlignment(Pos.CENTER_RIGHT);

        Button openBtn = new Button("📂");
        openBtn.getStyleClass().add("custom-file-cell-button");
        openBtn.setTooltip(new Tooltip("Open in Explorer"));
        openBtn.setOnAction(e -> openFileInExplorer(item.getPath()));

        HBox root = new HBox(10, text, size, openBtn);
        root.setAlignment(Pos.CENTER_LEFT);
        root.setPadding(new Insets(8, 10, 8, 10));

        // Background styling
        root.getStyleClass().add("custom-file-cell");

        setGraphic(root);
        setStyle("-fx-background-color: transparent; -fx-padding: 4;");
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp-1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    private void openFileInExplorer(String path) {
        if (path == null) return;
        new Thread(() -> {
            try {
                if (System.getProperty("os.name").toLowerCase().contains("win")) {
                    new ProcessBuilder("explorer.exe", "/select,", path).start();
                } else if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(new File(path).getParentFile());
                }
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }
}