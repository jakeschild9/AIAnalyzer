<<<<<<< HEAD
=======

>>>>>>> clean-feature-branch
package edu.missouristate.aianalyzer.service.metrics;

import edu.missouristate.aianalyzer.model.database.*;
import edu.missouristate.aianalyzer.repository.database.*;
import lombok.RequiredArgsConstructor;
<<<<<<< HEAD
import org.springframework.stereotype.Service;
=======
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
>>>>>>> clean-feature-branch
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
<<<<<<< HEAD

@Service
@RequiredArgsConstructor
public class MetricsService {

=======
import java.util.concurrent.CompletableFuture;


/*
 * Service for recording various user and system metrics to DB (insert/update)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MetricsService {

    private final UserDecisionLogRepository userDecisionLogRepository;
>>>>>>> clean-feature-branch
    private final UserDecisionLogRepository decisionRepo;
    private final FileTypeMetricsRepository metricsRepo;
    private final MetricsSummaryRepository summaryRepo;

    private static String normalize(String type) {
        if (type == null || type.isBlank()) return "unknown";
        return type.trim().toLowerCase(Locale.ROOT);
    }

<<<<<<< HEAD
=======
    /**
     * Asynchronously records a user decision without blocking the UI thread.
     * If the database is locked, it will retry automatically.
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CompletableFuture<Void> recordUserDecisionAsync(
            String username,
            String fileId,
            String fileType,
            DecisionType decision,
            String quarantinePath,
            String aiSummary,
            boolean success,
            String errorMessage) {

        try {
            // Use system username as fallback if not provided
            String actualUsername = (username == null || username.isBlank())
                    ? System.getProperty("user.name", "unknown")
                    : username;

            recordUserDecision(actualUsername, fileId, fileType, decision,
                    quarantinePath, aiSummary, success, errorMessage);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Failed to record user decision asynchronously: {}", e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }


>>>>>>> clean-feature-branch
    @Transactional
    public void recordActiveDescribe(String fileType) {
        String ft = normalize(fileType);
        var m = metricsRepo.findByFileType(ft).orElseGet(() -> fresh(ft));
        m.setAiDescribeCount(m.getAiDescribeCount() + 1);
        m.setUpdatedAt(Instant.now());
        metricsRepo.save(m);

        var s = summaryRepo.findById(1).orElseGet(() -> MetricsSummary.builder().id(1).build());
        s.setTotalActiveDescriptions(s.getTotalActiveDescriptions() + 1);
        summaryRepo.save(s);
    }

    @Transactional
    public void recordVirusScan(String fileType, boolean infected) {
        String ft = normalize(fileType);
        var m = metricsRepo.findByFileType(ft).orElseGet(() -> fresh(ft));
        m.setVirusScanCount(m.getVirusScanCount() + 1);
        if (infected) m.setVirusPositiveCount(m.getVirusPositiveCount() + 1);
        m.setUpdatedAt(Instant.now());
        metricsRepo.save(m);

        var s = summaryRepo.findById(1).orElseGet(() -> MetricsSummary.builder().id(1).build());
        s.setTotalVirusScans(s.getTotalVirusScans() + 1);
        if (infected) s.setTotalVirusPositives(s.getTotalVirusPositives() + 1);
        summaryRepo.save(s);
    }

<<<<<<< HEAD
    @Transactional
    public void recordUserDecision(String userId,
                                   String fileId,
                                   String fileType,
                                   DecisionType decision,
                                   Boolean virusInfectedAtDecision,
                                   String aiSummaryExcerpt,
                                   boolean success,
                                   String contextJson) {

        var entry = UserDecisionLog.builder()
                .userId(userId)
                .fileId(fileId)
                .fileType(normalize(fileType))
                .decision(decision)
                .virusInfected(virusInfectedAtDecision)
                .aiSummaryExcerpt(aiSummaryExcerpt)
                .success(success)
                .contextJson(contextJson)
                .createdAt(Instant.now())
                .build();
        decisionRepo.save(entry);

        String ft = normalize(fileType);
        var m = metricsRepo.findByFileType(ft).orElseGet(() -> fresh(ft));
        switch (decision) {
            case DELETE -> {
                m.setUserDeleteCount(m.getUserDeleteCount() + 1);
                if (success) m.setUserSuccessDeleteCount(m.getUserSuccessDeleteCount() + 1);
            }
            case QUARANTINE -> m.setUserQuarantineCount(m.getUserQuarantineCount() + 1);
            case IGNORE -> m.setUserIgnoreCount(m.getUserIgnoreCount() + 1);
        }
        m.setUpdatedAt(Instant.now());
        metricsRepo.save(m);
    }
=======
    /**
     * Synchronous version - records user decision immediately.
     * Used by async wrapper above.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordUserDecision(
            String username,
            String fileId,
            String fileType,
            DecisionType decision,
            String quarantinePath,
            String aiSummary,
            boolean success,
            String errorMessage) {

        log.info("Recording user decision: username={}, fileId={}, fileType={}, decision={}, success={}",
                username, fileId, fileType, decision, success);

        // Save to user_decision_log table
        UserDecisionLog decisionLog = UserDecisionLog.builder()
                .userId(username)
                .fileId(fileId)
                .fileType(fileType)
                .decision(decision)
                .aiSummaryExcerpt(aiSummary)
                .success(success)
                .createdAt(Instant.now())
                .build();

        userDecisionLogRepository.save(decisionLog);
        log.info("Saved to user_decision_log: {}", decisionLog.getId());

        // Update file_type_metrics table
        String ft = normalize(fileType);
        log.info("Normalized fileType '{}' to '{}'", fileType, ft);

        var metrics = metricsRepo.findByFileType(ft).orElseGet(() -> fresh(ft));
        log.info("Found/created metrics for fileType '{}': id={}", ft, metrics.getId());

        // Increment the appropriate counter based on decision type
        switch (decision) {
            case DELETE:
                metrics.setUserDeleteCount(metrics.getUserDeleteCount() + 1);
                if (success) {
                    metrics.setUserSuccessDeleteCount(metrics.getUserSuccessDeleteCount() + 1);
                }
                break;
            case QUARANTINE:
                metrics.setUserQuarantineCount(metrics.getUserQuarantineCount() + 1);
                break;
            case IGNORE:
                metrics.setUserIgnoreCount(metrics.getUserIgnoreCount() + 1);
                break;
        }

        metrics.setUpdatedAt(Instant.now());
        metricsRepo.save(metrics);
        log.info("Saved metrics to file_type_metrics: id={}, fileType={}, ignoreCount={}",
                metrics.getId(), metrics.getFileType(), metrics.getUserIgnoreCount());
}
>>>>>>> clean-feature-branch

    private FileTypeMetrics fresh(String ft) {
        return FileTypeMetrics.builder()
                .fileType(ft)
                .scansCount(0)
                .userDeleteCount(0)
                .userQuarantineCount(0)
                .userIgnoreCount(0)
                .userSuccessDeleteCount(0)
                .focusCount(0)
                .aiDescribeCount(0)
                .virusScanCount(0)
                .virusPositiveCount(0)
                .updatedAt(Instant.now())
                .build();
    }
<<<<<<< HEAD
}
=======
}
>>>>>>> clean-feature-branch
