package edu.missouristate.aianalyzer.repository.database;

import edu.missouristate.aianalyzer.model.database.VirusScan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

<<<<<<< HEAD
/**
 * Provides methods to retrieve latest scan for a file
 */
=======

>>>>>>> clean-feature-branch
public interface VirusScanRepository extends JpaRepository<VirusScan, Long> {
    Optional<VirusScan> findTopByFileIdOrderByScannedAtDesc(Long fileId);
}
