package edu.missouristate.aianalyzer.service.database;

import edu.missouristate.aianalyzer.model.database.ErrorLog;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class ErrorRetryWorker {
    private final ErrorLogService errorLogService;
    private final ActiveScanService activeScanService;
    public ErrorRetryWorker(ErrorLogService errorLogService, ActiveScanService activeScanService) {
        this.errorLogService = errorLogService;
        this.activeScanService = activeScanService;
    }

    @Scheduled(fixedDelay = 60_000)
    public void retryHighPriority() {
        for (ErrorLog e : errorLogService.pendingHighPriority(50)) {
            final String pathStr = e.getFilePath();
            if (pathStr == null || pathStr.isBlank()) continue;

            errorLogService.markRetrying(e.getId());
            boolean ok = requeueActiveAiIfAllowed(pathStr);
            if (ok) {
                errorLogService.markResolved(e.getId());
            }
        }
    }

    private boolean requeueActiveAiIfAllowed(String pathStr) {
        try {
            return activeScanService.requestActiveDescriptionByPath(Path.of(pathStr));
        } catch (Exception ex) {
            return false;
        }
    }
}
