package edu.missouristate.aianalyzer.service.database;

import edu.missouristate.aianalyzer.model.database.FileRecord;
import edu.missouristate.aianalyzer.model.database.ScanQueueItem;
import edu.missouristate.aianalyzer.repository.database.FileRecordRepository;
import edu.missouristate.aianalyzer.repository.database.ScanQueueItemRepository;
<<<<<<< HEAD
=======
import edu.missouristate.aianalyzer.service.ai.ProcessFileService;
>>>>>>> clean-feature-branch
import edu.missouristate.aianalyzer.service.ai.ScanForVirusService;
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
<<<<<<< HEAD
=======
import java.io.*;
import java.nio.file.*;
>>>>>>> clean-feature-branch

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
<<<<<<< HEAD

    private static final int BATCH_SIZE = 50;                // How many items to process per run
    private static final int BASE_RETRY_DELAY_SECONDS = 300; // 5 minutes
    private static final int MAX_ATTEMPTS = 5;
=======
    private final ProcessFileService processFileService;
    private final LabelService labelService;


    private static final int BATCH_SIZE = 50; // How many items to process per run
>>>>>>> clean-feature-branch

    /**
     * This method runs on a fixed schedule, acting as our main worker loop.
     * It replaces the `while(true)` loop from the original Main.java.
     */
    @Scheduled(fixedDelay = 5000) // Runs every 5 seconds
