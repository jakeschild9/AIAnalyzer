package edu.missouristate.aianalyzer.model.database;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(
        name = "file_type_metrics",
        uniqueConstraints = @UniqueConstraint(name="uk_ftm_file_type", columnNames = "file_type")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileTypeMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="file_type", nullable=false, unique=true)
    private String fileType;

    @Builder.Default private Integer scansCount = 0;
    @Builder.Default private Integer userDeleteCount = 0;
    @Builder.Default private Integer userQuarantineCount = 0;
    @Builder.Default private Integer userIgnoreCount = 0;
    @Builder.Default private Integer aiFlagMaliciousCount = 0;
    @Builder.Default private Integer aiFlagSuspiciousCount = 0;
    @Builder.Default private Integer userSuccessDeleteCount = 0;
    @Builder.Default private Integer focusCount = 0;

    @Column(name="updated_at")
    private Instant updatedAt;
}
