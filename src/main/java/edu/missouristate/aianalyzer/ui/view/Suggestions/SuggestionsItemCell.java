package edu.missouristate.aianalyzer.ui.view.Suggestions;

import edu.missouristate.aianalyzer.model.database.DecisionType;
import edu.missouristate.aianalyzer.model.database.FileRecord;
import edu.missouristate.aianalyzer.repository.database.FileRecordRepository;
import edu.missouristate.aianalyzer.service.database.FileIsolationService;
import edu.missouristate.aianalyzer.service.metrics.MetricsService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

public class SuggestionsItemCell extends ListCell<FileRecord> {

    private final FileRecordRepository fileRecordRepository;
    private final FileIsolationService fileIsolationService;
    private final MetricsService metricsService;
    private final Label statusLabel;

    // Shared state from the parent view to track which items are expanded.
    private final Set<Long> expandedIds;

    // Formatter for displaying time in the UI.
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    public SuggestionsItemCell(FileRecordRepository fileRecordRepository,
                               FileIsolationService fileIsolationService,
                               MetricsService metricsService,
                               Label statusLabel,
                               Set<Long> expandedIds) {
        this.fileRecordRepository = fileRecordRepository;
        this.fileIsolationService = fileIsolationService;
        this.metricsService = metricsService;
        this.statusLabel = statusLabel;
        this.expandedIds = expandedIds;

        setStyle("-fx-background-color: transparent; -fx-padding: 5 0 5 0;");
    }

    @Override
    protected void updateItem(FileRecord item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
            setStyle("-fx-background-color: transparent; -fx-padding: 5 0 5 0;");
            return;
        }

        // --- 1. THE HEADER (Always visible section) ---
        BorderPane headerPane = createHeader(item);

        // --- 2. THE DETAILS (Expandable section) ---
        VBox detailsPane = createDetails(item);

        // Check the shared state to determine initial visibility.
        boolean isExpanded = expandedIds.contains(item.getId());

        detailsPane.setVisible(isExpanded);
        detailsPane.setManaged(isExpanded);

        // Set initial arrow rotation based on the expanded state.
        Label arrow = (Label) headerPane.getLeft();
        arrow.setRotate(isExpanded ? 90 : 0);

        // --- 3. EXPAND/COLLAPSE LOGIC ---
        headerPane.setOnMouseClicked(e -> {
            boolean currentlyExpanded = expandedIds.contains(item.getId());

            if (currentlyExpanded) {
                expandedIds.remove(item.getId()); // Collapse
            } else {
                expandedIds.add(item.getId());    // Expand
            }

            // Apply UI Updates
            boolean newVisibleState = !currentlyExpanded;
            detailsPane.setVisible(newVisibleState);
            detailsPane.setManaged(newVisibleState);
            arrow.setRotate(newVisibleState ? 90 : 0);

            // Request layout recalculation to adjust list cell height.
            getListView().requestLayout();
        });

        // --- 4. ROOT CONTAINER ---
        VBox root = new VBox(0, headerPane, detailsPane);

        String accentColor = severityColor(item.getTypeLabel());
        String borderStyle = "-fx-border-color: " + accentColor + " transparent transparent " + accentColor + ";";
        String borderWidth = "-fx-border-width: 0 0 0 5;";

        root.getStyleClass().add("suggestions-item-card");
        root.setStyle(borderStyle + borderWidth);

