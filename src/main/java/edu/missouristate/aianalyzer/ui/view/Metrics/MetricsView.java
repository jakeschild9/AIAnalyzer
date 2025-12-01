package edu.missouristate.aianalyzer.ui.view.Metrics;

import edu.missouristate.aianalyzer.service.metrics.MetricsAggregationService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * JavaFX view component responsible for displaying aggregated application metrics,
 * including file analysis counts, throughput, and user actions, using dashboard cards and charts.
 */
@Component
public class MetricsView extends ScrollPane {

    private final MetricsAggregationService metricsService;

    // Dashboard Cards for key metrics
    private final MetricCard safeCard;
    private final MetricCard suspiciousCard;
    private final MetricCard maliciousCard;
    private final MetricCard unclassifiedCard;

    private final MetricCard throughputCard;
    private final MetricCard actionRequiredCard;
    private final MetricCard queueCard;

    // Chart Components
    private StackedBarChart<String, Number> userActionsChart;
    private CategoryAxis xAxis;
    private final XYChart.Series<String, Number> ignoreSeries = new XYChart.Series<>();
    private final XYChart.Series<String, Number> quarantineSeries = new XYChart.Series<>();
    private final XYChart.Series<String, Number> deleteSeries = new XYChart.Series<>();

    private Timeline autoRefreshTimeline;
    private volatile boolean loading = false;

    // Colors for consistency in charts and styling
    private static final String IGNORE_COLOR = "#2a6f85";
    private static final String QUARANTINE_COLOR = "#b07d1a";
    private static final String DELETE_COLOR = "#a33a37";

