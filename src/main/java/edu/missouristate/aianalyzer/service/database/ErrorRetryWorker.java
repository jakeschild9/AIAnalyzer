package edu.missouristate.aianalyzer.service.database;

import edu.missouristate.aianalyzer.model.database.ErrorLog;
import edu.missouristate.aianalyzer.service.ai.ProcessFileService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

@Component
public class ErrorRetryWorker {
    private final ErrorLogService errorLogService;
    private final ProcessFileService processFileService;
    public ErrorRetryWorker(ErrorLogService errorLogService, ProcessFileService processFileService) {
             this.errorLogService = errorLogService;
             this.processFileService = processFileService;
    }

    @Scheduled(fixedDelay = 60_000)
    public void retryHighPriority() {
        for (ErrorLog e : errorLogService.pendingHighPriority(50)) {
            final String pathStr = e.getFilePath();
            if (pathStr == null || pathStr.isBlank()) continue;

            errorLogService.markRetrying(e.getId());
            boolean ok = attempt(pathStr);
            if (ok) {
                errorLogService.markResolved(e.getId());
            }
        }
    }

    private boolean attempt(String pathStr) {
        try {
            Path p = Path.of(pathStr);

            // Derive
            String name = p.getFileName().toString();
            String ext = "";
            int i = name.lastIndexOf('.');
            if (i >= 0 && i < name.length() - 1) {
                ext = name.substring(i + 1).toLowerCase(Locale.ROOT); // e.g. "pdf"
            }

            // try probe then map to extension
            if (ext.isBlank()) {
                String probed = null;
                try {
                    probed = Files.probeContentType(p);
                } catch (Exception ignore) {
                }
                if (probed != null) {
                    switch (probed.toLowerCase(Locale.ROOT)) {
                        case "application/pdf" -> ext = "pdf";
                        case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> ext = "docx";
                        case "application/msword" -> ext = "doc";
                        case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> ext = "xlsx";
                        case "application/vnd.ms-excel" -> ext = "xls";
                        case "application/vnd.openxmlformats-officedocument.presentationml.presentation" ->
                                ext = "pptx";
                        case "application/vnd.ms-powerpoint" -> ext = "ppt";
                        case "text/plain" -> ext = "txt";
                        case "text/csv" -> ext = "csv";
                        case "application/json" -> ext = "json";
                        case "application/sql", "text/x-sql" -> ext = "sql";
                        default -> ext = ""; // let ProcessFile handle empty
                    }
                }
            }


            String result = processFileService.processFileAIResponse(p, ext);

            if (result == null) return false;

            String lower = result.toLowerCase(Locale.ROOT);
            if (lower.startsWith("error processing file") || lower.startsWith("file does not exist")) {
                return false;
            }
            return true;

        } catch (Exception ex) {
            return false;
        }
    }
}

