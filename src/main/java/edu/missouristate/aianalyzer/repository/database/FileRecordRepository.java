package edu.missouristate.aianalyzer.repository.database;

import edu.missouristate.aianalyzer.model.database.FileRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileRecordRepository extends JpaRepository<FileRecord, Long> {
    // Spring Data JPA gives us findAll(), save(), etc. for free.
    // We can add custom methods like this one.
    Optional<FileRecord> findByPath(String path);
    Optional<FileRecord> findTopByContentHashOrderByAiAnalyzedUnixDesc(String contentHash);
}
