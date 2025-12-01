package edu.missouristate.aianalyzer.repository.database;

import edu.missouristate.aianalyzer.model.database.VirusScan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Provides methods to retrieve latest scan for a file
 */
public interface VirusScanRepository extends JpaRepository<VirusScan, Long> {
    Optional<VirusScan> findTopByFileIdOrderByScannedAtDesc(Long fileId);
}
