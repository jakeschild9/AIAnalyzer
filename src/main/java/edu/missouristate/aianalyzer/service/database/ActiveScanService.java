package edu.missouristate.aianalyzer.service.database;

import edu.missouristate.aianalyzer.model.database.ScanQueueItem;
import edu.missouristate.aianalyzer.repository.database.ScanQueueItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/*
 This will be the "Producer" which finds files on disk and adds them to the scan_queue table for processing
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ActiveScanService {

    private final ScanQueueItemRepository scanQueueItemRepository;

    // --- Configuration (from old FileScanner and PassiveScanner) ---
    private static final Set<String> EXCLUDED_DIRS = Set.of("$recycle.bin", "node_modules", ".git");
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "bmp", "tif", "tiff", "webp", "heic", // Images
            "mp4", "mov", "mkv", "avi", "wmv", // Videos
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "md", "csv", "json" // Docs
    );

    /**
     * Performs a high-speed, multi-threaded scan of the given root directories.
     * This is the replacement for the old ActiveScanner.
     *
     * @param roots A list of starting directories to scan.
     */
    public void performActiveScan(List<Path> roots) {
        log.info("Starting active scan on roots: {}", roots);
        int threads = Math.max(2, Runtime.getRuntime().availableProcessors());
        ExecutorService executor = Executors.newFixedThreadPool(threads);

        for (final Path root : roots) {
            executor.submit(() -> {
                try {
                    Files.walkFileTree(root, new SimpleFileVisitor<>() {
                        @Override
                        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                            String dirName = dir.getFileName().toString().toLowerCase();
                            if (EXCLUDED_DIRS.contains(dirName)) {
                                return FileVisitResult.SKIP_SUBTREE;
                            }
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                            if (attrs.isRegularFile() && isFileTypeAllowed(file)) {
                                enqueueFileTask(file, "file");
                            }
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFileFailed(Path file, IOException exc) {
                            log.warn("Failed to visit file: {}", file, exc);
                            return FileVisitResult.CONTINUE;
                        }
                    });
                } catch (IOException e) {
                    log.error("Error during active scan of root: {}", root, e);
                }
            });
        }

        try {
            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.HOURS); // Wait for scan to complete
        } catch (InterruptedException e) {
            log.error("Active scan was interrupted.", e);
            Thread.currentThread().interrupt();
        }
        log.info("Active scan finished.");
    }


    /**
     * Starts a background thread that continuously watches directories for real-time changes.
     * This is the replacement for the old PassiveScanner.
     */
    public void startPassiveWatcher(List<Path> roots) {
        log.info("Starting passive watcher on roots: {}", roots);
        try {
            WatchService watchService = FileSystems.getDefault().newWatchService();
            for (Path root : roots) {
                Files.walkFileTree(root, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        String dirName = dir.getFileName().toString().toLowerCase();
                        if (EXCLUDED_DIRS.contains(dirName)) {
                            return FileVisitResult.SKIP_SUBTREE;
                        }
                        dir.register(watchService,
                                StandardWatchEventKinds.ENTRY_CREATE,
                                StandardWatchEventKinds.ENTRY_MODIFY,
                                StandardWatchEventKinds.ENTRY_DELETE);
                        return FileVisitResult.CONTINUE;
                    }
                });
            }

            // Start a new thread to handle watch events
            Thread watcherThread = new Thread(() -> {
                WatchKey key;
                try {
                    while ((key = watchService.take()) != null && !Thread.currentThread().isInterrupted()) {
                        Path dir = (Path) key.watchable();
                        for (WatchEvent<?> event : key.pollEvents()) {
                            Path filePath = dir.resolve((Path) event.context());
                            if (isFileTypeAllowed(filePath)) {
                                log.debug("Passive watcher detected change: {} on {}", event.kind(), filePath);
                                enqueueFileTask(filePath, "file");
                            }
                        }
                        key.reset();
                    }
                } catch (InterruptedException e) {
                    log.info("Passive watcher thread interrupted.");
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    log.error("Error in passive watcher", e);
                }
            }, "Passive-File-Watcher");

            watcherThread.setDaemon(true); // This allows the app to exit even if this thread is running
            watcherThread.start();

        } catch (IOException e) {
            log.error("Failed to start passive watcher.", e);
        }
    }


    // --- Helper Methods ---

    /**
     * Creates a new ScanQueueItem and saves it to the database.
     * This is the modern replacement for the manual SQL inserts.
     */
    private void enqueueFileTask(Path file, String kind) {
        try {
            ScanQueueItem item = new ScanQueueItem();
            item.setPath(file.toAbsolutePath().toString());
            item.setKind(ScanQueueItem.Kind.ACTIVE_AI);
            item.setNotBeforeUnix(Instant.now().getEpochSecond());
            item.setAttempts(0);
            scanQueueItemRepository.save(item);
        } catch (Exception e) {
            // This might happen if there's a unique constraint violation, which is okay.
            log.trace("Could not enqueue file task for {}: {}", file, e.getMessage());
        }
    }

    /**
     * Checks if a file's extension is in our list of allowed types.
     */
    private boolean isFileTypeAllowed(Path file) {
        String fileName = file.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            String extension = fileName.substring(dotIndex + 1).toLowerCase();
            return ALLOWED_EXTENSIONS.contains(extension);
        }
        // Allow files with no extension (e.g., text files)
        return dotIndex == -1;
    }

}
