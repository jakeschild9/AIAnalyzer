package edu.missouristate.aianalyzer.model.database;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(
        name = "user_decision_log",
        indexes = {
                @Index(name = "idx_udl_user_time", columnList = "user_id,created_at"),
                @Index(name = "idx_udl_filetype", columnList = "file_type")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDecisionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable=false)
    private String userId;

    @Column(name="file_id")
    private String fileId;

    @Column(name="file_type")
    private String fileType;

    @Enumerated(EnumType.STRING)
    @Column(name="decision", nullable=false)
    private DecisionType decision;

    @Enumerated(EnumType.STRING)
    @Column(name="ai_label")
    private AiLabel aiLabel;

    @Column(name="success", nullable=false)
    private Boolean success;

    @Lob
    @Column(name="context_json")
    private String contextJson;

    @Column(name="created_at")
    private Instant createdAt;
}