    /**
     * Constructor for Spring dependency injection. Initializes service and sets up the UI layout.
     * @param metricsService The service used to fetch aggregated metrics data.
     */
    public MetricsView(MetricsAggregationService metricsService) {
        this.metricsService = metricsService;

        ignoreSeries.setName("Ignored");
        quarantineSeries.setName("Quarantined");
        deleteSeries.setName("Deleted");

        // Layout Setup
        VBox contentBox = new VBox();
        contentBox.getStyleClass().add("page-container");
        contentBox.setAlignment(Pos.TOP_CENTER);
        contentBox.setPadding(new Insets(20));
        contentBox.setMaxWidth(1800);
        contentBox.setFillWidth(true);

        Label header = new Label("Metrics of Files Scanned by AI Analizer");
        // Apply CSS Class for header styling
        header.getStyleClass().add("drive-header-label");

        // --- Grid Layout Setup (4 COLUMNS) ---
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10, 0, 10, 0));
        gridPane.setHgap(20);
        gridPane.setVgap(20);

        // Configure column widths to be equal (25% each)
        javafx.scene.layout.ColumnConstraints col1 = new javafx.scene.layout.ColumnConstraints(); col1.setPercentWidth(25);
        javafx.scene.layout.ColumnConstraints col2 = new javafx.scene.layout.ColumnConstraints(); col2.setPercentWidth(25);
        javafx.scene.layout.ColumnConstraints col3 = new javafx.scene.layout.ColumnConstraints(); col3.setPercentWidth(25);
        javafx.scene.layout.ColumnConstraints col4 = new javafx.scene.layout.ColumnConstraints(); col4.setPercentWidth(25);
        gridPane.getColumnConstraints().addAll(col1, col2, col3, col4);

        // --- ROW 1: Classification Buckets ---
        safeCard = new MetricCard("Safe Files", "#4CAF50", true);
        gridPane.add(safeCard, 0, 0);

        suspiciousCard = new MetricCard("Suspicious Files", "#FF9800", true);
        gridPane.add(suspiciousCard, 1, 0);

        maliciousCard = new MetricCard("Malicious Files", "#F44336", true);
        gridPane.add(maliciousCard, 2, 0);

        unclassifiedCard = new MetricCard("Unclassified", "#9E9E9E", false);
        gridPane.add(unclassifiedCard, 3, 0);

        // --- ROW 2: Performance & Queue ---
        // Throughput card spans two columns
        throughputCard = new MetricCard("Data Processed", "#607D8B", true);
        gridPane.add(throughputCard, 0, 1, 2, 1);

        actionRequiredCard = new MetricCard("Action Required", "#2196F3", false);
        gridPane.add(actionRequiredCard, 2, 1);

        queueCard = new MetricCard("Scan Queue", "#009688", true);
        gridPane.add(queueCard, 3, 1);

        // --- ROW 3: User Actions Chart ---
        Node chartCard = createUserActionsChartCard();
        gridPane.add(chartCard, 0, 2, 4, 1);

        contentBox.getChildren().addAll(header, gridPane);

        this.setContent(contentBox);
        this.setFitToWidth(true);
        this.setFitToHeight(false);
        this.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        this.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        // Set transparent background to rely on theme styling
        this.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        this.setPannable(false);

        // Add listeners to start/stop refresh based on visibility
        this.visibleProperty().addListener((obs, wasVisible, isVisible) -> {
            if (isVisible) { loadMetricsData(); startAutoRefresh(); } else { stopAutoRefresh(); }
        });
        this.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null && isVisible()) { loadMetricsData(); startAutoRefresh(); } else { stopAutoRefresh(); }
        });

        // Initial load upon creation
        loadMetricsData();
    }

    // Fetches and updates all dashboard metrics from the service.
    private void loadMetricsData() {
        if (loading) return;
        loading = true;
        new Thread(() -> {
            try {
                MetricsAggregationService.DashboardMetrics metrics = metricsService.getDashboardMetrics();
                Platform.runLater(() -> {
                    // Update main counts
                    safeCard.updateCount(metrics.safeCount);
                    suspiciousCard.updateCount(metrics.suspiciousCount);
                    maliciousCard.updateCount(metrics.maliciousCount);
                    unclassifiedCard.updateCount(metrics.unclassifiedCount);
                    actionRequiredCard.updateCount(metrics.getActionRequiredCount());
                    queueCard.updateCount(metrics.queueCount);

                    // Update sparklines and special text fields
                    queueCard.updateSparkline(metrics.queueHistory);
                    throughputCard.updateText(formatBytes(metrics.totalBytesProcessed));
                    safeCard.updateSparkline(metrics.safeHistory);
                    suspiciousCard.updateSparkline(metrics.suspiciousHistory);
                    maliciousCard.updateSparkline(metrics.maliciousHistory);
                    throughputCard.updateSparkline(metrics.throughputHistory);

                    updateUserActionsChart(metrics.userActionsByType);
                    loading = false;
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    loading = false;
                    e.printStackTrace();
                });
            }
        }, "metrics-loader").start();
    }

    // Formats a byte count into a human-readable string (KB, MB, GB, etc.).
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp-1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    /**
     * Updates the data series in the Stacked Bar Chart based on the latest user actions
     * aggregated by file type.
     * * The chart displays the top 8 file types by total user actions (Ignore, Quarantine, Delete).
     *
     * @param actionsByType A map where the key is the file extension and the value contains the action counts.
     */
    private void updateUserActionsChart(Map<String, MetricsAggregationService.UserActionData> actionsByType) {
        if (actionsByType == null || actionsByType.isEmpty()) {
            ignoreSeries.getData().clear();
            quarantineSeries.getData().clear();
            deleteSeries.getData().clear();
            return;
        }

        // Filter out file types with no actions and sort by total actions (descending), limiting to top 8.
        List<Map.Entry<String, MetricsAggregationService.UserActionData>> sortedEntries = actionsByType.entrySet().stream()
                .filter(e -> (e.getValue().ignores + e.getValue().quarantines + e.getValue().deletes) > 0)
                .sorted((e1, e2) -> {
                    long t1 = e1.getValue().ignores + e1.getValue().quarantines + e1.getValue().deletes;
                    long t2 = e2.getValue().ignores + e2.getValue().quarantines + e2.getValue().deletes;
                    return Long.compare(t2, t1);
                })
                .limit(8)
                .toList();

        // Update the categories shown on the X-axis (file types).
        List<String> newCategories = new ArrayList<>();
        for (var entry : sortedEntries) {
            String ft = entry.getKey();
            if (ft == null || ft.isEmpty()) ft = "unknown";
            newCategories.add(ft);
        }
        xAxis.setCategories(FXCollections.observableArrayList(newCategories));

        // Update the data points for each series.
        updateSeriesData(ignoreSeries, sortedEntries, data -> data.ignores);
        updateSeriesData(quarantineSeries, sortedEntries, data -> data.quarantines);
        updateSeriesData(deleteSeries, sortedEntries, data -> data.deletes);

        applyChartStyling();
    }

    /**
     * Efficiently updates the data points for a specific chart series.
     * This minimizes changes by updating existing points or adding new ones only if necessary.
     *
     * @param series The XYChart.Series to update (e.g., ignoreSeries).
     * @param sortedEntries The list of file type data entries, sorted and filtered.
     * @param valueExtractor A function to extract the relevant metric (ignores, quarantines, or deletes) from the data.
     */
    private void updateSeriesData(XYChart.Series<String, Number> series,
                                  List<Map.Entry<String, MetricsAggregationService.UserActionData>> sortedEntries,
                                  java.util.function.Function<MetricsAggregationService.UserActionData, Number> valueExtractor) {

        // Remove old data points that are no longer in the top 8 categories.
        series.getData().removeIf(data ->
                sortedEntries.stream().noneMatch(e -> {
                    String ft = e.getKey();
                    if (ft == null || ft.isEmpty()) ft = "unknown";
                    return ft.equals(data.getXValue());
                })
        );

        // Update existing data points or add new ones.
        for (var entry : sortedEntries) {
            String rawType = entry.getKey();
            final String fileType = (rawType == null || rawType.isEmpty()) ? "unknown" : rawType;
            Number newValue = valueExtractor.apply(entry.getValue());

            var existingData = series.getData().stream()
                    .filter(d -> d.getXValue().equals(fileType))
                    .findFirst();

            if (existingData.isPresent()) {
                // Update only if the value has actually changed.
                if (!existingData.get().getYValue().equals(newValue)) {
                    existingData.get().setYValue(newValue);
                }
            } else {
                // Add new data point.
                series.getData().add(new XYChart.Data<>(fileType, newValue));
            }
        }
    }

    // Applies custom CSS styling to chart elements, specifically bar colors and legend text.
    private void applyChartStyling() {
        userActionsChart.layout();
        // Use a short delay to ensure chart nodes are created before styling.
        Timeline stylingTimeline = new Timeline(new KeyFrame(Duration.millis(120), ev -> {
            applySeriesColor(ignoreSeries, IGNORE_COLOR);
            applySeriesColor(quarantineSeries, QUARANTINE_COLOR);
            applySeriesColor(deleteSeries, DELETE_COLOR);

            for (Node node : userActionsChart.lookupAll(".chart-legend-item")) {
                if (node instanceof Label) {
                    Label label = (Label) node;
                    // Apply theme-consistent text color and font style to legend labels.
                    label.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: -fx-custom-text-primary;");

                    if (label.getGraphic() != null) {
                        String seriesName = label.getText();
                        String color = switch (seriesName) {
                            case "Ignored" -> IGNORE_COLOR;
                            case "Quarantined" -> QUARANTINE_COLOR;
                            case "Deleted" -> DELETE_COLOR;
                            default -> "#333333";
                        };
                        // Style the legend marker box with the series color.
                        label.getGraphic().setStyle("-fx-background-color: " + color + "; -fx-background-radius: 5px;");
                    }
                }
            }
        }));
        stylingTimeline.play();
    }

    // Sets the custom bar color for all nodes belonging to a specific series.
    private void applySeriesColor(XYChart.Series<String, Number> series, String color) {
        Node sNode = series.getNode();
        if (sNode != null) sNode.setStyle("-fx-bar-fill: " + color + ";");
        for (XYChart.Data<String, Number> d : series.getData()) {
            Node bar = d.getNode();
            if (bar != null) bar.setStyle("-fx-bar-fill: " + color + "; -fx-background-color: " + color + ";");
        }
    }

    // Creates the card container holding the Stacked Bar Chart for user actions.
    private Node createUserActionsChartCard() {
        VBox cardContent = new VBox(10);
        cardContent.setAlignment(Pos.TOP_LEFT);
        cardContent.setPadding(new Insets(20));

        Label titleLabel = new Label("User Actions by File Type");
        // Apply CSS Class for chart title styling
        titleLabel.getStyleClass().add("chart-title");

        xAxis = new CategoryAxis();
        xAxis.setLabel("File Type");
        xAxis.setTickLabelRotation(45);
        // Apply CSS Class/Variable for Axes styling
        xAxis.getStyleClass().add("chart-axis-label");
        xAxis.setAnimated(false);

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Count");
        // Apply CSS Class/Variable for Axes styling
        yAxis.getStyleClass().add("chart-axis-label");
        yAxis.setMinorTickVisible(false);
        yAxis.setAnimated(false);

        userActionsChart = new StackedBarChart<>(xAxis, yAxis);
        userActionsChart.setLegendSide(Side.RIGHT);
        userActionsChart.setLegendVisible(true);
        userActionsChart.setPrefHeight(400);
        userActionsChart.setAnimated(false);
        userActionsChart.setTitle("");

        // Chart background is set to transparent to respect the card's theme variables
        userActionsChart.setStyle("-fx-chart-plot-background: transparent; -fx-background-color: transparent;");

        userActionsChart.getData().addAll(ignoreSeries, quarantineSeries, deleteSeries);

        cardContent.getChildren().addAll(titleLabel, userActionsChart);

        StackPane card = new StackPane(cardContent);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setPrefHeight(450);

        // Apply CSS Class for card background and shadow styling
        card.getStyleClass().add("chart-card");

        return card;
    }

    // Starts the timeline for auto-refreshing metrics data every 2 seconds.
    private void startAutoRefresh() {
        if (autoRefreshTimeline != null) autoRefreshTimeline.stop();
        autoRefreshTimeline = new Timeline(new KeyFrame(Duration.seconds(2), event -> {
            // Only load data if the view is visible and not currently loading
            if (isVisible() && getScene() != null && !loading) loadMetricsData();
        }));
        autoRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        autoRefreshTimeline.play();
    }

    // Stops the timeline, disabling automatic data refresh.
    private void stopAutoRefresh() {
        if (autoRefreshTimeline != null) autoRefreshTimeline.stop();
    }
}