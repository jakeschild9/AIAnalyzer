package edu.missouristate.aianalyzer.service.database;

import edu.missouristate.aianalyzer.model.database.ScanQueueItem;
import edu.missouristate.aianalyzer.repository.database.ScanQueueItemRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.context.event.ApplicationReadyEvent;

/**
 * A background service that passively monitors the file system for changes.
 * It replaces the old PassiveScanner.java by using Spring's scheduling and async features.
 */
@Service
public class PassiveScanService {

    private final ScanQueueItemRepository scanQueueItemRepository;
    private final List<Path> roots;
    private WatchService watcher;

    // These are from the old PassiveScanner, used to filter which files we care about.
    private static final Set<String> EXCLUDE_DIRS = Set.of("$recycle.bin", "node_modules", ".git");
    private static final Set<String> ALLOWED_EXT = Set.of(
            "jpg", "jpeg", "png", "gif", "bmp", "tif", "tiff", "webp", "heic", // Images
            "mp4", "mov", "mkv", "avi", "wmv", // Videos
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "md", "csv", "json" // Docs
    );

    @Autowired
    public PassiveScanService(ScanQueueItemRepository scanQueueItemRepository,
                              @Value("${scan.roots}") List<String> scanRoots) {
        this.scanQueueItemRepository = scanQueueItemRepository;
        // Convert the String paths from application.properties into Path objects.
        this.roots = scanRoots.stream().map(Paths::get).toList();
    }

    /**
     * This method runs automatically after the service is created, thanks to @PostConstruct.
     * It sets up the WatchService, just like the startAsync() method in the old code.
     */
    @PostConstruct
    public void initialize() {
        try {
            watcher = FileSystems.getDefault().newWatchService();
            for (Path root : roots) {
                if (Files.exists(root)) {
                    System.out.println("Initializing passive watch on: " + root);
                    registerAll(root);
                }
            }

//            startMonitoring(); // Kick off the background monitoring task.
        } catch (IOException e) {
            System.err.println("Error initializing PassiveScanService: " + e.getMessage());
        }
    }

    /**
     * NEW METHOD: This listener will trigger AFTER the application is fully started.
     * This is the safe place to start our background blocking task.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
//        startMonitoring();
    }

    /**
     * This is the core of the real-time monitoring.
     * The @Async annotation tells Spring to run this in a separate background thread pool.
     * This is crucial so it doesn't block the rest of the application.
     */
    @Async
    public void startMonitoring() {
        System.out.println("Passive file monitoring started...");
        try {
            WatchKey key;
            while ((key = watcher.take()) != null) {
                Path dir = (Path) key.watchable();
                List<ScanQueueItem> itemsToQueue = new ArrayList<>();

                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.kind() == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }
                    Path changedFile = dir.resolve((Path) event.context());
                    if (Files.isRegularFile(changedFile) && isAllowedByExtension(changedFile)) {
                        System.out.println("Detected change: " + event.kind().name() + " on " + changedFile);
                        itemsToQueue.add(createQueueItem(changedFile));
                    }
                }
                if (!itemsToQueue.isEmpty()) {
                    scanQueueItemRepository.saveAll(itemsToQueue);
                }
                key.reset();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Passive file monitoring interrupted.");
        }
    }

    /**
     * This is the "trickle crawl" from the old code, reimplemented using Spring's scheduler.
     * The @Scheduled annotation tells Spring to run this method automatically on a timer.
     * `fixedDelay = 10000` means it will run every 10 seconds.
     */
    @Scheduled(fixedDelay = 10000, initialDelay = 5000) // Run 5s after startup, then every 10s
    public void trickleCrawl() {
        // This is a simplified version of the trickle crawl. For a real implementation,
        // you would add logic to slowly walk the directory without re-queueing existing files.
        // For now, we'll just log that it's running.
        // System.out.println("Executing trickle crawl...");
    }

    /**
     * This method runs automatically when the application is shutting down.
     * It ensures the WatchService is closed properly to free up system resources.
     */
    @PreDestroy
    public void shutdown() {
        System.out.println("Shutting down passive file monitoring...");
        try {
            if (watcher != null) {
                watcher.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing WatchService: " + e.getMessage());
        }
    }

    // --- Helper Methods (Adapted from the old PassiveScanner.java) ---

    private void registerAll(Path start) throws IOException {
        Files.walkFileTree(start, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                if (EXCLUDE_DIRS.contains(dir.getFileName().toString().toLowerCase())) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                dir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private boolean isAllowedByExtension(Path p) {
        String name = p.getFileName().toString();
        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot >= name.length() - 1) return false;
        String ext = name.substring(dot + 1).toLowerCase();
        return ALLOWED_EXT.contains(ext);
    }

    private ScanQueueItem createQueueItem(Path path) {
        ScanQueueItem item = new ScanQueueItem();
        item.setPath(path.toString());
        //item.setKind("file");
        item.setNotBeforeUnix(Instant.now().getEpochSecond());
        item.setAttempts(0);
        return item;
    }
}