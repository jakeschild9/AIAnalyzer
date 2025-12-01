package edu.missouristate.aianalyzer.repository.database;

import edu.missouristate.aianalyzer.model.database.UserDecisionLog;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Used to retrieve records of how a user responds to files for analytics
 */

public interface UserDecisionLogRepository extends JpaRepository<UserDecisionLog, Long> {
}

