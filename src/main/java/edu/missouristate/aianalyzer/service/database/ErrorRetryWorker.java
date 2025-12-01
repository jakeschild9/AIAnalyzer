package edu.missouristate.aianalyzer.service.database;

import edu.missouristate.aianalyzer.model.database.ErrorLog;
import edu.missouristate.aianalyzer.model.database.ScanQueueItem;
import edu.missouristate.aianalyzer.repository.database.ScanQueueItemRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Schedules a worker to retry failed operations
 */
@Component
public class ErrorRetryWorker {
    private final ErrorLogService errorLogService;
    private final ScanQueueItemRepository scanQueueItemRepository;

    public ErrorRetryWorker(ErrorLogService errorLogService,
                            ScanQueueItemRepository scanQueueItemRepository) {
        this.errorLogService = errorLogService;
        this.scanQueueItemRepository = scanQueueItemRepository;
    }

    /**
     * Looks for high priority errors and enqueues them for Active_AI processing
     */
    @Scheduled(fixedDelay = 60_000)
    public void retryHighPriority() {
        for (ErrorLog e : errorLogService.pendingHighPriority(50)) {
            final String pathStr = e.getFilePath();
            if (pathStr == null || pathStr.isBlank()) continue;

            errorLogService.markRetrying(e.getId());
            boolean ok = enqueueActiveAi(pathStr);
            if (ok) {
                errorLogService.markResolved(e.getId());
            }
        }
    }

    /**
     *Enqueues ScanQueueItem for ACTIVE_AI processing for a path
     */
    private boolean enqueueActiveAi(String pathStr) {
        try {
            ScanQueueItem item = new ScanQueueItem();
            item.setPath(pathStr);
            item.setKind(ScanQueueItem.Kind.ACTIVE_AI); // <-- enum, not a String
            item.setNotBeforeUnix(Instant.now().getEpochSecond());
            item.setAttempts(0);
            scanQueueItemRepository.save(item);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
