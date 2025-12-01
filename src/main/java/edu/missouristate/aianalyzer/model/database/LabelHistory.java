package edu.missouristate.aianalyzer.model.database;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "label_history")
public class LabelHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String path;
    private String label;
    private Double confidence;
    private String source;
    private long createdUnix;
}
