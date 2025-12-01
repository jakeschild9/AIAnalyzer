package edu.missouristate.aianalyzer.ui.view.Home;

import edu.missouristate.aianalyzer.ui.service.FileSystemService;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Custom TreeItem implementation for displaying local file system data.
 * Handles lazy loading of children and background calculation of folder sizes.
 */
public class FileTreeItem extends TreeItem<FileItemModel> {

    // Thread pool dedicated to background tasks like file size calculations and directory listing.
    private static final ExecutorService scannerPool = Executors.newFixedThreadPool(4, r -> {
        Thread t = new Thread(r);
        t.setDaemon(true); // Allows the application to exit even if these threads are running.
        return t;
    });

    private final FileSystemService fileSystemService;
    private boolean isChildrenLoaded = false;

    public FileTreeItem(FileItemModel model, FileSystemService fileSystemService) {
        super(model);
        this.fileSystemService = fileSystemService;

        // If the item is a folder (and not the file system root), begin the size calculation in the background.
        if (model.getFile().isDirectory() && model.getFile().getParent() != null) {
            calculateFolderSize(model);
        }
    }

    @Override
    public ObservableList<TreeItem<FileItemModel>> getChildren() {
        // Implement lazy loading: children are only loaded when the item is expanded for the first time.
        if (!isChildrenLoaded && !isLeaf()) {
            isChildrenLoaded = true;
            // Add a temporary "Loading..." item for immediate user feedback.
            super.getChildren().add(new TreeItem<>(new FileItemModel(new File("Loading..."))));
            loadChildren();
        }
        return super.getChildren();
    }

    @Override
    public boolean isLeaf() {
        // An item is a leaf if it represents a file, otherwise it's a directory.
        return getValue().getFile().isFile();
    }

    // Loads the files and directories within the current directory in a background thread.
    private void loadChildren() {
        scannerPool.submit(() -> {
            try {
                List<File> files = fileSystemService.getChildrenForPath(getValue().getFile());

                if (files == null) {
                    Platform.runLater(() -> super.getChildren().clear());
                    return;
                }

                // 1. Convert File objects to FileTreeItem model wrappers.
                List<FileTreeItem> children = files.stream()
                        .map(f -> new FileTreeItem(new FileItemModel(f), fileSystemService))
                        .collect(Collectors.toList());

                // 2. Calculate the immediate size sum of files only within this directory (non-recursive).
                // This provides faster, partial visual feedback before the deep scan finishes.
                long immediateSize = files.stream()
                        .filter(File::isFile)
                        .mapToLong(File::length)
                        .sum();

                if (immediateSize > 0) {
                    Platform.runLater(() -> {
                        // Only update if the deep size calculation hasn't already completed.
                        if (getValue().getSize() == 0) {
                            getValue().setSize(immediateSize);
                        }
                    });
                }

                Platform.runLater(() -> {
                    // Update the UI with the actual children.
                    super.getChildren().setAll(children);
                    // Trigger the deep recursive size calculation for any child folders.
                    children.stream()
                            .filter(child -> child.getValue().getFile().isDirectory())
                            .forEach(child -> calculateFolderSize(child.getValue()));
                });

            } catch (Exception e) {
                // Clear children on error.
                Platform.runLater(() -> super.getChildren().clear());
            }
        });
    }

    // Recursively calculates the total size of a folder in a background thread.
    private void calculateFolderSize(FileItemModel model) {
        if (model.getFile().isFile()) return;

        scannerPool.submit(() -> {
            try {
                long size = 0;
                Path start = model.getFile().toPath();

                // Perform a deep recursive walk of the directory structure.
                try (Stream<Path> stream = Files.walk(start)) {
                    size = stream.filter(p -> p.toFile().isFile())
                            .mapToLong(p -> p.toFile().length())
                            .sum();
                } catch (IOException ignored) {} // Ignore IO exceptions during walk

                final long finalSize = size;
                // Update the model property on the JavaFX application thread.
                Platform.runLater(() -> model.setSize(finalSize));

            } catch (Exception ignored) {} // Ignore exceptions during thread execution
        });
    }
}