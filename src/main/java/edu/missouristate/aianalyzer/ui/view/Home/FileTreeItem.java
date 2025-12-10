package edu.missouristate.aianalyzer.ui.view.Home;

import edu.missouristate.aianalyzer.ui.service.FileSystemService;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

import java.io.File;
<<<<<<< HEAD
=======
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
>>>>>>> clean-feature-branch
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
<<<<<<< HEAD

public class FileTreeItem extends TreeItem<File> {

    // I need to load folders in the background so the UI doesn't freeze.
    // Making one shared thread for all file loading is way more efficient
    // than creating a new thread for every single folder.
    private static final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
        Thread t = new Thread(runnable);
        // This is important. It makes the thread a "daemon," which means the app can close
        // properly even if this thread is still in the middle of scanning a huge folder.
        t.setDaemon(true);
=======
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
>>>>>>> clean-feature-branch
        return t;
    });

    private final FileSystemService fileSystemService;
    private boolean isChildrenLoaded = false;

<<<<<<< HEAD
    public FileTreeItem(File file, FileSystemService fileSystemService) {
        super(file);
        this.fileSystemService = fileSystemService;
    }

    @Override
    public ObservableList<TreeItem<File>> getChildren() {
        // This whole section is the magic for lazy-loading. It only loads the contents
        // of a folder the very first time the user clicks the little expand arrow.
        if (!isChildrenLoaded && !isLeaf()) {
            isChildrenLoaded = true; // Mark it as loaded so we don't do this again.
            super.getChildren().setAll(createLoadingNode()); // Show "Loading..." immediately.
            loadChildrenInBackground(); // Start the real work on the background thread.
=======
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
>>>>>>> clean-feature-branch
        }
        return super.getChildren();
    }

<<<<<<< HEAD
    private void loadChildrenInBackground() {
        // Hand off the slow file-scanning work to our background thread.
        executor.submit(() -> {
            try {
                // --- THIS PART RUNS IN THE BACKGROUND ---
                // It's safe to do slow stuff here.
                List<File> childrenFiles = fileSystemService.getChildrenForPath(getValue());

                // Take the list of files and turn them into `FileTreeItem`s for the tree.
                List<TreeItem<File>> childrenItems = childrenFiles.stream()
                        .map(file -> new FileTreeItem(file, fileSystemService))
                        .collect(Collectors.toList());

                // --- THIS PART RUNS BACK ON THE UI THREAD ---
                // Results have been received, now to update the UI, which must be done on the main JavaFX thread.
                // `Platform.runLater` is how you do that safely.
                Platform.runLater(() -> {
                    super.getChildren().setAll(childrenItems); // Replace "Loading..." with the actual folders/files.
                });

            } catch (Exception e) {
                // If something goes wrong (e.g., access denied to a folder), just print the error
                // and clear the "Loading..." message from the UI.
                e.printStackTrace();
                Platform.runLater(() -> {
                    super.getChildren().clear();
                });
=======
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
>>>>>>> clean-feature-branch
            }
        });
    }

<<<<<<< HEAD
    @Override
    public boolean isLeaf() {
        // A 'leaf' is an item that can't be expanded. In our case, that's a file.
        // A folder is not a leaf.
        return getValue().isFile();
    }

    // A simple helper to create that temporary "Loading..." node.
    private TreeItem<File> createLoadingNode() {
        return new TreeItem<>(new File("Loading..."));
    }
}
=======
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
>>>>>>> clean-feature-branch
