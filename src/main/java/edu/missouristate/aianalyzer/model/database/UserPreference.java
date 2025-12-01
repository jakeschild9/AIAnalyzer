package edu.missouristate.aianalyzer.model.database;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "user_preference",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id","namespace","pref_key"}))
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class UserPreference {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

    @Column(name="user_id", nullable=false) private String userId;
    @Column(nullable=false) private String namespace;
    @Column(name="pref_key", nullable=false) private String key;

    @Column(name="value_type", nullable=false) private String valueType; // STRING|BOOL|INT|DOUBLE|JSON
    private String valueString;
    private Boolean valueBool;
    private Integer valueInt;
    private Double valueDouble;

    @Lob private String jsonValue;

    private String updatedBy;
    @Column(name="updated_at") private Instant updatedAt;
}
