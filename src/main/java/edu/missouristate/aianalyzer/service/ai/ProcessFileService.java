package edu.missouristate.aianalyzer.service.ai;

import edu.missouristate.aianalyzer.utility.ai.AiQueryUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import edu.missouristate.aianalyzer.service.database.VirusScanService;

import java.io.*;
import java.nio.file.*;

import static edu.missouristate.aianalyzer.model.FileInterpretation.SUPPORTED_FILE_TYPES;

/**
 * Service responsible for scanning files for viruses and then passing them to the AI
 * for content interpretation. If the file exceeds the maximum size limit for direct
 * processing, it is uploaded to Google Cloud for remote analysis.
 *
 * The workflow:
 * 1. Verify the file exists.
 * 2. Scan the file for viruses through the VirusScanService.
 * 3. Determine whether the file should be processed locally or uploaded to cloud storage.
 * 4. Request an AI-generated description of the file contents.
 */
@Service
@RequiredArgsConstructor
public class ProcessFileService {

    private final AiQueryUtil AiQueryUtil;

    /** Virus scanning service used to detect and persist scan results. */
    private final VirusScanService virusScanService;

    /** Size of the current file being processed. */
    static long fileSize;

    /** Maximum size threshold (8 MB) before processing is delegated to Google Cloud. */
    static final int maxFileSize = 8 * 1024 * 1024;

    /**
     * Processes a file by first scanning it for viruses and then submitting it
     * for AI interpretation. Files larger than the defined size threshold are
     * handled through Google Cloud.
     *
     * Steps performed:
     * 1. Ensures the file exists.
     * 2. Scans the file for viruses. If infected, processing stops.
     * 3. Determines whether the file is small or large based on size.
     * 4. Sends the file to the appropriate AI processing method.
     *
     * @param filePath the path to the file that will be processed
     * @param fileType the extension or format of the file
     * @return an AI-generated response describing the file contents, or an error message
     * @throws IOException if file reading or processing fails
     */
    public String processFileAIResponse(Path filePath, String fileType) throws IOException {
        if (!Files.exists(filePath)) {
            return "File does not exist: " + filePath;
        }

        if (!SUPPORTED_FILE_TYPES.contains(fileType.toLowerCase())) {
            return "This file type cannot be processed: " + fileType;
        }

            // Perform virus scan before reading or processing the file
        try {
            if (virusScanService.scanAndPersist(filePath)) {
                return "Virus detected in file: " + filePath.getFileName();
            }
        } catch (Exception e) {
            return "Error scanning file: " + e.getMessage();
        }

        fileSize = filePath.toFile().length();

        try {
            if (fileSize <= maxFileSize) {
                return AiQueryUtil.processSmallFileAIResponse(filePath, fileType);
            } else {
                return AiQueryUtil.processLargeFileAIResponse(filePath, fileType);
            }
        } catch (Exception e) {
            return "Error processing file: " + e.getMessage();
        }
    }
}
