package edu.missouristate.aianalyzer.service.database;

import edu.missouristate.aianalyzer.model.database.ErrorLog;
import edu.missouristate.aianalyzer.repository.database.ErrorLogRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

<<<<<<< HEAD
/**
 * Service for creating and updating ErrorLog Entries
 * Centralizes error logging to then integrate the retry worker
 */
=======
>>>>>>> clean-feature-branch
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

<<<<<<< HEAD
    /**
     * Marks an error as retryinh and increments the retry count.
     * Timestamps are updated for the last attempt.
     */
=======
>>>>>>> clean-feature-branch
    public void markRetrying(Long id) {
        repo.findById(id).ifPresent(e -> {
            e.setStatus("retrying");
            e.setRetryCount((e.getRetryCount() == null ? 0 : e.getRetryCount()) + 1);
            e.setLastAttemptUnix(System.currentTimeMillis());
            repo.save(e);
        });
    }

<<<<<<< HEAD
    /**
     * Marks errors as resolved once operation succeeds
     */
=======
>>>>>>> clean-feature-branch
    public void markResolved(Long id) {
        repo.findById(id).ifPresent(e -> { e.setStatus("resolved"); repo.save(e); });
    }

<<<<<<< HEAD
    /**
     * Returns list of errors that are pending or retrying
     */
=======
>>>>>>> clean-feature-branch
    public java.util.List<ErrorLog> pendingHighPriority(int limit) {
        return repo.findPendingHighPriority(PageRequest.of(0, limit));
    }

<<<<<<< HEAD
    /**
     * captures stack trace as a string
     */
=======
>>>>>>> clean-feature-branch
    private static String stackTrace(Throwable t) {
        java.io.StringWriter sw = new java.io.StringWriter();
        t.printStackTrace(new java.io.PrintWriter(sw));
        return sw.toString();
    }
}
