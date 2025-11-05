package edu.missouristate.aianalyzer.model.database;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "scan_queue",
        indexes = {
                @Index(name="ix_sq_kind_notbefore", columnList = "kind, notBeforeUnix"),
                @Index(name="ix_sq_path", columnList = "path")
        })
public class ScanQueueItem {

    public enum Kind {
        ACTIVE_AI
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String path;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Kind kind;

    private long notBeforeUnix;
    private int attempts;
}
