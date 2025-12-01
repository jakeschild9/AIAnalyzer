package edu.missouristate.aianalyzer.repository.database;

import edu.missouristate.aianalyzer.model.database.ErrorLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ErrorLogRepository extends JpaRepository<ErrorLog, Long> {
    @Query("""
    select e from ErrorLog e
    where e.status in ('pending','retrying') and e.filePriority = 1
    order by e.tsUnix asc
  """)
    List<ErrorLog> findPendingHighPriority(org.springframework.data.domain.Pageable page);
}
