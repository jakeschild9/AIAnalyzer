package edu.missouristate.aianalyzer.repository.database;

import edu.missouristate.aianalyzer.model.database.FileTypeMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FileTypeMetricsRepository extends JpaRepository<FileTypeMetrics, Long> {
    Optional<FileTypeMetrics> findByFileType(String fileType);
}
