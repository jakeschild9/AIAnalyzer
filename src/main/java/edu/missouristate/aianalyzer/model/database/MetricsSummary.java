package edu.missouristate.aianalyzer.model.database;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "metrics_summary")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetricsSummary {

    @Id
    private Integer id;

    @Builder.Default private Integer totalAiMalicious = 0;
    @Builder.Default private Integer totalAiSafe = 0;
    @Builder.Default private Integer totalAiSuspicious = 0;

    @Column(name="updated_at")
    private Instant updatedAt;
}
