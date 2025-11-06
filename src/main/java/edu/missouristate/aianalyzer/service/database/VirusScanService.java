package edu.missouristate.aianalyzer.service.database;

import edu.missouristate.aianalyzer.model.database.FileRecord;
import edu.missouristate.aianalyzer.model.database.VirusScan;
import edu.missouristate.aianalyzer.repository.database.FileRecordRepository;
import edu.missouristate.aianalyzer.repository.database.VirusScanRepository;
import edu.missouristate.aianalyzer.service.ai.ScanForVirusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class VirusScanService {

    private final FileRecordRepository fileRecordRepository;
    private final VirusScanRepository virusScanRepository;

    /**
     * Runs clamscan via static method and persists the result to virus_scan.
     */
    @Transactional
    public boolean scanAndPersist(Path path) throws Exception {
        boolean infected = ScanForVirusService.scanFileWithClam(path);

        final String abs = path.toAbsolutePath().toString();
        FileRecord fr = fileRecordRepository.findByPath(abs)
                .orElseThrow(() -> new IllegalArgumentException("File not indexed: " + abs));

        virusScanRepository.save(VirusScan.builder()
                .fileId(fr.getId())
                .infected(infected)
                .signature(null)
                .engine("clamscan")
                .scannedAt(Instant.now())
                .build());

        log.debug("Virus scan persisted for {} (infected={})", abs, infected);
        return infected;
    }

    @Transactional
    public boolean scanAndPersist(Long fileId) throws Exception {
        FileRecord fr = fileRecordRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("No FileRecord with id=" + fileId));
        return scanAndPersist(Path.of(fr.getPath()));
    }
}
