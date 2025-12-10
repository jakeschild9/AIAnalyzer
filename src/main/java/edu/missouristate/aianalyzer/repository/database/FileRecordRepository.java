package edu.missouristate.aianalyzer.repository.database;

import edu.missouristate.aianalyzer.model.database.FileRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

@Repository
public interface FileRecordRepository extends JpaRepository<FileRecord, Long> {
    // Spring Data JPA gives us findAll(), save(), etc. for free.

    Optional<FileRecord> findByPath(String path);
    Optional<FileRecord> findTopByContentHashOrderByAiAnalyzedUnixDesc(String contentHash);

    @Query("""
        SELECT f FROM FileRecord f
        WHERE LOWER(f.path) LIKE LOWER(CONCAT('%', :q, '%'))
           OR LOWER(f.typeLabel) LIKE LOWER(CONCAT('%', :q, '%'))
           OR LOWER(COALESCE(f.aiSummary, '')) LIKE LOWER(CONCAT('%', :q, '%'))
    """)
    Page<FileRecord> search(@Param("q") String q, Pageable pageable);

    // START Josh D

    // Dynamically fetch all unique file extensions currently present in the database.
    @Query("SELECT DISTINCT f.ext FROM FileRecord f WHERE f.ext IS NOT NULL ORDER BY f.ext ASC")
    List<String> findDistinctExtensions();

    // Finds a paginated list of file records filtered by a specific classification label (e.g., "Malicious").
    Page<FileRecord> findByTypeLabelIgnoreCase(String typeLabel, Pageable pageable);

    // Counts the total number of files with a specific classification label.
    long countByTypeLabelIgnoreCase(String typeLabel);

    // Custom query to calculate the sum of all file sizes in bytes for throughput metrics.
    @Query("SELECT COALESCE(SUM(f.sizeBytes), 0) FROM FileRecord f")
    long sumTotalFileBytes();

    // Custom query to aggregate file count and total size by file extension (for dashboard category stats).
    @Query("SELECT LOWER(f.ext), COUNT(f), SUM(f.sizeBytes) FROM FileRecord f GROUP BY LOWER(f.ext)")
    List<Object[]> getStatsByExtension();

    // Finds a list of file records whose extension is within a given list of extensions.
    @Query("SELECT f FROM FileRecord f WHERE LOWER(f.ext) IN :extensions ORDER BY f.sizeBytes DESC")
    List<FileRecord> findByExtensionIn(@Param("extensions") List<String> extensions);

    // 1. Finds records filtered only by file extension.
    Page<FileRecord> findByExtIgnoreCase(String ext, Pageable pageable);

    // 2. Finds records filtered by both classification status and file extension.
    Page<FileRecord> findByTypeLabelIgnoreCaseAndExtIgnoreCase(String typeLabel, String ext, Pageable pageable);

    // 3. Finds records where the classification status is NULL (Unclassified).
    Page<FileRecord> findByTypeLabelIsNull(Pageable pageable);

    // 4. Finds records where the status is NULL (Unclassified) and filtered by file extension.
    Page<FileRecord> findByTypeLabelIsNullAndExtIgnoreCase(String ext, Pageable pageable);

    // Finds all file records EXCEPT those marked with the given classification status (used to exclude "Ignored").
    Page<FileRecord> findByTypeLabelNotIgnoreCase(String typeLabel, Pageable pageable);
    // END Josh D

    // --- Duplicate image support ---

    /**
     * Aggregated stats for duplicate images:
     * - count of FileRecord entries that are part of a duplicate group
     * - total sizeBytes of those records
     *
     * A duplicate group is defined as files with the same non-null contentHash
     * and an image extension, where group size > 1.
     */
    @Query("""
        SELECT COUNT(f), COALESCE(SUM(f.sizeBytes), 0)
        FROM FileRecord f
        WHERE LOWER(f.ext) IN :imageExts
          AND f.contentHash IS NOT NULL
          AND f.contentHash IN (
                SELECT fh.contentHash
                FROM FileRecord fh
                WHERE LOWER(fh.ext) IN :imageExts
                  AND fh.contentHash IS NOT NULL
                GROUP BY fh.contentHash
                HAVING COUNT(fh) > 1
          )
    """)
    Object[] getDuplicateImageStats(@Param("imageExts") List<String> imageExts);

    /**
     * Returns all FileRecord entries that belong to duplicate image groups
     * (same definition as above).
     */
    @Query("""
        SELECT f
        FROM FileRecord f
        WHERE LOWER(f.ext) IN :imageExts
          AND f.contentHash IS NOT NULL
          AND f.contentHash IN (
                SELECT fh.contentHash
                FROM FileRecord fh
                WHERE LOWER(fh.ext) IN :imageExts
                  AND fh.contentHash IS NOT NULL
                GROUP BY fh.contentHash
                HAVING COUNT(fh) > 1
          )
        ORDER BY f.contentHash ASC, f.sizeBytes DESC
    """)
    List<FileRecord> findDuplicateImages(@Param("imageExts") List<String> imageExts);
}