package edu.missouristate.aianalyzer.repository.database;

import edu.missouristate.aianalyzer.model.database.FileTypeMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Provides a log for statistics such as scans, and file deletion
 */
public interface FileTypeMetricsRepository extends JpaRepository<FileTypeMetrics, Long> {
    Optional<FileTypeMetrics> findByFileType(String fileType);
}
