package edu.missouristate.aianalyzer.ui.view.Suggestions;

<<<<<<< HEAD
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Component;

/**
 * Component representing the AI Suggestions page.
=======
import edu.missouristate.aianalyzer.model.database.FileRecord;
import edu.missouristate.aianalyzer.repository.database.FileRecordRepository;
import edu.missouristate.aianalyzer.service.database.FileIsolationService;
import edu.missouristate.aianalyzer.service.metrics.MetricsService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * JavaFX view for displaying AI analysis suggestions.
 * Manages pagination, sorting, filtering, and auto-refresh of file records.
>>>>>>> clean-feature-branch
 */
@Component
public class SuggestionsView extends VBox {

<<<<<<< HEAD
    public SuggestionsView() {
        this.getStyleClass().add("page-container");
        this.setAlignment(Pos.CENTER);
        this.setPrefSize(700, 600);

        Label header = new Label("AI Suggestions");
        header.getStyleClass().add("header-label");

        Label content = new Label("This is the AI Suggestions page. Recommended actions can display here.");
        content.getStyleClass().add("placeholder-label");

        this.getChildren().addAll(header, content);
    }
}
=======
    private final FileRecordRepository fileRecordRepository;
    private final FileIsolationService fileIsolationService;
    private final MetricsService metricsService;

    // Stores IDs of items currently expanded in the list view to maintain state across refreshes.
    private final Set<Long> expandedIds = new HashSet<>();

    private final ListView<FileRecord> listView = new ListView<>();
    private final Label statusLabel = new Label("");
    private final ProgressIndicator progress = new ProgressIndicator();

    // Pagination state variables
    private int pageIndex = 0;
    private final int pageSize = 10;
    private int totalPages = 0;
    private long totalItems = 0;
    private volatile boolean loading = false;

    // Auto-refresh components
    private Timeline autoRefreshTimeline;
    private final CheckBox autoRefreshCheckbox = new CheckBox("Auto-refresh");

    // Pagination UI controls
    private final Button firstPageBtn = new Button("|<");
    private final Button prevPageBtn = new Button("< Prev");
    private final Button nextPageBtn = new Button("Next >");
    private final Button lastPageBtn = new Button(">|");
    private final Label pageLabel = new Label("Page 1 of 1");

    // Filtering and sorting controls
    private final TextField searchField = new TextField();
    private final ComboBox<String> filterCombo = new ComboBox<>();
    private final ComboBox<String> extensionCombo = new ComboBox<>();
    private final ComboBox<String> sortCombo = new ComboBox<>();
    private final Button refreshBtn = new Button("Refresh");

    /**
     * Constructor for Spring dependency injection. Initializes services and configures the UI layout.
     */
    @Autowired
    public SuggestionsView(FileRecordRepository fileRecordRepository,
                           FileIsolationService fileIsolationService,
                           MetricsService metricsService) {
        this.fileRecordRepository = fileRecordRepository;
        this.fileIsolationService = fileIsolationService;
        this.metricsService = metricsService;

        setAlignment(Pos.TOP_LEFT);
        setPadding(new Insets(20));
        setSpacing(15);
        setPrefSize(1000, 700);
        setStyle("-fx-background-color: transparent;");

        Label header = new Label("AI Suggestions");
        header.getStyleClass().add("drive-header-label");
        header.setStyle("-fx-font-size: 24px;");


        // --- UI Configuration and Styling ---
        searchField.setPromptText("Search by file name or path...");
        searchField.setPrefColumnCount(25);
        searchField.getStyleClass().add("custom-input-field");

        filterCombo.getItems().addAll("All", "Safe", "Suspicious", "Malicious", "Unclassified");
        filterCombo.getSelectionModel().selectFirst();
        filterCombo.setPrefWidth(110);
        filterCombo.getStyleClass().add("custom-combo-box");

        // The extension filter is populated dynamically from the database.
        extensionCombo.getStyleClass().add("custom-combo-box");
        extensionCombo.setPrefWidth(100);
        extensionCombo.setPromptText("All Types");

        sortCombo.getItems().addAll("Newest AI", "Oldest AI", "Largest", "Smallest");
        sortCombo.getSelectionModel().selectFirst();
        sortCombo.getStyleClass().add("custom-combo-box");

        refreshBtn.getStyleClass().add("custom-button");

        progress.setMaxSize(20, 20);
        progress.setVisible(false);

        autoRefreshCheckbox.setSelected(true);
        autoRefreshCheckbox.getStyleClass().add("custom-checkbox");
        autoRefreshCheckbox.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) startAutoRefresh(); else stopAutoRefresh();
        });

        // --- Toolbar Layout ---
        HBox toolbar = new HBox(12,
                new Label("Filter:") {{ setStyle("-fx-text-fill: #b0b0b0;"); }},
                filterCombo,
                extensionCombo,
                new Label("Sort:") {{ setStyle("-fx-text-fill: #b0b0b0;"); }},
                sortCombo,
                new Label("Search:") {{ setStyle("-fx-text-fill: #b0b0b0;"); }},
                searchField,
                refreshBtn,
                autoRefreshCheckbox,
                progress
        );
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(10));
        toolbar.getStyleClass().add("custom-toolbar");

        // List View setup
        listView.setPlaceholder(new Label("No AI results found.") {{ setStyle("-fx-text-fill: #666;"); }});
        // Use a custom cell factory to render each FileRecord as a SuggestionsItemCell
        listView.setCellFactory(lv -> new SuggestionsItemCell(
                fileRecordRepository,
                fileIsolationService,
                metricsService,
                statusLabel,
                expandedIds
        ));
        listView.getStyleClass().add("custom-list-view");
        VBox.setVgrow(listView, Priority.ALWAYS);

        statusLabel.getStyleClass().add("custom-status-label");

        // --- Event Listeners and Pagination Setup ---
        refreshBtn.setOnAction(e -> {
            populateExtensions(); // Re-fetch extensions on manual refresh
            loadDataReset();
        });

        // Load data whenever a filter, search, or sort parameter changes
        searchField.textProperty().addListener((obs, o, n) -> loadDataReset());
        filterCombo.valueProperty().addListener((obs, o, n) -> loadDataReset());
        extensionCombo.valueProperty().addListener((obs, o, n) -> loadDataReset());
        sortCombo.valueProperty().addListener((obs, o, n) -> loadDataReset());

        // Pagination button actions
        firstPageBtn.setOnAction(e -> goToPage(0));
        prevPageBtn.setOnAction(e -> goToPage(pageIndex - 1));
        nextPageBtn.setOnAction(e -> goToPage(pageIndex + 1));
        lastPageBtn.setOnAction(e -> goToPage(totalPages - 1));

        firstPageBtn.getStyleClass().add("custom-pagination-button");
        prevPageBtn.getStyleClass().add("custom-pagination-button");
        nextPageBtn.getStyleClass().add("custom-pagination-button");
        lastPageBtn.getStyleClass().add("custom-pagination-button");
        pageLabel.getStyleClass().add("custom-pagination-label");

        HBox paginationBar = new HBox(10, firstPageBtn, prevPageBtn, pageLabel, nextPageBtn, lastPageBtn);
        paginationBar.setAlignment(Pos.CENTER_RIGHT);
        paginationBar.setPadding(new Insets(5, 0, 0, 0));

        // Add all components to the VBox
        getChildren().addAll(header, toolbar, listView, paginationBar, statusLabel);

        // Initial View Load
        populateExtensions();
        loadDataReset();
        startAutoRefresh();
    }

    /**
     * Dynamically fetches the list of unique file extensions present in the database
     * to populate the extension filter combo box.
     */
    private void populateExtensions() {
        // Run database query in a background thread to prevent UI freezing
        new Thread(() -> {
            try {
                List<String> distinctExtensions = fileRecordRepository.findDistinctExtensions();

                Platform.runLater(() -> {
                    String currentSelection = extensionCombo.getValue();

                    extensionCombo.getItems().clear();
                    extensionCombo.getItems().add("All Types");

                    if (distinctExtensions != null) {
                        extensionCombo.getItems().addAll(distinctExtensions);
                    }

                    // Restore the previous selection or default to "All Types"
                    if (currentSelection != null && extensionCombo.getItems().contains(currentSelection)) {
                        extensionCombo.getSelectionModel().select(currentSelection);
                    } else {
                        extensionCombo.getSelectionModel().selectFirst();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Starts the recurring timeline for automatically refreshing the data view every 5 seconds.
     */
    private void startAutoRefresh() {
        if (autoRefreshTimeline != null) autoRefreshTimeline.stop();
        autoRefreshTimeline = new Timeline(new KeyFrame(javafx.util.Duration.seconds(5), event -> {
            if (!loading) loadData();
        }));
        autoRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        autoRefreshTimeline.play();
    }

    /**
     * Stops the timeline, disabling the automatic data refresh.
     */
    private void stopAutoRefresh() {
        if (autoRefreshTimeline != null) autoRefreshTimeline.stop();
    }

    /**
     * Changes the current page index and triggers a data load.
     * @param newPageIndex The index of the page to navigate to.
     */
    private void goToPage(int newPageIndex) {
        if (newPageIndex < 0 || newPageIndex >= totalPages || loading) return;
        pageIndex = newPageIndex;
        loadData();
    }

    /**
     * Resets pagination to the first page and clears the view before loading new data.
     */
    private void loadDataReset() {
        pageIndex = 0;
        listView.getItems().clear();
        updatePaginationControls();
        loadData();
    }

    /**
     * Fetches paginated data from the database based on current filters, sorting, and page index.
     * The loading process runs on a background thread.
     */
    private void loadData() {
        setBusy(true, "Loading results...");
        loading = true;
        Thread loader = new Thread(() -> {
            try {
                // Determine the sort criteria based on user selection
                Sort sort = Sort.by(Sort.Order.desc("aiAnalyzedUnix"), Sort.Order.desc("lastScannedUnix"));

                String sortSel = sortCombo.getValue();
                if ("Oldest AI".equals(sortSel)) {
                    sort = Sort.by(Sort.Order.asc("aiAnalyzedUnix"), Sort.Order.asc("lastScannedUnix"));
                } else if ("Largest".equals(sortSel)) {
                    sort = Sort.by(Sort.Order.desc("sizeBytes"));
                } else if ("Smallest".equals(sortSel)) {
                    sort = Sort.by(Sort.Order.asc("sizeBytes"));
                }

                String statusFilter = filterCombo.getValue();
                String extFilter = extensionCombo.getValue();
                String query = searchField.getText() == null ? "" : searchField.getText().trim();
                PageRequest pageReq = PageRequest.of(pageIndex, pageSize, sort);

                Page<FileRecord> page;

                // Execute search or filtered query
                if (query != null && !query.isBlank()) {
                    page = fileRecordRepository.search(query, pageReq);
                }
                else {
                    boolean isStatusAll = "All".equals(statusFilter) || statusFilter == null;
                    boolean isExtAll = "All Types".equals(extFilter) || extFilter == null;
                    boolean isUnclassified = "Unclassified".equals(statusFilter);

                    if (isStatusAll && isExtAll) {
                        // Retrieve all records that are not flagged as "Ignored"
                        page = fileRecordRepository.findByTypeLabelNotIgnoreCase("Ignored", pageReq);
                    }
                    else if (!isStatusAll && isExtAll) {
                        if (isUnclassified) {
                            page = fileRecordRepository.findByTypeLabelIsNull(pageReq);
                        } else {
                            page = fileRecordRepository.findByTypeLabelIgnoreCase(statusFilter, pageReq);
                        }
                    }
                    else if (isStatusAll && !isExtAll) {
                        page = fileRecordRepository.findByExtIgnoreCase(extFilter, pageReq);
                    }
                    else {
                        // Both filters are active
                        if (isUnclassified) {
                            page = fileRecordRepository.findByTypeLabelIsNullAndExtIgnoreCase(extFilter, pageReq);
                        } else {
                            page = fileRecordRepository.findByTypeLabelIgnoreCaseAndExtIgnoreCase(statusFilter, extFilter, pageReq);
                        }
                    }
                }

                List<FileRecord> items = new ArrayList<>(page.getContent());
                long totalCount = page.getTotalElements();
                int totalPagesCount = page.getTotalPages();

                // Remove file records from the list if the actual file no longer exists on disk
                items.removeIf(fr -> {
                    if (fr.getPath() == null) return true;
                    try { return !Files.exists(Paths.get(fr.getPath())); } catch (Exception e) { return true; }
                });

                // Update the UI on the JavaFX application thread
                Platform.runLater(() -> {
                    listView.getItems().setAll(items);
                    totalItems = totalCount;
                    totalPages = Math.max(1, totalPagesCount);
                    updatePaginationControls();

                    int displayedCount = items.size();
                    int startItem = displayedCount > 0 ? (pageIndex * pageSize) + 1 : 0;
                    int endItem = Math.min((pageIndex * pageSize) + displayedCount, (int) totalItems);

                    String statusText = (statusFilter != null && !"All".equals(statusFilter)) || (extFilter != null && !"All Types".equals(extFilter))
                            ? " (filtered)" : "";

                    setBusy(false, String.format("Showing %d-%d of %d items%s", startItem, endItem, totalItems, statusText));
                    loading = false;
                });

            } catch (Exception ex) {
                Platform.runLater(() -> {
                    setBusy(false, "Load failed: " + ex.getMessage());
                    loading = false;
                });
            }
        }, "load-worker");
        loader.setDaemon(true);
        loader.start();
    }

    /**
     * Updates the text and disabled state of the pagination buttons based on the current page index.
     */
    private void updatePaginationControls() {
        firstPageBtn.setDisable(pageIndex == 0);
        prevPageBtn.setDisable(pageIndex == 0);
        nextPageBtn.setDisable(pageIndex >= totalPages - 1);
        lastPageBtn.setDisable(pageIndex >= totalPages - 1);
        pageLabel.setText(String.format("Page %d of %d", pageIndex + 1, totalPages));
    }

    /**
     * Shows or hides the progress indicator and updates the status message label.
     * @param busy True to show the progress indicator, false to hide it.
     * @param message The status text to display.
     */
    private void setBusy(boolean busy, String message) {
        progress.setVisible(busy);
        statusLabel.setText(message);
    }

    /**
     * Cleans up resources when the view is closed or destroyed.
     */
    public void dispose() {
        stopAutoRefresh();
    }
}
>>>>>>> clean-feature-branch
