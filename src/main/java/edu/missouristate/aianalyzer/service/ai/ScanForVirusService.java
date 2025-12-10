package edu.missouristate.aianalyzer.service.ai;

<<<<<<< HEAD
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
=======
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
>>>>>>> clean-feature-branch

import java.io.*;
import java.nio.file.*;

import static edu.missouristate.aianalyzer.utility.ai.ClamDownloadUtil.ensureClamInstalled;
<<<<<<< HEAD
=======
import static edu.missouristate.aianalyzer.utility.ai.ClamDownloadUtil.getClamScanPath;
>>>>>>> clean-feature-branch

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
<<<<<<< HEAD
@Service
@RequiredArgsConstructor
public class ScanForVirusService {

    /** Path to the clamscan executable. Loaded from application properties. */
    private static Path clamScanPath;

    /**
     * Constructor that initializes the ClamAV scanner path from configuration properties.
     *
     * @param path Path to the clamscan executable as defined in application properties
     */
    private ScanForVirusService(@Value("${clam.scan.path}") String path) {
        this.clamScanPath = Path.of(path);
    }

=======
@Slf4j
@Service
public class ScanForVirusService {

>>>>>>> clean-feature-branch
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
<<<<<<< HEAD
        ensureClamInstalled();  // sets clamScanPath
=======
        // Ensure ClamAV is installed and get the path
        ensureClamInstalled();
        Path clamScanPath = getClamScanPath();

        if (clamScanPath == null || !Files.exists(clamScanPath)) {
            throw new IllegalStateException("ClamAV executable not found. Please install ClamAV manually.");
        }

        log.debug("Using ClamAV at: {}", clamScanPath);
>>>>>>> clean-feature-branch

        ProcessBuilder pb = new ProcessBuilder(
                clamScanPath.toString(),
                "--infected",
                "--no-summary",
                filePath.toString()
        );
        pb.redirectErrorStream(true);

        Process process = pb.start();

<<<<<<< HEAD
        // Read and print scan output
=======
        // Read and capture scan output
        StringBuilder output = new StringBuilder();
>>>>>>> clean-feature-branch
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {

            String line;
            while ((line = reader.readLine()) != null) {
<<<<<<< HEAD
                System.out.println(line);
=======
                output.append(line).append("\n");
                log.debug("ClamAV: {}", line);
>>>>>>> clean-feature-branch
            }
        }

        int exitCode = process.waitFor();

<<<<<<< HEAD
        if (exitCode == 0) {
            System.out.println("No viruses detected.");
        } else if (exitCode == 1) {
            System.out.println("Virus detected!");
        } else {
            System.out.println("Scan finished with code: " + exitCode);
        }

        // ClamAV returns 1 when a virus is found
        return exitCode == 1;
    }
}
=======
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
>>>>>>> clean-feature-branch
