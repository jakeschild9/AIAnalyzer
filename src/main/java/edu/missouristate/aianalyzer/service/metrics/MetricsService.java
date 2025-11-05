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
}
