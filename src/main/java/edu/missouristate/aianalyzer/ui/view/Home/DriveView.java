package edu.missouristate.aianalyzer.ui.view.Home;

<<<<<<< HEAD
import edu.missouristate.aianalyzer.ui.service.FileSystemService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
=======
import edu.missouristate.aianalyzer.model.database.FileRecord;
import edu.missouristate.aianalyzer.service.metrics.HomeMetricsService;
import edu.missouristate.aianalyzer.ui.service.FileSystemService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
>>>>>>> clean-feature-branch
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
<<<<<<< HEAD
=======
import java.util.List;
import javafx.scene.Node;
import java.util.Map;
>>>>>>> clean-feature-branch

@Component
public class DriveView extends SplitPane {

    private final FileSystemService fileSystemService;
<<<<<<< HEAD

    @Autowired
    public DriveView(FileSystemService fileSystemService) {
        this.fileSystemService = fileSystemService;

        // Set up the two main panels: the drive list on the left, categories on the right.
        VBox driveTreePanel = createDriveTreePanel();
        VBox categoryPanel = createCategoryPanel();

        this.getItems().addAll(driveTreePanel, categoryPanel);
        this.setDividerPositions(0.40);
        this.getStyleClass().add("drive-split-view");

        HBox.setHgrow(this, Priority.ALWAYS);
        VBox.setVgrow(this, Priority.ALWAYS);
=======
    private final HomeMetricsService homeMetricsService;

    private final StackPane rightPanelStack = new StackPane();
    private ScrollPane categoryGridPane;
    private VBox fileListPane;

    private CategoryCard imagesCard, videosCard, docsCard, archivesCard, codeCard, execCard, audioCard, othersCard;
    private CategoryCard duplicateImagesCard;
    private TreeTableView<FileItemModel> treeTable;

    private Timeline autoRefreshTimeline;
    private volatile boolean loading = false;

    @Autowired
    public DriveView(FileSystemService fileSystemService, HomeMetricsService homeMetricsService) {
        this.fileSystemService = fileSystemService;
        this.homeMetricsService = homeMetricsService;

        VBox leftPanel = createDriveTreePanel();
        initRightPanel();

        this.getItems().addAll(leftPanel, rightPanelStack);
        this.setDividerPositions(0.35);

        // Apply CSS class for the split view container
        this.getStyleClass().add("drive-split-view");
        this.setPadding(Insets.EMPTY);

        // Start/stop auto-refresh and data loading based on view visibility
        this.visibleProperty().addListener((obs, wasVisible, isVisible) -> {
            if (isVisible) { refreshCategoryData(); startAutoRefresh(); } else { stopAutoRefresh(); }
        });
        this.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null && isVisible()) { refreshCategoryData(); startAutoRefresh(); } else { stopAutoRefresh(); }
        });

        refreshCategoryData();
        startAutoRefresh();
