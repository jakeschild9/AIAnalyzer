package edu.missouristate.aianalyzer.repository.database;

import edu.missouristate.aianalyzer.model.database.VirusScan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

//this will be necessary when PassiveScanService is seperated from ActiveAI queue
public interface VirusScanRepository extends JpaRepository<VirusScan, Long> {
    Optional<VirusScan> findTopByFileIdOrderByScannedAtDesc(Long fileId);
}
