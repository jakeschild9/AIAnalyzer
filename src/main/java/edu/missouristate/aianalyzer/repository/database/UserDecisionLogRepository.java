package edu.missouristate.aianalyzer.repository.database;

import edu.missouristate.aianalyzer.model.database.UserDecisionLog;
import org.springframework.data.jpa.repository.JpaRepository;

<<<<<<< HEAD
/**
 * Used to retrieve records of how a user responds to files for analytics
 */

=======
>>>>>>> clean-feature-branch
public interface UserDecisionLogRepository extends JpaRepository<UserDecisionLog, Long> {
}

