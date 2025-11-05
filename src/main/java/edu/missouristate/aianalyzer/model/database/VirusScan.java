package edu.missouristate.aianalyzer.model.database;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

//this will be necessary when PassiveScanService is seperated from ActiveAI queue.
@Entity
@Table(name = "virus_scan", indexes = {
        @Index(name = "ix_vs_file_time", columnList = "file_id, scanned_at")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VirusScan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK to FileRecord.id  */
    @Column(name = "file_id", nullable = false)
    private Long fileId;

    /** true if ClamA reported infected */
    @Column(name = "infected", nullable = false)
    private boolean infected;

    @Column(name = "signature")
    private String signature;

    @Column(name = "engine", nullable = false)
    private String engine;

    /** when scan occurred */
    @Column(name = "scanned_at", nullable = false)
    private Instant scannedAt;
}
