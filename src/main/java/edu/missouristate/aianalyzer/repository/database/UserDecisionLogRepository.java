package edu.missouristate.aianalyzer.repository.database;

import edu.missouristate.aianalyzer.model.database.UserDecisionLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDecisionLogRepository extends JpaRepository<UserDecisionLog, Long> {
}

