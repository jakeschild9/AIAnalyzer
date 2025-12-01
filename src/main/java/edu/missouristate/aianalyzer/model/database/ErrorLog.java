package edu.missouristate.aianalyzer.model.database;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "error_log",
        indexes = {
                @Index(name = "ix_errorlog_status", columnList = "status, filePriority, tsUnix"),
                @Index(name = "ix_errorlog_component", columnList = "component, status"),
                @Index(name = "ix_errorlog_file", columnList = "filePath")
        })
public class ErrorLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ts_unix", nullable = false)
    private long tsUnix = Instant.now().toEpochMilli();

    @Column(nullable = false)
    private String level = "ERROR";

    @Column(nullable = false)
    private String component;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "file_priority")
    private Integer filePriority = 0;

    @Column(name = "error_code")
    private String errorCode;

    @Column(nullable = false, length = 1024)
    private String message;

    @Lob
    private String details;

    @Lob
    @Column(name = "context_json")
    private String contextJson;

    @Column(nullable = false)
    private String status = "pending";

    @Column(name = "last_attempt_unix")
    private Long lastAttemptUnix;

    @Column(name = "retry_count")
    private Integer retryCount = 0;

    // ---- Getters and setters ----
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public long getTsUnix() { return tsUnix; }
    public void setTsUnix(long tsUnix) { this.tsUnix = tsUnix; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getComponent() { return component; }
    public void setComponent(String component) { this.component = component; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public Integer getFilePriority() { return filePriority; }
    public void setFilePriority(Integer filePriority) { this.filePriority = filePriority; }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getContextJson() { return contextJson; }
    public void setContextJson(String contextJson) { this.contextJson = contextJson; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getLastAttemptUnix() { return lastAttemptUnix; }
    public void setLastAttemptUnix(Long lastAttemptUnix) { this.lastAttemptUnix = lastAttemptUnix; }

    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }
}

