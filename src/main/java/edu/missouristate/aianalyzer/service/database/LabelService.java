package edu.missouristate.aianalyzer.service.database;

import edu.missouristate.aianalyzer.model.database.FileRecord;
import edu.missouristate.aianalyzer.model.database.LabelHistory;
import edu.missouristate.aianalyzer.repository.database.FileRecordRepository;
import edu.missouristate.aianalyzer.repository.database.LabelHistoryRepository;
import edu.missouristate.aianalyzer.service.metrics.MetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/*
    After ProcessFile.java gets a result from the AI, it needs a way to permanently store that result
    This service will be responsible for updating the ai_safety and ai_response columns a FileRecord entity
 */
@Service
@RequiredArgsConstructor
public class LabelService {

    private final FileRecordRepository fileRecordRepository;
    private final LabelHistoryRepository labelHistoryRepository;
    private final MetricsService metricsService;

    /**
     * Applies a label to a file record and saves a history entry.
     * @param path The full path of the file.
     * @param label The label to apply (e.g., "Safe", "Suspicious", "Malicious").
     * @param confidence A score representing the confidence in the label (0.0-1.0).
     * @param source The origin of the label (e.g., "ClamAV", "AI").
     * @param summary Optional description/summary text.
     */
    @Transactional // This annotation handles database transactions for us automatically.
    public void applyLabel(String path, String label, Double confidence, String source, String summary) {
        long now = Instant.now().getEpochSecond();

        // 1. Find the file record in the database.
        FileRecord fileRecord = fileRecordRepository.findByPath(path)
                .orElseThrow(() -> new IllegalArgumentException("No such file in database: " + path));

        // 2. Update the main file record with the new label information.
        fileRecord.setTypeLabel(label);
        fileRecord.setTypeLabelConfidence(confidence);
        fileRecord.setTypeLabelSource(source);
        fileRecord.setTypeLabelUpdatedUnix(now);
        fileRecord.setAiAnalyzedUnix(now);

        // Set summary if provided
        if (summary != null && !summary.isBlank()) {
            fileRecord.setAiSummary(summary);
        }


        // 3. Create a new history entry to log this event.
        LabelHistory history = new LabelHistory();
        history.setPath(path);
        history.setLabel(label);
        history.setConfidence(confidence);
        history.setSource(source);
        history.setCreatedUnix(now);
        labelHistoryRepository.save(history);

        // 4. Record metrics if this is an AI-based active description
        if ("AI".equalsIgnoreCase(source) && summary != null && !summary.isBlank()) {
            String fileType = fileRecord.getExt();
            if (fileType == null || fileType.isBlank()) {
                fileType = fileRecord.getKind();
            }
            if (fileType == null || fileType.isBlank()) {
                fileType = "unknown";
            }
            metricsService.recordActiveDescribe(fileType);
        }
    }

    /**
     * Overload without summary for backward compatibility.
     */
    @Transactional
    public void applyLabel(String path, String label, Double confidence, String source) {
        applyLabel(path, label, confidence, source, null);
    }
}
