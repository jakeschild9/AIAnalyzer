package edu.missouristate.aianalyzer.service.database;

import edu.missouristate.aianalyzer.model.database.FileRecord;
import edu.missouristate.aianalyzer.repository.database.FileRecordRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

<<<<<<< HEAD
/**
 * Search function for FileRecords, can be used in the UI search function
 * Wraps FileRecordRepository for paging and sorting by AI analysis.
 */
=======
>>>>>>> clean-feature-branch
@Service
public class SearchService {

    private final FileRecordRepository repo;

    public SearchService(FileRecordRepository repo) {
        this.repo = repo;
    }

    public Page<FileRecord> search(String q, int page, int size) {
        PageRequest paging = PageRequest.of(
                page,
                Math.min(size, 100),
                Sort.by(Sort.Direction.DESC, "aiAnalyzedUnix")
        );
        return repo.search(q, paging);
    }
}
