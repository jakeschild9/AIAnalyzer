package edu.missouristate.aianalyzer.service.database;

import edu.missouristate.aianalyzer.model.database.FileRecord;
import edu.missouristate.aianalyzer.repository.database.FileRecordRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;

@Service
public class FileIsolationService {

    private final FileRecordRepository fileRecordRepository;
    private final Path quarantineRoot;

    public FileIsolationService(
            FileRecordRepository fileRecordRepository,
            @Value("${aianalyzer.quarantine-root:quarantine}") String quarantineDir
    ) throws IOException {
        this.fileRecordRepository = fileRecordRepository;
        this.quarantineRoot = Paths.get(quarantineDir).toAbsolutePath().normalize();
        Files.createDirectories(this.quarantineRoot);
    }

    public void isolate(Long fileId) {
        FileRecord record = fileRecordRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found: id=" + fileId));

        Path current = Paths.get(record.getPath());
        if (!Files.exists(current)) return;

        String fileName = current.getFileName().toString();
        Path isoDir = quarantineRoot.resolve(String.valueOf(fileId));
        Path isoFile = isoDir.resolve(fileName);

        try {
            Files.createDirectories(isoDir);
            Files.move(current, isoFile, StandardCopyOption.REPLACE_EXISTING);

            // update DB
            record.setParentPath(isoDir.toString());
            record.setPath(isoFile.toString());
            fileRecordRepository.save(record);

        } catch (IOException e) {
            throw new RuntimeException("Failed to isolate file id=" + fileId, e);
        }
    }

    public void release(Long fileId, String destFolder) {
        FileRecord record = fileRecordRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found: id=" + fileId));

        Path current = Paths.get(record.getPath());
        if (!Files.exists(current)) return;

        Path destRoot = Paths.get(destFolder).toAbsolutePath();
        Path destFile = destRoot.resolve(current.getFileName());

        try {
            Files.createDirectories(destRoot);
            Files.move(current, destFile, StandardCopyOption.REPLACE_EXISTING);

            record.setParentPath(destRoot.toString());
            record.setPath(destFile.toString());
            fileRecordRepository.save(record);

        } catch (IOException e) {
            throw new RuntimeException("Failed to release file id=" + fileId, e);
        }
    }

    public void purge(Long fileId) {
        FileRecord record = fileRecordRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found: id=" + fileId));

        Path filePath = Paths.get(record.getPath());
        Path parentDir = filePath.getParent();

        try {
            if (Files.exists(parentDir)) {
                Files.walk(parentDir)
                        .sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try { Files.deleteIfExists(path); } catch (Exception ignored) {}
                        });
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to purge file id=" + fileId, e);
        }
    }
}
