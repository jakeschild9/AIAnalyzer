package edu.missouristate.aianalyzer.service.metrics;

import edu.missouristate.aianalyzer.model.database.FileTypeMetrics;
import edu.missouristate.aianalyzer.repository.database.FileRecordRepository;
import edu.missouristate.aianalyzer.repository.database.FileTypeMetricsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetricsAggregationService {

    private final FileRecordRepository fileRecordRepository;
    private final FileTypeMetricsRepository fileTypeMetricsRepository;

    private final edu.missouristate.aianalyzer.repository.database.ScanQueueItemRepository scanQueueItemRepository;

    private static final int MAX_HISTORY_POINTS = 50;

    // History Queues
    private final Deque<Integer> safeHistoryQueue = new ArrayDeque<>();
    private final Deque<Integer> suspiciousHistoryQueue = new ArrayDeque<>();
    private final Deque<Integer> maliciousHistoryQueue = new ArrayDeque<>();
    private final Deque<Integer> throughputHistoryQueue = new ArrayDeque<>();
    private final Deque<Integer> queueHistoryQueue = new ArrayDeque<>();

    // State trackers
    private long lastSafeCount = 0;
    private long lastSuspiciousCount = 0;
    private long lastMaliciousCount = 0;
    private long lastTotalBytes = 0;

    public synchronized DashboardMetrics getDashboardMetrics() {
        DashboardMetrics metrics = new DashboardMetrics();

        // 1. Fetch Counts (WE NEED ALL 4 NOW)
        metrics.safeCount = fileRecordRepository.countByTypeLabelIgnoreCase("Safe");
        metrics.suspiciousCount = fileRecordRepository.countByTypeLabelIgnoreCase("Suspicious");
        metrics.maliciousCount = fileRecordRepository.countByTypeLabelIgnoreCase("Malicious");
        metrics.unclassifiedCount = fileRecordRepository.countByTypeLabelIgnoreCase("Unclassified"); // Updates count of unclassified files
        metrics.queueCount = scanQueueItemRepository.count(); // Updates count of items in the queue
        metrics.totalBytesProcessed = fileRecordRepository.sumTotalFileBytes(); // Updates total bytes processed

        // 2. Calculate Velocity (Counts)
        int newSafe = (int) (metrics.safeCount - lastSafeCount);
        int newSuspicious = (int) (metrics.suspiciousCount - lastSuspiciousCount);
        int newMalicious = (int) (metrics.maliciousCount - lastMaliciousCount);

        // 3. Calculate Velocity (Bytes)
        long byteDelta = metrics.totalBytesProcessed - lastTotalBytes;
        int kbDelta = (int) (byteDelta / 1024);

        // Update State
        lastSafeCount = metrics.safeCount;
        lastSuspiciousCount = metrics.suspiciousCount;
        lastMaliciousCount = metrics.maliciousCount;
        lastTotalBytes = metrics.totalBytesProcessed;

        // 4. Update Queues
        updateQueue(safeHistoryQueue, Math.max(0, newSafe));
        updateQueue(suspiciousHistoryQueue, Math.max(0, newSuspicious));
        updateQueue(maliciousHistoryQueue, Math.max(0, newMalicious));
        updateQueue(throughputHistoryQueue, Math.max(0, kbDelta));
        // Note: We track the ABSOLUTE count (Depth), not the delta.
        // We want to see how "deep" the water is over time.
        updateQueue(queueHistoryQueue, (int) metrics.queueCount);

        // 5. Populate DTO
        metrics.safeHistory = new ArrayList<>(safeHistoryQueue);
        metrics.suspiciousHistory = new ArrayList<>(suspiciousHistoryQueue);
        metrics.maliciousHistory = new ArrayList<>(maliciousHistoryQueue);
        metrics.throughputHistory = new ArrayList<>(throughputHistoryQueue);
        metrics.queueHistory = new ArrayList<>(queueHistoryQueue);

        // 6. User Actions
        List<FileTypeMetrics> typeMetrics = fileTypeMetricsRepository.findAll();
        for (FileTypeMetrics tm : typeMetrics) {
            metrics.userActionsByType.put(
                    tm.getFileType(),
                    new UserActionData(tm.getUserDeleteCount(), tm.getUserQuarantineCount(), tm.getUserIgnoreCount())
            );
        }

        return metrics;
    }

    private void updateQueue(Deque<Integer> queue, int newValue) {
        queue.addLast(newValue);
        if (queue.size() > MAX_HISTORY_POINTS) {
            queue.removeFirst();
        }
    }

    public static class DashboardMetrics {
        public long safeCount = 0;
        public long suspiciousCount = 0;
        public long maliciousCount = 0;

        public long unclassifiedCount = 0;
        public long queueCount = 0;

        public long totalBytesProcessed = 0;

        public List<Integer> safeHistory = new ArrayList<>();
        public List<Integer> suspiciousHistory = new ArrayList<>();
        public List<Integer> maliciousHistory = new ArrayList<>();
        public List<Integer> throughputHistory = new ArrayList<>();
        public List<Integer> queueHistory = new ArrayList<>();

        public Map<String, UserActionData> userActionsByType = new HashMap<>();

        public long getActionRequiredCount() {
            // "Action Required" usually means Suspicious + Unclassified
            return suspiciousCount + unclassifiedCount;
        }
    }

    public static class UserActionData {
        public final int deletes;
        public final int quarantines;
        public final int ignores;

        public UserActionData(int deletes, int quarantines, int ignores) {
            this.deletes = deletes;
            this.quarantines = quarantines;
            this.ignores = ignores;
        }
        public long getTotal() { return deletes + quarantines + ignores; }
    }
}