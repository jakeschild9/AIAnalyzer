package edu.missouristate.aianalyzer.ui.view.Metrics;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/**
 * A reusable JavaFX component that displays a single metric using a large counter
 * and an optional sparkline (AreaChart) for history visualization.
 * It encapsulates its own styling, including background color and shadow effect.
 */
public class MetricCard extends StackPane {

    private final Label countLabel;
    private final AreaChart<Number, Number> sparkline;
    private final XYChart.Series<Number, Number> series;

    public MetricCard(String title, String color, boolean showGraph) {
        // 1. Setup Container and Styling
        this.setStyle(String.format(
                "-fx-background-color: %s; " +
                        "-fx-background-radius: 8px; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 5);",
                color));

        this.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        this.setPrefHeight(180);

        // 2. Setup Layout
        VBox cardContent = new VBox(5);
        cardContent.setAlignment(Pos.TOP_LEFT);

        // Adjust padding based on whether the history graph is present.
        if (showGraph) {
            cardContent.setPadding(new Insets(15, 20, 0, 20));
        } else {
            cardContent.setPadding(new Insets(15, 20, 15, 20));
        }

        // 3. Labels
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: rgba(255,255,255,0.9);");

        countLabel = new Label("0"); // Initialize count to zero
        countLabel.setStyle("-fx-font-size: 48px; -fx-font-weight: bold; -fx-text-fill: white;");

        cardContent.getChildren().addAll(titleLabel, countLabel);

        // 4. Sparkline initialization
        if (showGraph) {
            series = new XYChart.Series<>();
            sparkline = createSparkline();
            // Allow the graph to grow and fill available vertical space.
            VBox.setVgrow(sparkline, Priority.ALWAYS);
            cardContent.getChildren().add(sparkline);
        } else {
            sparkline = null;
            series = null;
        }
        this.getChildren().add(cardContent);
    }

    /**
     * Updates the primary numerical count displayed on the card, formatted with commas.
     * @param count The new long count value.
     */
    public void updateCount(long count) {
        Platform.runLater(() -> countLabel.setText(String.format("%,d", count)));
    }

    /**
     * Updates the sparkline visualization with new history data.
     * The method clears previous data and plots new points based on the index (time) and value (activity).
     * @param historyList A list of integers representing activity over time.
     */
    public void updateSparkline(List<Integer> historyList) {
        if (series == null || historyList == null) return;

        Platform.runLater(() -> {
            series.getData().clear();
            // Loop through the history and plot points
            for (int i = 0; i < historyList.size(); i++) {
                series.getData().add(new XYChart.Data<>(i, historyList.get(i)));
            }
        });
    }

    // Creates and configures the AreaChart used as a sparkline.
    private AreaChart<Number, Number> createSparkline() {
        // Create invisible axes for the minimal sparkline look.
        NumberAxis xSpark = new NumberAxis();
        xSpark.setOpacity(0);
        xSpark.setPadding(Insets.EMPTY);

        NumberAxis ySpark = new NumberAxis();
        ySpark.setOpacity(0);
        ySpark.setPadding(Insets.EMPTY);

        AreaChart<Number, Number> chart = new AreaChart<>(xSpark, ySpark);
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setCreateSymbols(false);
        chart.setHorizontalGridLinesVisible(false);
        chart.setVerticalGridLinesVisible(false);
        chart.setPadding(new Insets(0, -5, -5, -5)); // Negative margins to fill container edges
        chart.setMinHeight(80);
        chart.setPrefHeight(80);

        // Apply internal CSS styling to remove chart borders and padding.
        chart.lookup(".chart-content").setStyle("-fx-padding: 0;");
        chart.setStyle("-fx-background-color: transparent;");

        chart.getData().add(series);

        // Apply visual styling to the fill and line once nodes are created and attached.
        Platform.runLater(() -> {
            Node fill = series.getNode().lookup(".chart-series-area-fill");
            Node line = series.getNode().lookup(".chart-series-area-line");
            if (line != null) line.setStyle("-fx-stroke: rgba(255,255,255,0.9); -fx-stroke-width: 2px;");
            // Use a gradient for the fill effect.
            if (fill != null) fill.setStyle("-fx-fill: linear-gradient(to bottom, rgba(255,255,255,0.3), rgba(255,255,255,0.0));");
        });

        return chart;
    }

    /**
     * Updates the count label with a custom text string, used primarily for data like 'Data Processed'.
     * @param text The new string value for the label.
     */
    public void updateText(String text) {
        Platform.runLater(() -> countLabel.setText(text));
    }
}