>>>>>>> clean-feature-branch
    }

    private VBox createDriveTreePanel() {
        VBox panel = new VBox(10);
<<<<<<< HEAD
        panel.setAlignment(Pos.TOP_LEFT);
        panel.getStyleClass().add("drive-tree-panel");

        Label header = new Label("System Drives");
        header.getStyleClass().add("header-label");

        // Set up the TreeView. It's now generic and works directly with <File> objects.
        TreeView<File> treeView = new TreeView<>();
        treeView.getStyleClass().add("tree-view");
        VBox.setVgrow(treeView, Priority.ALWAYS);

        // We need a root for the tree that isn't a real drive, so I'll make a fake 'My Computer' item.
        File virtualRootFile = new File("My Computer");
        TreeItem<File> rootItem = new TreeItem<>(virtualRootFile);
        rootItem.setExpanded(true);
        treeView.setRoot(rootItem);
        treeView.setShowRoot(true);

        // Loop through all the system drives and add them under 'My Computer'.
        // I'm using my custom FileTreeItem so it can load subfolders on its own.
        for (File drive : File.listRoots()) {
            FileTreeItem driveItem = new FileTreeItem(drive, fileSystemService);
            rootItem.getChildren().add(driveItem);
        }

        // This is the important part. By default, the TreeView doesn't know how to display a `File` object.
        // We have to tell it exactly what text to show for each item in the tree.
        treeView.setCellFactory(tv -> new TreeCell<>() {
            @Override
            protected void updateItem(File file, boolean empty) {
                super.updateItem(file, empty);
                if (empty || file == null) {
                    setText(null);
                } else {
                    // Handle my fake root and the 'Loading...' placeholder text.
                    if (file.getPath().equals("My Computer") || file.getPath().equals("Loading...")) {
                        setText(file.getPath());
                    } else {
                        // For actual drives and folders, show a clean path and, if it's a drive, the free space.
                        long totalSpace = file.getTotalSpace();

                        // For root drives, getName() is empty, so instead getPath() (e.g., "C:\").
                        // For all other folders/files, getName() (e.g., "GamingRoot").
                        String displayName = file.getName().isEmpty() ? file.getPath() : file.getName();

                        String textToShow = displayName;

                        // Only show free space if the file is a root drive (which has no parent).
                        if (file.getParentFile() == null) {
                            textToShow = String.format("%s (%.1f GB Free)",
                                    displayName, file.getUsableSpace() / 1_000_000_000.0);
                        }

                        setText(textToShow);
                    }
=======
        panel.setPadding(new Insets(10));

        // Apply CSS class for the left panel container
        panel.getStyleClass().add("drive-pane-left");

        Label header = new Label("System Storage");
        // Apply CSS class for the header label
        header.getStyleClass().add("drive-header-label");

        treeTable = new TreeTableView<>();
        treeTable.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY);

        // Apply CSS class for the tree table view
        treeTable.getStyleClass().add("tree-table-view");

        VBox.setVgrow(treeTable, Priority.ALWAYS);

        // --- Column 1: Name & Bar Visual ---
        TreeTableColumn<FileItemModel, FileItemModel> nameCol = new TreeTableColumn<>("Name");
        nameCol.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getValue()));

        nameCol.setCellFactory(col -> new TreeTableCell<>() {
            private final StackPane stack = new StackPane();
            private final Rectangle bar = new Rectangle();
            private final Label label = new Label();
            private final HBox textBox = new HBox(5);
            private final Label icon = new Label();
            private ChangeListener<Number> sizeListener;
            private ChangeListener<Number> widthListener;

            {
                bar.setHeight(28);
                bar.setArcWidth(0);
                bar.setArcHeight(0);

                // Use CSS class for the size bar visualization
                bar.getStyleClass().add("custom-drive-tree-bar");

                // Use CSS class for the text color
                label.getStyleClass().add("custom-drive-tree-label");

                icon.setStyle("-fx-font-size: 14px;");

                textBox.getChildren().addAll(icon, label);
                textBox.setAlignment(Pos.CENTER_LEFT);
                textBox.setPadding(new Insets(0, 0, 0, 8));

                stack.getChildren().addAll(bar, textBox);
                stack.setAlignment(Pos.CENTER_LEFT);
                stack.setMaxWidth(Double.MAX_VALUE);
            }

            @Override
            protected void updateItem(FileItemModel item, boolean empty) {
                super.updateItem(item, empty);

                // Remove existing listeners to avoid memory leaks or duplicate updates
                if (getItem() != null && sizeListener != null) getItem().sizeProperty().removeListener(sizeListener);
                if (widthListener != null) widthProperty().removeListener(widthListener);

                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    label.setText(item.getName());
                    // Set appropriate icon based on file type
                    if (item.getFile().getParent() == null) icon.setText("\uD83D\uDCBE");
                    else if (item.getFile().isDirectory()) icon.setText("\uD83D\uDCC1");
                    else icon.setText("\uD83D\uDCC4");

                    // Logic to calculate the width of the size bar
                    Runnable updateBar = () -> {
                        double currentCellWidth = getWidth();
                        if (currentCellWidth <= 0) return;

                        long mySize = item.getSize();
                        long parentSize = 0;

                        TreeTableRow<FileItemModel> row = getTreeTableRow();
                        if (row != null && row.getTreeItem() != null) {
                            TreeItem<FileItemModel> parentItem = row.getTreeItem().getParent();
                            if (parentItem != null && parentItem.getValue() != null) {
                                // Use parent size for ratio
                                parentSize = parentItem.getValue().getSize();
                            } else {
                                // Use total disk capacity for root drives
                                parentSize = item.getTotalCapacity();
                            }
                        }

                        double pct = 0;
                        if (parentSize > 0) {
                            pct = (double) mySize / parentSize;
                        }

                        // Clamp the percentage between 0 and 1
                        if (pct > 1.0) pct = 1.0;
                        if (pct < 0) pct = 0;

                        bar.setWidth(currentCellWidth * pct);
                    };

                    // Add listeners for size change and cell width change
                    sizeListener = (obs, o, n) -> updateBar.run();
                    item.sizeProperty().addListener(sizeListener);

                    widthListener = (obs, o, n) -> updateBar.run();
                    widthProperty().addListener(widthListener);

                    updateBar.run();
                    setGraphic(stack);
>>>>>>> clean-feature-branch
                }
            }
        });

<<<<<<< HEAD
        panel.getChildren().addAll(header, treeView);
        return panel;
    }


    // RIGHT panel above
    // LEFT panel below

    // Builds the panel on the right that shows all the file category cards.
    private VBox createCategoryPanel() {
        VBox panel = new VBox(10);
        panel.setAlignment(Pos.TOP_LEFT);
        panel.setPadding(new Insets(15));

        Label header = new Label("File Categories");
        header.getStyleClass().add("header-label");

        // A TilePane is perfect for the category cards, it'll wrap them nicely as the window resizes.
        TilePane categoryGrid = new TilePane(10, 10);
        categoryGrid.setPadding(new Insets(10, 0, 0, 0));
        VBox.setVgrow(categoryGrid, Priority.ALWAYS);

        // For now, just add some placeholder cards to see how it looks.
        categoryGrid.getChildren().addAll(
                createCategoryCard("Photos", "24,561 files"),
                createCategoryCard("Videos", "283 GB"),
                createCategoryCard("Archives", "1.2 GB of ZIP/RAR"),
                createCategoryCard("Documents", "345 documents"),
                createCategoryCard("Duplicated", "4.1 GB waste"),
                createCategoryCard("Unused", "Oldest file: 2012"),
                createCategoryCard("System", "Ready for cleanup"),
                createCategoryCard("Games", "25 games found"),
                createCategoryCard("Temp/Cache", "Clearable Cache"),
                createCategoryCard("Unclassified", "Needs AI review")
        );

        panel.getChildren().addAll(header, categoryGrid);
        return panel;
    }

    // Helper function to make one of those category cards.
    private Node createCategoryCard(String title, String subtitle) {
        VBox cardContent = new VBox(5);
        cardContent.setAlignment(Pos.TOP_LEFT);
        cardContent.setPadding(new Insets(15));
        VBox.setVgrow(cardContent, Priority.ALWAYS);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("card-title");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("card-subtitle");
        VBox.setVgrow(subtitleLabel, Priority.ALWAYS);

        cardContent.getChildren().addAll(titleLabel, subtitleLabel);

        // The VBox `card` is the main container with all the styling.
        VBox card = new VBox(cardContent);
        card.setPrefSize(180, 100);
        card.getStyleClass().add("category-card");

        // This lets me use the card's title for CSS. I'm converting a title like "Temp/Cache"
        // into a safe class name like "category-temp-cache" so I can style each card
        // type differently in the CSS file.
        String styleClassName = "category-" + title.toLowerCase()
                .replace(" ", "-")
                .replace("/", "-");
        card.getStyleClass().add(styleClassName);

        return card;
    }
}
=======
        // --- Column 2: Size ---
        TreeTableColumn<FileItemModel, Number> sizeCol = new TreeTableColumn<>("Size");
        sizeCol.setCellValueFactory(param -> param.getValue().getValue().sizeProperty());
        sizeCol.setCellFactory(col -> new TreeTableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatBytes(item.longValue()));
                    this.getStyleClass().add("custom-drive-tree-size");
                }
            }
        });

        // Configure column dimensions and sorting
        nameCol.setPrefWidth(300);
        sizeCol.setPrefWidth(90);
        sizeCol.setMaxWidth(90);
        sizeCol.setResizable(false);

        treeTable.getColumns().addAll(nameCol, sizeCol);

        // Initial sort order
        sizeCol.setSortType(TreeTableColumn.SortType.DESCENDING);
        treeTable.getSortOrder().add(sizeCol);

        // Setup the invisible root item for the tree view
        File virtualRoot = new File("This PC");
        FileItemModel rootModel = new FileItemModel(virtualRoot);
        TreeItem<FileItemModel> rootItem = new TreeItem<>(rootModel);
        rootItem.setExpanded(true);
        treeTable.setRoot(rootItem);
        treeTable.setShowRoot(false);

        // Populate the tree with system drives/roots
        for (File drive : File.listRoots()) {
            FileItemModel driveModel = new FileItemModel(drive);
            // Set initial size as occupied space (total - free)
            driveModel.setSize(drive.getTotalSpace() - drive.getFreeSpace());
            FileTreeItem driveItem = new FileTreeItem(driveModel, fileSystemService);
            rootItem.getChildren().add(driveItem);
        }

        panel.getChildren().addAll(header, treeTable);
        return panel;
    }

    // Utility function to format bytes into human-readable strings.
    private String formatBytes(long bytes) {
        if (bytes <= 0) return "0 B";
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    // --- RIGHT PANEL LOGIC ---
    private void initRightPanel() {
        categoryGridPane = createCategoryGrid();
        fileListPane = new VBox();
        // Set transparent background
        fileListPane.setStyle("-fx-background-color: transparent;");
        fileListPane.setVisible(false);
        rightPanelStack.getChildren().addAll(categoryGridPane, fileListPane);
        rightPanelStack.setAlignment(Pos.TOP_LEFT);
    }

    // Creates the scrollable grid view containing clickable file category cards.
    private ScrollPane createCategoryGrid() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: transparent;"); // Set transparent background

        Label header = new Label("File Categories");
        // Reuse header CSS class
        header.getStyleClass().add("drive-header-label");

        // Initialize category cards
        imagesCard   = new CategoryCard("Images", "#4CAF50", () -> showCategory("Images"));
        videosCard   = new CategoryCard("Videos", "#2196F3", () -> showCategory("Videos"));
        docsCard     = new CategoryCard("Documents", "#FF9800", () -> showCategory("Documents"));
        archivesCard = new CategoryCard("Archives", "#9C27B0", () -> showCategory("Archives"));
        codeCard     = new CategoryCard("Code", "#00BCD4", () -> showCategory("Code"));
        execCard     = new CategoryCard("Executables", "#F44336", () -> showCategory("Executables"));
        audioCard    = new CategoryCard("Audio", "#E91E63", () -> showCategory("Audio"));
        othersCard   = new CategoryCard("Others", "#757575", () -> showCategory("Others"));
        duplicateImagesCard = new CategoryCard("Duplicate Images", "#3F51B5", () -> showCategory("Duplicate Images"));

        // Use TilePane for flexible grid layout
        TilePane grid = new TilePane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPrefColumns(3);

        grid.getChildren().addAll(imagesCard, videosCard, audioCard, docsCard, archivesCard, codeCard, execCard, othersCard, duplicateImagesCard);
        content.getChildren().addAll(header, grid);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        // Set transparent background
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        return scroll;
    }

    // Builds the list view when a category card is clicked.
    private void buildFileListView(String category, List<FileRecord> files) {
        fileListPane.getChildren().clear();
        fileListPane.setPadding(new Insets(20));
        fileListPane.setSpacing(10);

        Button backBtn = new Button("< Back");
        // Use navigation button CSS class
        backBtn.getStyleClass().add("nav-button");

        backBtn.setOnAction(e -> {
            fileListPane.setVisible(false);
            categoryGridPane.setVisible(true);
            // Resume refreshing data when returning to the grid view
            startAutoRefresh();
            refreshCategoryData();
        });

        Label title = new Label(category + " (" + files.size() + ")");
        // Use header CSS class
        title.getStyleClass().add("drive-header-label");

        HBox topBar = new HBox(15, backBtn, title);
        topBar.setAlignment(Pos.CENTER_LEFT);

        // For all normal categories, show a flat list as before.
        if (!"Duplicate Images".equals(category)) {
            ListView<FileRecord> list = new ListView<>();
            list.getItems().addAll(files);
            list.setCellFactory(lv -> new ExplorerFileCell());
            // Use list view CSS class
            list.getStyleClass().add("custom-list-view");

            VBox.setVgrow(list, Priority.ALWAYS);

            if (files.isEmpty()) {
                Label placeholder = new Label("No files found in this category.");
                // Use CSS class for placeholder text
                placeholder.getStyleClass().add("custom-list-placeholder");

                list.setPlaceholder(placeholder);
            }

            fileListPane.getChildren().addAll(topBar, list);
        } else {
            // Special layout for duplicate images: group by content hash.
            Node groupsView = createDuplicateGroupsView(files);
            VBox.setVgrow(groupsView, Priority.ALWAYS);
            fileListPane.getChildren().addAll(topBar, groupsView);
        }

        categoryGridPane.setVisible(false);
        fileListPane.setVisible(true);
    }

    /**
     * Builds a grouped view for duplicate images, where each group represents
     * all files sharing the same contentHash.
     */
    private Node createDuplicateGroupsView(List<FileRecord> files) {
        VBox groupsContainer = new VBox(12);
        groupsContainer.setFillWidth(true);

        if (files == null || files.isEmpty()) {
            Label placeholder = new Label("No duplicate images found.");
            placeholder.getStyleClass().add("custom-list-placeholder");
            groupsContainer.getChildren().add(placeholder);
            return groupsContainer;
        }

        // Group by contentHash (ignore null or sentinel values)
        Map<String, List<FileRecord>> groups = files.stream()
                .filter(fr -> fr.getContentHash() != null)
                .filter(fr -> !fr.getContentHash().equalsIgnoreCase("ERROR")
                        && !fr.getContentHash().equalsIgnoreCase("QUARANTINED"))
                .collect(java.util.stream.Collectors.groupingBy(FileRecord::getContentHash));

        // Only keep groups with more than one file (true duplicates)
        List<Map.Entry<String, List<FileRecord>>> duplicateGroups = groups.entrySet().stream()
                .filter(e -> e.getValue().size() > 1)
                // Sort groups by total size descending (largest duplicate sets first)
                .sorted((e1, e2) -> {
                    long s1 = e1.getValue().stream().mapToLong(FileRecord::getSizeBytes).sum();
                    long s2 = e2.getValue().stream().mapToLong(FileRecord::getSizeBytes).sum();
                    return Long.compare(s2, s1);
                })
                .toList();

        if (duplicateGroups.isEmpty()) {
            Label placeholder = new Label("No duplicate image groups found.");
            placeholder.getStyleClass().add("custom-list-placeholder");
            groupsContainer.getChildren().add(placeholder);
            return groupsContainer;
        }

        int groupIndex = 1;
        for (Map.Entry<String, List<FileRecord>> entry : duplicateGroups) {
            String hash = entry.getKey();
            List<FileRecord> groupFiles = entry.getValue();

            long totalSize = groupFiles.stream().mapToLong(FileRecord::getSizeBytes).sum();

            String headerText = String.format(
                    "Group %d • %d files • %s total",
                    groupIndex++,
                    groupFiles.size(),
                    formatBytes(totalSize)
            );

            TitledPane pane = new TitledPane();
            pane.setText(headerText);

            // Inside each group, reuse the ExplorerFileCell so the user sees familiar UI.
            ListView<FileRecord> groupList = new ListView<>();
            groupList.getItems().addAll(groupFiles);
            groupList.setCellFactory(lv -> new ExplorerFileCell());
            groupList.getStyleClass().add("custom-list-view");
            groupList.setPrefHeight(Math.min(250, 50 + groupFiles.size() * 40.0)); // basic sizing

            pane.setContent(groupList);
            pane.setExpanded(false); // start collapsed for large numbers of groups

            groupsContainer.getChildren().add(pane);
        }

        ScrollPane scrollPane = new ScrollPane(groupsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        return scrollPane;
    }


    // Initiates the loading of files for a specific category in a background thread.
    private void showCategory(String category) {
        // Stop background refresh while viewing the list
        stopAutoRefresh();
        new Thread(() -> {
            List<FileRecord> files = homeMetricsService.getFilesByCategory(category);
            Platform.runLater(() -> buildFileListView(category, files));
        }).start();
    }

    // Fetches aggregated file statistics for all categories.
    public void refreshCategoryData() {
        if (loading) return;
        loading = true;
        new Thread(() -> {
            try {
                var stats = homeMetricsService.getCategoryStats();
                // Calculate the total number of files across all categories
                long totalFiles = stats.values().stream().mapToLong(s -> s.count).sum();

                // Update each category card on the JavaFX thread
                Platform.runLater(() -> {
                    updateCard(imagesCard, stats.get("Images"), totalFiles);
                    updateCard(videosCard, stats.get("Videos"), totalFiles);
                    updateCard(docsCard, stats.get("Documents"), totalFiles);
                    updateCard(archivesCard, stats.get("Archives"), totalFiles);
                    updateCard(codeCard, stats.get("Code"), totalFiles);
                    updateCard(execCard, stats.get("Executables"), totalFiles);
                    updateCard(audioCard, stats.get("Audio"), totalFiles);
                    updateCard(othersCard, stats.get("Others"), totalFiles);
                    updateCard(duplicateImagesCard, stats.get("Duplicate Images"), totalFiles);
                });
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                loading = false;
            }
        }).start();
    }

    // Helper method to safely update a CategoryCard's display.
    private void updateCard(CategoryCard card, HomeMetricsService.CategoryStats stats, long totalFiles) {
        if (card != null) {
            long count = (stats != null) ? stats.count : 0;
            long size = (stats != null) ? stats.sizeBytes : 0;
            card.update(count, size, totalFiles);
        }
    }

    // Starts the timeline for automatic data refresh every 2 seconds.
    private void startAutoRefresh() {
        if (autoRefreshTimeline != null) autoRefreshTimeline.stop();
        autoRefreshTimeline = new Timeline(new KeyFrame(Duration.seconds(2), event -> {
            // Only refresh if the view is visible, showing the grid, and not currently loading
            if (isVisible() && categoryGridPane.isVisible() && !loading) {
                refreshCategoryData();
                // Re-sort and refresh the tree table view for updated sizes
                if (treeTable != null) {
                    treeTable.sort();
                    treeTable.refresh();
                }
            }
        }));
        autoRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        autoRefreshTimeline.play();
    }

    // Stops the timeline, disabling automatic data refresh.
    private void stopAutoRefresh() {
        if (autoRefreshTimeline != null) autoRefreshTimeline.stop();
    }
}
>>>>>>> clean-feature-branch
