package edu.missouristate.aianalyzer.service.metrics;

import edu.missouristate.aianalyzer.model.database.*;
import edu.missouristate.aianalyzer.repository.database.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class MetricsService {

    private final UserDecisionLogRepository decisionRepo;
    private final FileTypeMetricsRepository metricsRepo;
    private final MetricsSummaryRepository summaryRepo;

    private static String normalize(String type) {
        if (type == null || type.isBlank()) return "unknown";
        return type.trim().toLowerCase(Locale.ROOT);
    }

    /** Called when file is scanned by AI */
    @Transactional
    public void recordScan(String fileType) {
        String ft = normalize(fileType);
        var m = metricsRepo.findByFileType(ft).orElseGet(() -> fresh(ft));
        m.setScansCount(m.getScansCount() + 1);
        m.setUpdatedAt(Instant.now());
        metricsRepo.save(m);
    }

    /** Called when AI assigns a label */
    @Transactional
    public void recordAiLabel(String fileType, AiLabel label) {
        String ft = normalize(fileType);
        var m = metricsRepo.findByFileType(ft).orElseGet(() -> fresh(ft));
        switch (label) {
            case MALICIOUS -> m.setAiFlagMaliciousCount(m.getAiFlagMaliciousCount() + 1);
            case SUSPICIOUS -> m.setAiFlagSuspiciousCount(m.getAiFlagSuspiciousCount() + 1);
            case SAFE -> { /* no counter now */ }
        }
        m.setUpdatedAt(Instant.now());
        metricsRepo.save(m);
    }

    /** Called when user does something on AI Summary page */
    @Transactional
    public void recordUserDecision(String userId,
                                   String fileId,
                                   String fileType,
                                   DecisionType decision,
                                   AiLabel aiLabel,
                                   boolean success,
                                   String contextJson) {

        // 1. Append log
        var entry = UserDecisionLog.builder()
                .userId(userId)
                .fileId(fileId)
                .fileType(normalize(fileType))
                .decision(decision)
                .aiLabel(aiLabel)
                .success(success)
                .contextJson(contextJson)
                .createdAt(Instant.now())
                .build();
        decisionRepo.save(entry);

        // 2. Update metrics
        String ft = normalize(fileType);
        var m = metricsRepo.findByFileType(ft).orElseGet(() -> fresh(ft));

        switch (decision) {
            case DELETE -> {
                m.setUserDeleteCount(m.getUserDeleteCount() + 1);
                if (success)
                    m.setUserSuccessDeleteCount(m.getUserSuccessDeleteCount() + 1);
            }
            case QUARANTINE -> m.setUserQuarantineCount(m.getUserQuarantineCount() + 1);
            case IGNORE -> m.setUserIgnoreCount(m.getUserIgnoreCount() + 1);
        }

        m.setUpdatedAt(Instant.now());
        metricsRepo.save(m);
    }

    @Transactional
    public void recordFocusOnType(String fileType) {
        String ft = normalize(fileType);
        var m = metricsRepo.findByFileType(ft).orElseGet(() -> fresh(ft));
        m.setFocusCount(m.getFocusCount() + 1);
        m.setUpdatedAt(Instant.now());
        metricsRepo.save(m);
    }

    private FileTypeMetrics fresh(String ft) {
        return FileTypeMetrics.builder()
                .fileType(ft)
                .scansCount(0)
                .userDeleteCount(0)
                .userQuarantineCount(0)
                .userIgnoreCount(0)
                .aiFlagMaliciousCount(0)
                .aiFlagSuspiciousCount(0)
                .userSuccessDeleteCount(0)
                .focusCount(0)
                .updatedAt(Instant.now())
                .build();
    }
}

