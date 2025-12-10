package edu.missouristate.aianalyzer.ui.view.Home;

import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Custom JavaFX component representing a dashboard card for a file category.
 * Displays file count, total size, and a progress bar showing the percentage
 * of files belonging to this category relative to the total indexed files.
 */
public class CategoryCard extends VBox {

    private final Label countLabel;
    private final Label sizeLabel;
    private final Label percentLabel;
    // Property to bind to the progress bar width, representing file percentage.
    private final SimpleDoubleProperty progressProperty = new SimpleDoubleProperty(0.0);

    public CategoryCard(String title, String colorHex, Runnable onClick) {
        this.setPadding(new Insets(15));
        this.setSpacing(8);
        this.setPrefSize(220, 130);

        // Apply base CSS styling for the container.
        this.getStyleClass().add("category-card");

        // Apply dynamic border color based on the category's theme color.
        this.setStyle("-fx-border-color: " + colorHex + "; -fx-border-width: 0 0 0 4;");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("card-title");

        countLabel = new Label("0 files");
        countLabel.getStyleClass().add("card-subtitle");

        sizeLabel = new Label("0 B");
        // Apply dynamic text color and specialized font for the size label.
        sizeLabel.setStyle("-fx-text-fill: " + colorHex + "; -fx-font-weight: bold; -fx-font-family: 'Consolas'; -fx-font-size: 12px;");

        HBox stats = new HBox(countLabel, sizeLabel);
        stats.setSpacing(10);
        HBox.setHgrow(sizeLabel, Priority.ALWAYS);
        sizeLabel.setMaxWidth(Double.MAX_VALUE);
        sizeLabel.setAlignment(Pos.CENTER_RIGHT);

        // --- Custom Progress Bar Implementation ---
        StackPane barContainer = new StackPane();
        barContainer.setAlignment(Pos.CENTER_LEFT);
        barContainer.setPrefHeight(6);
        barContainer.setMaxWidth(Double.MAX_VALUE);

        // The background track of the progress bar.
        Rectangle track = new Rectangle();
        track.heightProperty().bind(barContainer.heightProperty());
        track.widthProperty().bind(barContainer.widthProperty());

        track.getStyleClass().add("custom-progress-track");

        track.setArcWidth(4); track.setArcHeight(4);

        // The colored fill layer representing the progress.
        Rectangle fill = new Rectangle();
        fill.heightProperty().bind(barContainer.heightProperty());
        fill.setFill(Color.web(colorHex));
        fill.setArcWidth(4); fill.setArcHeight(4);
        // Bind width to the container's width multiplied by the progress ratio.
        fill.widthProperty().bind(barContainer.widthProperty().multiply(progressProperty));

        barContainer.getChildren().addAll(track, fill);

        percentLabel = new Label("0% of all files");

        // Use CSS class for the percentage label text color.
        percentLabel.getStyleClass().add("custom-percent-label");

        percentLabel.setMaxWidth(Double.MAX_VALUE);
        percentLabel.setAlignment(Pos.CENTER_RIGHT);

        // Set the card to be clickable and handle the action.
        this.setCursor(javafx.scene.Cursor.HAND);
        this.setOnMouseClicked(e -> onClick.run());

        this.getChildren().addAll(titleLabel, stats, barContainer, percentLabel);
    }

    // Updates the displayed metrics (count, size, and percentage) on the JavaFX thread.
    public void update(long count, long bytes, long totalSystemFiles) {
        Platform.runLater(() -> {
            countLabel.setText(String.format("%,d files", count));
            sizeLabel.setText(formatBytes(bytes));

            // Calculate the ratio of files in this category to the total indexed files.
            double ratio = (totalSystemFiles > 0) ? (double) count / totalSystemFiles : 0.0;
            // Clamp the ratio to valid range.
            if (ratio > 1.0) ratio = 1.0;
            if (ratio < 0.0) ratio = 0.0;

            // Update the progress property, which automatically updates the progress bar fill width.
            progressProperty.set(ratio);

            if (ratio > 0) {
                percentLabel.setText(String.format("%.1f%% of all files", ratio * 100));
            } else {
                percentLabel.setText("No files");
            }
        });
    }

    // Utility function to format bytes into a human-readable string (KB, MB, GB, etc.).
    private String formatBytes(long bytes) {
        if (bytes == 0) return "0 B";
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp-1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}