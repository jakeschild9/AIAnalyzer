package edu.missouristate.aianalyzer.service.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.io.*;
import java.nio.file.*;

import static edu.missouristate.aianalyzer.utility.ai.ClamDownloadUtil.ensureClamInstalled;

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
        ensureClamInstalled();  // sets clamScanPath

        ProcessBuilder pb = new ProcessBuilder(
                clamScanPath.toString(),
                "--infected",
                "--no-summary",
                filePath.toString()
        );
        pb.redirectErrorStream(true);

        Process process = pb.start();

        // Read and print scan output
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }

        int exitCode = process.waitFor();

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
