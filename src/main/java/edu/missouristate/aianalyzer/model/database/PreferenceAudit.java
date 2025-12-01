package edu.missouristate.aianalyzer.model.database;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "preference_audit")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PreferenceAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable=false)
    private String userId;

    @Column(name="namespace", nullable=false)
    private String namespace;

    @Column(name="pref_key", nullable=false)
    private String key;

    @Column(name="value_snapshot")
    private String valueSnapshot;

    @Column(name="source")
    private String source;

    @Column(name="created_at")
    private Instant createdAt;
}
