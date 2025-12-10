package edu.missouristate.aianalyzer.repository.database;

import edu.missouristate.aianalyzer.model.database.ScanQueueItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

<<<<<<< HEAD
@Repository
public interface ScanQueueItemRepository extends JpaRepository<ScanQueueItem, Long> {

    /**
     * This is a custom query method that Spring Data JPA will implement for us automatically.
     * It finds all queue items that are "due" (not_before_unix is in the past),
     * orders them by the oldest first, and returns only a limited batch (a "page").
     *
     * This is the method our FileProcessingService needs to get its work.
     *
     * @param notBeforeUnix The current time in Unix epoch seconds.
     * @param pageable      An object that tells the query how many items to return (the batch size).
     * @return A list of ScanQueueItem entities ready to be processed.
     */
    List<ScanQueueItem>
    findAllByNotBeforeUnixLessThanEqualOrderByAttemptsAscNotBeforeUnixAsc(
            long notBeforeUnix,
            Pageable pageable
    );
}

=======
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
>>>>>>> clean-feature-branch
