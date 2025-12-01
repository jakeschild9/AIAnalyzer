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

/**
 * Repository for accessing FileRecords.
 * Files are recorded by its path
 */
@Repository
public interface FileRecordRepository extends JpaRepository<FileRecord, Long> {

    Optional<FileRecord> findByPath(String path);

    Optional<FileRecord> findTopByContentHashOrderByAiAnalyzedUnixDesc(String contentHash);

    @Query("""
        SELECT f FROM FileRecord f
        WHERE LOWER(f.path) LIKE LOWER(CONCAT('%', :q, '%'))
           OR LOWER(f.typeLabel) LIKE LOWER(CONCAT('%', :q, '%'))
           OR LOWER(COALESCE(f.aiSummary, '')) LIKE LOWER(CONCAT('%', :q, '%'))
    """)
    Page<FileRecord> search(@Param("q") String q, Pageable pageable);

    List<FileRecord> findByContentHashAndIdNot(String contentHash, Long id);

    List<FileRecord> findByContentHash(String contentHash);
}
