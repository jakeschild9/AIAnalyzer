package edu.missouristate.aianalyzer.service.database;

import edu.missouristate.aianalyzer.model.database.ErrorLog;
import edu.missouristate.aianalyzer.repository.database.ErrorLogRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class ErrorLogService {
    private final ErrorLogRepository repo;

    public ErrorLogService(ErrorLogRepository repo) { this.repo = repo; }

    public ErrorLog logError(String component, String filePath, int filePriority,
                             String code, String message, Throwable t, String contextJson) {
        ErrorLog e = new ErrorLog();
        e.setComponent(component);
        e.setFilePath(filePath);
        e.setFilePriority(filePriority);
        e.setErrorCode(code);
        e.setMessage(message);
        e.setDetails(t == null ? null : stackTrace(t));
        e.setContextJson(contextJson);
        return repo.save(e);
    }

    public void markRetrying(Long id) {
        repo.findById(id).ifPresent(e -> {
            e.setStatus("retrying");
            e.setRetryCount((e.getRetryCount() == null ? 0 : e.getRetryCount()) + 1);
            e.setLastAttemptUnix(System.currentTimeMillis());
            repo.save(e);
        });
    }

    public void markResolved(Long id) {
        repo.findById(id).ifPresent(e -> { e.setStatus("resolved"); repo.save(e); });
    }

    public java.util.List<ErrorLog> pendingHighPriority(int limit) {
        return repo.findPendingHighPriority(PageRequest.of(0, limit));
    }

    private static String stackTrace(Throwable t) {
        java.io.StringWriter sw = new java.io.StringWriter();
        t.printStackTrace(new java.io.PrintWriter(sw));
        return sw.toString();
    }
}