<<<<<<< HEAD
    @Transactional
    public void processQueue() {
        long now = Instant.now().getEpochSecond();

        // 1. Fetch a batch of due items from the queue, ordered by fewest attempts, then oldest due time.
        List<ScanQueueItem> items =
                scanQueueItemRepository
                        .findAllByNotBeforeUnixLessThanEqualOrderByAttemptsAscNotBeforeUnixAsc(
                                now,
                                PageRequest.of(0, BATCH_SIZE)
                        );
=======
    public void processQueue() {
        long now = Instant.now().getEpochSecond();

        // 1. Fetch a batch of due items from the queue.
        List<ScanQueueItem> items = scanQueueItemRepository.findAllByNotBeforeUnixLessThanEqualOrderByNotBeforeUnix(now, PageRequest.of(0, BATCH_SIZE));
>>>>>>> clean-feature-branch

        if (items.isEmpty()) {
            return; // Nothing to do.
        }

        log.info("Processing {} items from the scan queue.", items.size());

        for (ScanQueueItem item : items) {
            try {
<<<<<<< HEAD
                handleFileTask(item.getPath());
                scanQueueItemRepository.delete(item); // Task succeeded, remove from queue.
            } catch (Exception e) {
                log.error("Failed to process file task for path: {}", item.getPath(), e);
                requeueFailedTask(item); // Task failed, requeue for later with backoff.
=======
                handleFileTaskTransactional(item); // Process each item in its own transaction
            } catch (Exception e) {
                log.error("Failed to process file task for path: {}", item.getPath(), e);
                requeueFailedTask(item); // Task failed, requeue for later.
>>>>>>> clean-feature-branch
            }
        }
    }

    /**
<<<<<<< HEAD
     * Processes a single file path from the queue. This contains the core logic from QueueWorker.java.
     *
     * Now also performs a virus scan using ScanForVirusService.scanFileWithClam(...)
     * for supported file types before doing heavier processing.
     */
    private void handleFileTask(String pathStr)
            throws IOException, NoSuchAlgorithmException, InterruptedException {

=======
     * Processes a single file in its own transaction to avoid long-running locks.
     * MUST be public (or protected) for Spring's @Transactional proxy to work.
     */
    @Transactional
    public void handleFileTaskTransactional(ScanQueueItem item) throws IOException, NoSuchAlgorithmException {
        handleFileTask(item.getPath());
        scanQueueItemRepository.delete(item); // Task succeeded, remove from queue.
    }

    /**
     * Processes a single file path from the queue. This contains the core logic from QueueWorker.java.
     */
    private void handleFileTask(String pathStr) throws IOException, NoSuchAlgorithmException {
>>>>>>> clean-feature-branch
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
            fileRecordRepository.save(fileRecord);
            return;
        }

<<<<<<< HEAD
        // Determine extension early so we can use it for both virus scanning and type detection.
        String ext = getFileExtension(path);

        // --- Virus scan (passive/background) for supported file types ---
        if (SUPPORTED_FILE_TYPES.contains(ext.toLowerCase())) {
            boolean virusFound = ScanForVirusService.scanFileWithClam(path);
            if (virusFound) {
                // Mark as infected and skip further expensive processing.
                fileRecord.setKind("infected");
                fileRecord.setSizeBytes(Files.size(path));
                fileRecordRepository.save(fileRecord);
                return;
            }
        }

        // --- Normal metadata + hashing logic ---
=======
>>>>>>> clean-feature-branch
        BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        fileRecord.setSizeBytes(attrs.size());
        fileRecord.setMtimeUnix(attrs.lastModifiedTime().toMillis() / 1000);
        fileRecord.setCtimeUnix(attrs.creationTime().toMillis() / 1000);

<<<<<<< HEAD
        fileRecord.setExt(ext);
        fileRecord.setKind(detectKindFromExtension(ext)); // Simplified kind detection

        // Calculate content hash (from QueueWorker's handleImageDeep logic)
        if (IMAGE_TYPES.contains(ext.toLowerCase())) {
            hash = String.valueOf(FindDuplicatesService.calculateImageHash(String.valueOf(path)));
        } else {
            hash = calculateSha256(path, 256 * 1024 * 1024); // 256MB limit
        }
        fileRecord.setContentHash(hash);

        fileRecordRepository.save(fileRecord);
    }

    /**
     * Retry logic with exponential backoff and a maximum attempt count.
     * This is the core of the job queue optimization on failures.
     */
    private void requeueFailedTask(ScanQueueItem item) {
        int newAttempts = item.getAttempts() + 1;
        item.setAttempts(newAttempts);

        if (newAttempts >= MAX_ATTEMPTS) {
            log.warn("Giving up on {} after {} attempts", item.getPath(), newAttempts);
            // For now, drop from queue. Future improvement: move to an error/dead-letter table.
            scanQueueItemRepository.delete(item);
            return;
        }

        long now = Instant.now().getEpochSecond();
        long delay = (long) (BASE_RETRY_DELAY_SECONDS * Math.pow(2, newAttempts - 1));
        // attempts=1 -> 5 min, 2 -> 10 min, 3 -> 20 min, etc.

        item.setNotBeforeUnix(now + delay);
=======
        String ext = getFileExtension(path);
        fileRecord.setExt(ext);
        fileRecord.setKind(detectKindFromExtension(ext)); // Simplified kind detection

        // START Josh D. Continued AI processing here
        // Calculate content hash (SAFELY - catch FileSystemException for quarantined files)
        try {
            if (IMAGE_TYPES.contains(ext.toLowerCase())) {
                hash = String.valueOf(FindDuplicatesService.calculateImageHash(String.valueOf(path)));
            } else {
                hash = calculateSha256(path, 256 * 1024 * 1024); // 256MB limit
            }
            fileRecord.setContentHash(hash);
        } catch (FileSystemException fse) {
            // File is quarantined/blocked by Windows Defender or other AV
            log.warn("Cannot read file (likely quarantined by antivirus): {}", pathStr);
            fileRecord.setContentHash("QUARANTINED");
            fileRecord.setKind("quarantined");

            // Save basic metadata
            fileRecordRepository.save(fileRecord);

            // Mark as malicious since it's quarantined
            labelService.applyLabel(
                    pathStr,
                    "Malicious",
                    1.0,
                    "System",
                    "File is quarantined by Windows antivirus software and cannot be accessed."
            );

            return; // Stop processing this file
        } catch (IOException e) {
            log.error("Error calculating hash for {}: {}", pathStr, e.getMessage());
            fileRecord.setContentHash("ERROR");
        }


        // Save basic file metadata first (before labeling)
        fileRecordRepository.save(fileRecord);

        // File classification occurs in two stages
        // ===== STAGE 1: SECURITY SCAN (ClamAV) =====
        boolean isInfected = false;
        try {
            log.info("Running virus scan for: {}", pathStr);
            isInfected = ScanForVirusService.scanFileWithClam(path);

            if (isInfected) {
                log.warn("INFECTED FILE DETECTED: {}", pathStr);

                // LabelService handles EVERYTHING - DB update + history insert
                labelService.applyLabel(
                        pathStr,
                        "Malicious",
                        1.0,
                        "ClamAV",
                        "WARNING: This file contains malware and should be deleted immediately."
                );

                return; // Skip AI analysis for infected files
            }

            // ClamAV says file is clean - record baseline
            log.info("ClamAV: File is clean: {}", pathStr);
            labelService.applyLabel(
                    pathStr,
                    "Safe",
                    0.7,
                    "ClamAV",
                    "File passed ClamAV virus scan with no threats detected."
            );

        } catch (Exception clamEx) {
            log.warn("ClamAV scan failed for {}: {}", pathStr, clamEx.getMessage());
            labelService.applyLabel(
                    pathStr,
                    "Unclassified",
                    0.1,
                    "Error",
                    "ClamAV scan failed: " + clamEx.getMessage()
            );
        }

        // ===== STAGE 2: AI CONTENT ANALYSIS (Only for clean files) =====
        if (SUPPORTED_FILE_TYPES.contains(ext.toLowerCase())) {
            try {
                log.info("Requesting AI analysis for: {}", pathStr);
                String aiResponse = processFileService.processFileAIResponse(path, ext);

                if (aiResponse != null && aiResponse.contains("%")) {
                    String[] parts = aiResponse.split("%", 2);
                    String classification = parts[0].trim();
                    String description = parts.length > 1 ? parts[1].trim() : "";

                    if (classification.equals("Safe") ||
                            classification.equals("Suspicious") ||
                            classification.equals("Malicious")) {

                        // AI can override ClamAV's label - LabelService handles everything
                        labelService.applyLabel(
                                pathStr,
                                classification,
                                0.85,
                                "AI",
                                description
                        );

                        log.info("AI analysis complete: {} - {}", classification,
                                description.substring(0, Math.min(50, description.length())));
                    } else {
                        log.warn("AI returned invalid classification '{}', keeping ClamAV label", classification);
                    }
                } else {
                    log.warn("AI response invalid format: {}, keeping ClamAV label", aiResponse);
                }
            } catch (Exception aiEx) {
                log.warn("AI processing failed for {}: {}", pathStr, aiEx.getMessage());
                log.info("Keeping ClamAV baseline label due to AI failure");
            }
        }
        // If file type not supported by AI, ClamAV's "Safe" label remains
        // END Josh D. Added AI processing here
        // NOTE: No reposity save required since labelService.applyLabel() already saves the FileRecord
    }

    @Transactional
    public void requeueFailedTask(ScanQueueItem item) {
        item.setAttempts(item.getAttempts() + 1);
        item.setNotBeforeUnix(Instant.now().getEpochSecond() + 300); // Try again in 5 minutes
>>>>>>> clean-feature-branch
        scanQueueItemRepository.save(item);
    }

    // --- Helper methods from the original QueueWorker ---

    private String getFileExtension(Path path) {
        String name = path.getFileName().toString();
        int dotIndex = name.lastIndexOf('.');
<<<<<<< HEAD
        return (dotIndex > 0 && dotIndex < name.length() - 1)
                ? name.substring(dotIndex + 1).toLowerCase()
                : "";
    }

    private String detectKindFromExtension(String ext) {
        if (Set.of("jpg","jpeg","png","gif","bmp","tif","tiff","webp","heic").contains(ext)) return "image";
        if (Set.of("mp4","mov","mkv","avi","wmv").contains(ext)) return "video";
        if (Set.of("pdf","doc","docx","xls","xlsx","ppt","pptx","txt","md","csv","json").contains(ext)) return "doc";
=======
        return (dotIndex > 0 && dotIndex < name.length() - 1) ? name.substring(dotIndex + 1).toLowerCase() : "";
    }

    private String detectKindFromExtension(String ext) {
        if (Set.of("jpg", "jpeg", "png", "gif", "bmp", "tif", "tiff", "webp", "heic").contains(ext)) return "image";
        if (Set.of("mp4", "mov", "mkv", "avi", "wmv").contains(ext)) return "video";
        if (Set.of("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "md", "csv", "json").contains(ext))
            return "doc";
>>>>>>> clean-feature-branch
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
<<<<<<< HEAD
            while (bytesRead < bytesToRead &&
                    (read = dis.read(buffer, 0, (int) Math.min(buffer.length, bytesToRead - bytesRead))) != -1) {
=======
            while (bytesRead < bytesToRead && (read = dis.read(buffer, 0, (int) Math.min(buffer.length, bytesToRead - bytesRead))) != -1) {
>>>>>>> clean-feature-branch
                bytesRead += read;
            }
        }
        return HexFormat.of().formatHex(md.digest());
    }
<<<<<<< HEAD
=======

    /**
     * Josh D
     * I made this initially when testing the UI since label classifications were all over the place.
     * This rovides a fallback classification based on file extension when AI fails.
     * This ensures files always have some classification, even if low confidence.
     * NOTE: This should rarely be used now that ClamAV sets baseline labels.
     */
    private String getFallbackClassification(String ext) {
        return switch (ext.toLowerCase()) {
            case "exe", "msi", "dmg", "dll", "so", "bat", "sh", "ps1" -> "Suspicious";
            case "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "md", "csv" -> "Safe";
            case "jpg", "jpeg", "png", "gif", "bmp", "webp" -> "Safe";
            case "mp4", "mov", "avi", "mp3", "wav" -> "Safe";
            case "zip", "rar", "7z", "tar", "gz" -> "Safe";
            case "json", "xml", "yaml", "yml" -> "Safe";
            case "java", "py", "js", "cpp", "c", "h" -> "Safe";
            default -> "Unclassified";
        };

    }
>>>>>>> clean-feature-branch
}