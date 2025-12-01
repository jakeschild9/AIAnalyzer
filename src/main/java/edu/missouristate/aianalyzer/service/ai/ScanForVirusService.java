package edu.missouristate.aianalyzer.service.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;

import static edu.missouristate.aianalyzer.utility.ai.ClamDownloadUtil.ensureClamInstalled;
import static edu.missouristate.aianalyzer.utility.ai.ClamDownloadUtil.getClamScanPath;

/**
 * Service responsible for scanning files for viruses using ClamAV.
 *
 * This service ensures that ClamAV is installed and configured, then runs a scan
 * against the provided file. It uses the ClamAV command-line scanner (clamscan).
 *
 * The scan results follow ClamAV exit code standards:
 * - 0: No virus found
 * - 1: Virus found
 * - Other values: Error or unexpected state
 */
@Slf4j
@Service
public class ScanForVirusService {

    /**
     * Scans a file for viruses using ClamAV's clamscan tool.
     *
     * Steps performed:
     * 1. Ensures ClamAV is installed and available.
     * 2. Runs the clamscan command on the specified file.
     * 3. Streams output to the console.
     * 4. Returns true if a virus is detected based on ClamAV exit codes.
     *
     * @param filePath Path of the file to scan
     * @return true if a virus is detected; false otherwise
     * @throws IOException if the scan process fails to start or read results
     * @throws InterruptedException if the process is interrupted while waiting for results
     */
    public static boolean scanFileWithClam(Path filePath) throws IOException, InterruptedException {
        // Ensure ClamAV is installed and get the path
        ensureClamInstalled();
        Path clamScanPath = getClamScanPath();

        if (clamScanPath == null || !Files.exists(clamScanPath)) {
            throw new IllegalStateException("ClamAV executable not found. Please install ClamAV manually.");
        }

        log.debug("Using ClamAV at: {}", clamScanPath);

        ProcessBuilder pb = new ProcessBuilder(
                clamScanPath.toString(),
                "--infected",
                "--no-summary",
                filePath.toString()
        );
        pb.redirectErrorStream(true);

        Process process = pb.start();

        // Read and capture scan output
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                log.debug("ClamAV: {}", line);
            }
        }

        int exitCode = process.waitFor();

        // Interpret ClamAV exit codes
        if (exitCode == 0) {
            log.debug("No viruses detected in: {}", filePath.getFileName());
            return false;
        } else if (exitCode == 1) {
            log.warn("VIRUS DETECTED in: {}", filePath.getFileName());
            log.warn("ClamAV output:\n{}", output);
            return true;
        } else {
            log.warn("ClamAV scan finished with unexpected exit code {} for file: {}",
                    exitCode, filePath.getFileName());
            log.warn("ClamAV output:\n{}", output);
            return false; // Treat as not infected if scan had errors
        }
    }
}