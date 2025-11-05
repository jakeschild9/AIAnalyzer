package edu.missouristate.aianalyzer.model.database;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "metrics_summary")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MetricsSummary {

    @Id
    private Integer id;

    /** number of passive ClamAV scans */
    @Builder.Default private Integer totalVirusScans = 0;

    /** number of clam positive scans */
    @Builder.Default private Integer totalVirusPositives = 0;

    /** active scan run count */
    @Builder.Default private Integer totalActiveDescriptions = 0;

    @Column(name="updated_at")
    private Instant updatedAt;

    @PrePersist @PreUpdate
    void touch() { this.updatedAt = Instant.now(); }
}
