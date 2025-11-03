package edu.missouristate.aianalyzer.repository.database;

import edu.missouristate.aianalyzer.model.database.PreferenceAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PreferenceAuditRepository extends JpaRepository<PreferenceAudit, Long> {
}
