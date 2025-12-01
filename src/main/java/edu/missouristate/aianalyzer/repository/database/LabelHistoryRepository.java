package edu.missouristate.aianalyzer.repository.database;

import edu.missouristate.aianalyzer.model.database.LabelHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LabelHistoryRepository extends JpaRepository<LabelHistory, Long> {
    // Spring Data JPA provides all the necessary methods (save, findAll, etc.)
}
