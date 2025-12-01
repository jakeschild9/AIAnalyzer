package edu.missouristate.aianalyzer.repository.database;

import edu.missouristate.aianalyzer.model.database.ScanQueueItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for managing ScanQueueItem entities.
 * Extends JpaRepository to provide standard database operations.
 */
@Repository
public interface ScanQueueItemRepository extends JpaRepository<ScanQueueItem, Long> {

    // Custom query method that Spring Data JPA implements automatically.
    // This query finds all queue items whose scheduled execution time (notBeforeUnix)
    // is less than or equal to the current time, ordering them by oldest time first.
    // It returns a limited batch of work items (defined by pageable) for processing.
    List<ScanQueueItem> findAllByNotBeforeUnixLessThanEqualOrderByNotBeforeUnix(long notBeforeUnix, Pageable pageable);
}