        setGraphic(root);
    }

    // Creates the detailed, hidden section of the cell.
    private VBox createDetails(FileRecord item) {
        VBox box = new VBox(10);
        box.setPadding(new Insets(15));
        box.getStyleClass().add("suggestions-details-pane");

        Label header = new Label("Full AI Analysis");
        header.getStyleClass().add("suggestions-details-header");

        Label fullSummary = new Label(item.getAiSummary() != null ? item.getAiSummary() : "No analysis data available.");
        fullSummary.setWrapText(true);
        fullSummary.getStyleClass().add("suggestions-details-text");

        GridPane metaGrid = new GridPane();
        metaGrid.setHgap(15);
        metaGrid.setVgap(5);

        addMetaRow(metaGrid, 0, "File Size:", formatBytes(item.getSizeBytes()));
        addMetaRow(metaGrid, 1, "Hash (SHA256):", item.getContentHash() != null ? item.getContentHash() : "N/A");
        addMetaRow(metaGrid, 2, "Last Scanned:", formatTime(item.getLastScannedUnix()));
        addMetaRow(metaGrid, 3, "AI Analyzed:", formatTime(item.getAiAnalyzedUnix()));

        box.getChildren().addAll(header, fullSummary, new Separator(), metaGrid);
        return box;
    }

    // Helper method to add a label-value pair to the metadata grid.
    private void addMetaRow(GridPane grid, int row, String label, String value) {
        Label l = new Label(label);
        l.getStyleClass().add("suggestions-meta-label");

        Label v = new Label(value);
        v.getStyleClass().add("suggestions-meta-value");

        grid.add(l, 0, row);
        grid.add(v, 1, row);
    }

    // Creates a standardized action button with custom styling.
    private Button createActionButton(String text, String color) {
        Button btn = new Button(text);
        btn.getStyleClass().add("suggestions-action-button");
        btn.setStyle("-fx-background-color: " + color + ";");
        btn.setOnMouseEntered(e -> btn.setOpacity(0.8));
        btn.setOnMouseExited(e -> btn.setOpacity(1.0));
        return btn;
    }

    // Formats a file size in bytes to a human-readable string (KB, MB, GB, etc.).
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    // Formats a Unix timestamp into a readable date and time string.
    private String formatTime(Long unix) {
        if (unix == null || unix <= 0) return "Never";
        return DATE_FMT.format(Instant.ofEpochSecond(unix));
    }

    // Determines the appropriate color based on the AI classification label (severity).
    private String severityColor(String label) {
        if (label == null) return "#444b55";
        String l = label.toLowerCase(Locale.ROOT);
        if (l.equals("malicious")) return "#d32f2f";
        if (l.equals("suspicious")) return "#f57c00";
        if (l.equals("safe")) return "#388e3c";
        return "#444b55";
    }

    // Sets up the action for the "Ignore" button.
    private void setupIgnoreAction(Button btn, FileRecord item) {
        btn.setOnAction(e -> {
            btn.setDisable(true);
            new Thread(() -> {
                try {
                    // 1. Log the user's decision as a metric.
                    logMetric(item, DecisionType.IGNORE, true);

                    // 2. Update the database record to mark it as ignored.
                    item.setTypeLabel("Ignored");
                    fileRecordRepository.save(item);

                    // 3. Remove the item from the UI list.
                    Platform.runLater(() -> {
                        if (getListView() != null) getListView().getItems().remove(item);
                        if (statusLabel != null) statusLabel.setText("File ignored");
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> showError("Failed to ignore: " + ex.getMessage()));
                } finally {
                    // The button remains disabled as the item is usually removed.
                }
            }).start();
        });
    }

    // Sets up the action for the "Quarantine" button.
    private void setupQuarantineAction(Button btn, FileRecord item) {
        btn.setOnAction(e -> {
            if (!confirm("Quarantine this file?\n" + item.getPath())) return;
            btn.setDisable(true);
            new Thread(() -> {
                try {
                    // Initiate the isolation process via the service.
                    fileIsolationService.isolate(item.getId());
                    logMetric(item, DecisionType.QUARANTINE, true);

                    // Update UI upon successful quarantine.
                    Platform.runLater(() -> {
                        if (getListView() != null) getListView().getItems().remove(item);
                        if (statusLabel != null) statusLabel.setText("File quarantined");
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> showError("Failed: " + ex.getMessage()));
                } finally {
                    Platform.runLater(() -> btn.setDisable(false));
                }
            }).start();
        });
    }

    // Sets up the action for the "Delete" button.
    private void setupDeleteAction(Button btn, FileRecord item) {
        btn.setOnAction(e -> {
            if (!confirm("Delete file?\n" + item.getPath())) return;
            btn.setDisable(true);
            new Thread(() -> {
                try {
                    if (item.getPath() != null) {
                        java.nio.file.Path filePath = Paths.get(item.getPath());
                        // Attempt to delete the file from the file system.
                        Files.deleteIfExists(filePath);
                        // Delete the record from the database.
                        fileRecordRepository.delete(item);
                        logMetric(item, DecisionType.DELETE, true);

                        // Update UI upon successful deletion.
                        Platform.runLater(() -> {
                            if (getListView() != null) getListView().getItems().remove(item);
                            statusLabel.setText("File deleted");
                        });
                    }
                } catch (Exception ex) {
                    Platform.runLater(() -> showError("Failed: " + ex.getMessage()));
                } finally {
                    Platform.runLater(() -> btn.setDisable(false));
                }
            }).start();
        });
    }

    // Logs the user's decision to the metrics service asynchronously.
    private void logMetric(FileRecord item, DecisionType decision, boolean success) {
        try {
            metricsService.recordUserDecisionAsync(
                    System.getProperty("user.name"),
                    String.valueOf(item.getId()),
                    item.getExt(),
                    decision,
                    null,
                    // Truncate summary if too long for the metric field.
                    item.getAiSummary() != null && item.getAiSummary().length() > 512 ? item.getAiSummary().substring(0, 512) : item.getAiSummary(),
                    success,
                    null
            );
        } catch (Exception ignore) {
        }
    }

    // Displays a confirmation dialog to the user.
    private boolean confirm(String msg) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.OK, ButtonType.CANCEL);
        alert.setHeaderText(null);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    // Displays an error alert to the user.
    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.showAndWait();
    }

    // Creates the main header pane containing title, status, and action buttons.
    private BorderPane createHeader(FileRecord item) {
        String fileName = item.getPath() != null ? Paths.get(item.getPath()).getFileName().toString() : "(unknown)";
        String summary = item.getAiSummary();
        boolean hasSummary = summary != null && !summary.isBlank();
        // Truncate summary for display in the main card.
        if (hasSummary && summary.length() > 150) summary = summary.substring(0, 150) + "...";

        String label = item.getTypeLabel() != null ? item.getTypeLabel() : "Unclassified";
        double conf = item.getAiConfidence() != null ? item.getAiConfidence() : -1.0;
        String accentColor = severityColor(label);

        // --- Arrow Indicator ---
        Label arrow = new Label("▶");
        arrow.getStyleClass().add("suggestions-expand-arrow");

        // --- Classification Badge ---
        Label badge = new Label(label.toUpperCase());
        badge.getStyleClass().add("suggestions-badge");
        badge.setStyle("-fx-background-color: " + accentColor + ";");

        // --- File Title ---
        Label title = new Label(fileName);
        title.getStyleClass().add("suggestions-title");

        Label confidence = new Label(conf >= 0 ? String.format("%.0f%%", conf * 100) : "");
        confidence.getStyleClass().add("suggestions-confidence");

        HBox titleRow = new HBox(10, title, badge, confidence);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        // --- Path Subtitle + Open Button ---
        Label subtitle = new Label(item.getPath());
        subtitle.getStyleClass().add("suggestions-path");

        // Hyperlink to open the file's location in the OS explorer.
        Hyperlink openLink = new Hyperlink("Open Location");
        openLink.getStyleClass().add("suggestions-open-link");
        openLink.setOnAction(e -> openFileLocation(item.getPath()));

        // Set hover styling for the hyperlink.
        openLink.setOnMouseEntered(e -> openLink.setStyle("-fx-text-fill: #26a69a; -fx-font-size: 10px; -fx-padding: 0 0 0 10; -fx-underline: true;"));
        openLink.setOnMouseExited(e -> openLink.setStyle("-fx-text-fill: #009688; -fx-font-size: 10px; -fx-padding: 0 0 0 10; -fx-underline: false;"));

        // Prevent expanding the card when clicking this link.
        openLink.setOnMouseClicked(e -> e.consume());

        HBox pathRow = new HBox(0, subtitle, openLink);
        pathRow.setAlignment(Pos.CENTER_LEFT);

        // --- Summary Description ---
        Label desc = new Label(hasSummary ? summary : "Awaiting AI analysis...");
        desc.setWrapText(true);
        desc.setMaxHeight(40);
        if (hasSummary) {
            desc.getStyleClass().add("suggestions-description");
        } else {
            desc.getStyleClass().add("suggestions-description-placeholder");
        }

        VBox textContainer = new VBox(4, titleRow, pathRow, desc);
        textContainer.setAlignment(Pos.CENTER_LEFT);

        // --- Action Buttons ---
        Button ignoreBtn = createActionButton("Ignore", "#2a6f85");
        Button quarantineBtn = createActionButton("Quarantine", "#b07d1a");
        Button deleteBtn = createActionButton("Delete", "#a33a37");

        setupIgnoreAction(ignoreBtn, item);
        setupQuarantineAction(quarantineBtn, item);
        setupDeleteAction(deleteBtn, item);

        HBox actions = new HBox(8, ignoreBtn, quarantineBtn, deleteBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);
        // Prevent expanding the card when clicking an action button.
        actions.setOnMouseClicked(e -> e.consume());

        BorderPane layout = new BorderPane();
        layout.setLeft(arrow);
        BorderPane.setAlignment(arrow, Pos.TOP_LEFT);
        BorderPane.setMargin(arrow, new Insets(4, 0, 0, 0));

        layout.setCenter(textContainer);
        layout.setRight(actions);
        BorderPane.setMargin(actions, new Insets(0, 0, 0, 15));

        layout.setPadding(new Insets(12, 15, 12, 5));
        layout.setCursor(javafx.scene.Cursor.HAND);

        return layout;
    }

    // Attempts to open the OS file explorer/finder with the specific file selected or revealed.
    private void openFileLocation(String path) {
        if (path == null) return;
        new Thread(() -> {
            try {
                File file = new File(path);
                if (!file.exists()) return;

                String os = System.getProperty("os.name").toLowerCase();

                if (os.contains("win")) {
                    // Windows: Open Explorer and select the file.
                    new ProcessBuilder("explorer.exe", "/select,", path).start();
                } else if (os.contains("mac")) {
                    // Mac: Reveal the file in Finder.
                    new ProcessBuilder("open", "-R", path).start();
                } else {
                    // Linux/Other: Fallback to just opening the parent folder.
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().open(file.getParentFile());
                    }
                }
            } catch (IOException e) {
                System.err.println("Failed to open file location: " + e.getMessage());
            }
        }).start();
    }
}