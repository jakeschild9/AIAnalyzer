package edu.missouristate.aianalyzer.service.database;

import edu.missouristate.aianalyzer.model.database.FileRecord;
import edu.missouristate.aianalyzer.model.database.ScanQueueItem;
import edu.missouristate.aianalyzer.repository.database.FileRecordRepository;
import edu.missouristate.aianalyzer.repository.database.ScanQueueItemRepository;
import edu.missouristate.aianalyzer.service.photos.FindDuplicatesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;

import static edu.missouristate.aianalyzer.model.FileInterpretation.IMAGE_TYPES;
import static edu.missouristate.aianalyzer.model.FileInterpretation.SUPPORTED_FILE_TYPES;

/*
    This will be the "Consumer" which runs in the background and pulls tasks from the scan_queue
*/
@Slf4j
@Service
@RequiredArgsConstructor
public class FileProcessingService {

    private final ScanQueueItemRepository scanQueueItemRepository;
    private final FileRecordRepository fileRecordRepository;


    private static final int BATCH_SIZE = 50; // How many items to process per run

    /**
     * This method runs on a fixed schedule, acting as our main worker loop.
     * It replaces the `while(true)` loop from the original Main.java.
     */
    @Scheduled(fixedDelay = 5000) // Runs every 5 seconds
    @Transactional
    public void processQueue() {
        long now = Instant.now().getEpochSecond();

        // 1. Fetch a batch of due items from the queue.
        List<ScanQueueItem> items = scanQueueItemRepository.findAllByNotBeforeUnixLessThanEqualOrderByNotBeforeUnix(now, PageRequest.of(0, BATCH_SIZE));

        if (items.isEmpty()) {
            return; // Nothing to do.
        }

        log.info("Processing {} items from the scan queue.", items.size());

        for (ScanQueueItem item : items) {
            try {
                handleFileTask(item.getPath());
                scanQueueItemRepository.delete(item); // Task succeeded, remove from queue.
            } catch (Exception e) {
                log.error("Failed to process file task for path: {}", item.getPath(), e);
                requeueFailedTask(item); // Task failed, requeue for later.
            }
        }
    }

    /**
     * Processes a single file path from the queue. This contains the core logic from QueueWorker.java.
     */
    private void handleFileTask(String pathStr) throws IOException, NoSuchAlgorithmException {
        Path path = Paths.get(pathStr);
        FileRecord fileRecord = fileRecordRepository.findByPath(pathStr)
                .orElse(new FileRecord()); // Create a new record if it doesn't exist.

        String hash;
        fileRecord.setPath(pathStr);
        fileRecord.setParentPath(path.getParent() != null ? path.getParent().toString() : "");
        fileRecord.setLastScannedUnix(Instant.now().getEpochSecond());

        if (!Files.exists(path)) {
            fileRecord.setKind("missing");
            fileRecord.setSizeBytes(0);
        } else {
            BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            fileRecord.setSizeBytes(attrs.size());
            fileRecord.setMtimeUnix(attrs.lastModifiedTime().toMillis() / 1000);
            fileRecord.setCtimeUnix(attrs.creationTime().toMillis() / 1000);

            String ext = getFileExtension(path);
            fileRecord.setExt(ext);
            fileRecord.setKind(detectKindFromExtension(ext)); // Simplified kind detection

            // Calculate content hash (from QueueWorker's handleImageDeep logic)
            if (IMAGE_TYPES.contains(ext.toLowerCase())) {
                hash = String.valueOf(FindDuplicatesService.calculateImageHash(String.valueOf(path)));
            } else {
                hash = calculateSha256(path, 256 * 1024 * 1024); // 256MB limit
            }
            fileRecord.setContentHash(hash);
        }

        fileRecordRepository.save(fileRecord);
    }

    private void requeueFailedTask(ScanQueueItem item) {
        item.setAttempts(item.getAttempts() + 1);
        item.setNotBeforeUnix(Instant.now().getEpochSecond() + 300); // Try again in 5 minutes
        scanQueueItemRepository.save(item);
    }

    // --- Helper methods from the original QueueWorker ---

    private String getFileExtension(Path path) {
        String name = path.getFileName().toString();
        int dotIndex = name.lastIndexOf('.');
        return (dotIndex > 0 && dotIndex < name.length() - 1) ? name.substring(dotIndex + 1).toLowerCase() : "";
    }

    private String detectKindFromExtension(String ext) {
        if (Set.of("jpg","jpeg","png","gif","bmp","tif","tiff","webp","heic").contains(ext)) return "image";
        if (Set.of("mp4","mov","mkv","avi","wmv").contains(ext)) return "video";
        if (Set.of("pdf","doc","docx","xls","xlsx","ppt","pptx","txt","md","csv","json").contains(ext)) return "doc";
        return "other";
    }

    private String calculateSha256(Path path, long maxBytes) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        long bytesToRead = Math.min(Files.size(path), maxBytes);
        long bytesRead = 0;

        try (InputStream is = Files.newInputStream(path);
             DigestInputStream dis = new DigestInputStream(is, md)) {
            byte[] buffer = new byte[8192];
            int read;
            while (bytesRead < bytesToRead && (read = dis.read(buffer, 0, (int) Math.min(buffer.length, bytesToRead - bytesRead))) != -1) {
                bytesRead += read;
            }
        }
        return HexFormat.of().formatHex(md.digest());
    }
}
