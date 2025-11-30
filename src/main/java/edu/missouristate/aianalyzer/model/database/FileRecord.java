package edu.missouristate.aianalyzer.model.database;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "files")
public class FileRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String path;

    private String parentPath;
    private long sizeBytes;
    private long mtimeUnix;
    private Long ctimeUnix;
    private long lastScannedUnix; // passive timestamps
    private String contentHash;   // AI cache
    private String kind;
    private String typeLabel;
    private Double typeLabelConfidence;
    private String typeLabelSource;
    private Long typeLabelUpdatedUnix;
    private String ext;

    //Duplicate detector, if true then is part of a duplicate set/group
    @Column(nullable = false)
    private boolean duplicate = false;

    @Deprecated
    private String aiSafety;

    @Deprecated
    @Column(length = 1024)
    private String aiResponse;

    // AI description fields
    @Column(name = "ai_summary", length = 20000)
    private String aiSummary;

    @Column(name = "ai_labels_json", length = 4000)
    private String aiLabelsJson;

    @Column(name = "ai_confidence")
    private Double aiConfidence;

    @Column(name = "ai_analyzed_unix")
    private Long aiAnalyzedUnix;
}