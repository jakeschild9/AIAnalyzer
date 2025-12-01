package edu.missouristate.aianalyzer.service.database;

import edu.missouristate.aianalyzer.model.database.FileRecord;
import edu.missouristate.aianalyzer.repository.database.FileRecordRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Duplicate detection using Where queries.
 *  Checks FileRecord to search for any row in the database with the same hash
 */
@Service
public class DuplicateDetectionService {

    private final FileRecordRepository fileRecordRepository;

    public DuplicateDetectionService(FileRecordRepository fileRecordRepository) {
        this.fileRecordRepository = fileRecordRepository;
    }

    /**
     * Checks the record for duplicates, if detected flags the record.
     */
    public boolean updateDuplicateFlag(FileRecord record) {
        String hash = record.getContentHash();
        if (hash == null || hash.isBlank()) {
            record.setDuplicate(false);
            fileRecordRepository.save(record);
            return false;
        }

        List<FileRecord> others;
        if (record.getId() != null) {
            // File persisted, then excludes itself from the search
            others = fileRecordRepository.findByContentHashAndIdNot(hash, record.getId());
        } else {
            // Search by hash if the file isn't updated
            others = fileRecordRepository.findByContentHash(hash);
        }

        boolean isDuplicate = !others.isEmpty();
        record.setDuplicate(isDuplicate);
        fileRecordRepository.save(record);

        return isDuplicate;
    }
}
