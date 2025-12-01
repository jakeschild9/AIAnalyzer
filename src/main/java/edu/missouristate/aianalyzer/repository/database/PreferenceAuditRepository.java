package edu.missouristate.aianalyzer.repository.database;

import edu.missouristate.aianalyzer.model.database.PreferenceAudit;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Used for retrieving history of preference changes for debugging
 */
public interface PreferenceAuditRepository extends JpaRepository<PreferenceAudit, Long> {
}
