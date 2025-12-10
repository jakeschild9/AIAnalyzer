<<<<<<< HEAD
=======

>>>>>>> clean-feature-branch
package edu.missouristate.aianalyzer.utility.ai;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.*;
import java.util.Locale;

/**
 * Utility class responsible for ensuring that ClamAV is installed and available.
 *
 * This class supports automatic installation on Windows and macOS:
 * - On Windows, downloads the portable ClamAV ZIP, extracts it, configures
 *   a database directory, and runs freshclam to obtain virus definitions.
 * - On macOS, installs ClamAV through Homebrew and ensures the configuration
 *   and database directories exist.
 *
 * After installation, the utility stores the resolved path to the clamscan
 * executable, which other components of the system use to perform virus scans.
 */
<<<<<<< HEAD
public class ClamDownloadUtil {
=======
public final class ClamDownloadUtil {
>>>>>>> clean-feature-branch

    /** Download URL for the Windows portable ClamAV distribution. */
    private static final String WINDOWS_CLAMAV_URL =
            "https://www.clamav.net/downloads/production/clamav-0.105.1-win-x64-portable.zip";

    /** Path to the clamscan executable once installation or detection is complete. */
<<<<<<< HEAD
    private static Path clamScanPath;
=======
    private static Path clamScanPath = null;

    // Private constructor to prevent instantiation
    private ClamDownloadUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Returns the current ClamAV executable path.
     * @return Path to clamscan executable, or null if not yet initialized
     */
    public static Path getClamScanPath() {
        return clamScanPath;
    }
>>>>>>> clean-feature-branch

    /**
     * Ensures ClamAV is installed and ready to be used.
     *
     * If ClamAV is already installed and the executable path is known, this
     * method returns immediately. Otherwise, it detects the operating system
     * and performs the appropriate installation steps.
     *
     * For Windows:
<<<<<<< HEAD
     * - Creates a ClamAV directory in the user's home folder.
=======
     * - First checks if ClamAV is installed at common locations (C:, D:, E: drives)
     * - If not found, creates a ClamAV directory in the user's home folder.
>>>>>>> clean-feature-branch
     * - Downloads and extracts the portable ClamAV ZIP if missing.
     * - Generates a default freshclam.conf if needed.
     * - Runs freshclam to update virus definitions.
     *
     * For macOS:
     * - Installs ClamAV via Homebrew.
     * - Ensures configuration and definition directories exist.
     * - Runs freshclam to fetch definitions.
     *
     * @throws IOException if file operations, downloads, or directory creation fail
     * @throws InterruptedException if the freshclam command is interrupted
     */
<<<<<<< HEAD
    public static void ensureClamInstalled() throws IOException, InterruptedException {
        if (clamScanPath != null && Files.exists(clamScanPath)) return;
=======
    public static synchronized void ensureClamInstalled() throws IOException, InterruptedException {
        if (clamScanPath != null && Files.exists(clamScanPath)) {
            return; // Already initialized
        }
>>>>>>> clean-feature-branch

        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);

        if (os.contains("win")) {
<<<<<<< HEAD
            Path installDir = Paths.get(System.getProperty("user.home"), "clamav");
            Files.createDirectories(installDir);

            Path clamscanExe = installDir.resolve("clamav-x64\\clamscan.exe");
            Path freshclamExe = installDir.resolve("clamav-x64\\freshclam.exe");
=======
            // Check common Windows installation locations across C:, D:, and E: drives
            Path[] commonPaths = {
                    Paths.get("C:", "Program Files", "ClamAV", "clamscan.exe"),
                    Paths.get("D:", "Program Files", "ClamAV", "clamscan.exe"),
                    Paths.get("E:", "Program Files", "ClamAV", "clamscan.exe"),
                    Paths.get("C:", "Program Files (x86)", "ClamAV", "clamscan.exe"),
                    Paths.get("D:", "Program Files (x86)", "ClamAV", "clamscan.exe"),
                    Paths.get("E:", "Program Files (x86)", "ClamAV", "clamscan.exe"),
                    Paths.get(System.getProperty("user.home"), "clamav", "clamav-x64", "clamscan.exe")
            };

            for (Path path : commonPaths) {
                if (Files.exists(path)) {
                    System.out.println("Found existing ClamAV installation at: " + path);
                    clamScanPath = path;
                    return;
                }
            }

            // If not found, download and install portable version
            System.out.println("ClamAV not found in common locations. Installing portable version...");
            Path installDir = Paths.get(System.getProperty("user.home"), "clamav");
            Files.createDirectories(installDir);

            Path clamscanExe = installDir.resolve("clamav-x64").resolve("clamscan.exe");
            Path freshclamExe = installDir.resolve("clamav-x64").resolve("freshclam.exe");
>>>>>>> clean-feature-branch

            if (!Files.exists(clamscanExe)) {
                System.out.println("Downloading ClamAV portable for Windows...");
                Path zipPath = installDir.resolve("clamav.zip");
                downloadFile(WINDOWS_CLAMAV_URL, zipPath);
<<<<<<< HEAD
=======
                System.out.println("Extracting ClamAV...");
>>>>>>> clean-feature-branch
                unzip(zipPath, installDir);
            }

            Path dbDir = installDir.resolve("clamav-db");
            Files.createDirectories(dbDir);

            Path confFile = installDir.resolve("freshclam.conf");
            if (!Files.exists(confFile)) {
                String confContent = """
                DatabaseDirectory clamav-db
                UpdateLogFile freshclam.log
                LogVerbose yes
                DatabaseMirror database.clamav.net
                Checks 24
                """;
                Files.writeString(confFile, confContent);
            }

<<<<<<< HEAD
            runCommand(freshclamExe + " --config-file=" + confFile);

            if (!Files.exists(dbDir.resolve("main.cvd")) &&
                    !Files.exists(dbDir.resolve("main.cld"))) {
                throw new IllegalStateException("ClamAV database not found at: " + dbDir);
=======
            // Update virus definitions if needed
            if (!Files.exists(dbDir.resolve("main.cvd")) &&
                    !Files.exists(dbDir.resolve("main.cld"))) {
                System.out.println("Updating ClamAV virus definitions (this may take a few minutes)...");
                try {
                    runCommand(freshclamExe.toString() + " --config-file=" + confFile.toString());
                } catch (Exception e) {
                    System.err.println("Warning: Failed to update virus definitions. You may need to run freshclam manually.");
                }
            }

            if (!Files.exists(dbDir.resolve("main.cvd")) &&
                    !Files.exists(dbDir.resolve("main.cld"))) {
                throw new IllegalStateException("ClamAV database not found at: " + dbDir +
                        ". Please run freshclam manually to download virus definitions.");
>>>>>>> clean-feature-branch
            }

            clamScanPath = clamscanExe;
        }

        else if (os.contains("mac")) {
<<<<<<< HEAD
=======
            // Check if already installed
            Path clamscan = Paths.get("/opt/homebrew/bin/clamscan");
            if (Files.exists(clamscan)) {
                System.out.println("Found existing ClamAV installation at: " + clamscan);
                clamScanPath = clamscan;
                return;
            }

            System.out.println("ClamAV not found. Installing via Homebrew...");
>>>>>>> clean-feature-branch
            runCommand("brew install clamav");

            Path baseDir = Paths.get("/opt/homebrew");
            Path configDir = baseDir.resolve("etc/clamav");
            Path dbDir = baseDir.resolve("var/lib/clamav");

            Files.createDirectories(configDir);
            Files.createDirectories(dbDir);

            Path confFile = configDir.resolve("freshclam.conf");
            if (!Files.exists(confFile)) {
                String confContent = """
                        DatabaseDirectory /opt/homebrew/var/lib/clamav
                        UpdateLogFile /opt/homebrew/var/log/freshclam.log
                        LogVerbose yes
                        DatabaseMirror database.clamav.net
                        Checks 24
                        """;
                Files.writeString(confFile, confContent);
            }

<<<<<<< HEAD
            runCommand("/opt/homebrew/bin/freshclam");

            Path clamscan = Paths.get("/opt/homebrew/bin/clamscan");
=======
            System.out.println("Updating ClamAV virus definitions...");
            runCommand("/opt/homebrew/bin/freshclam");

>>>>>>> clean-feature-branch
            if (!Files.exists(clamscan)) {
                throw new IllegalStateException("ClamAV executable not found at: " + clamscan);
            }

            if (!Files.exists(dbDir.resolve("main.cvd"))) {
                throw new IllegalStateException("ClamAV database not found at: " + dbDir);
            }

            clamScanPath = clamscan;
        }

<<<<<<< HEAD
        if (!Files.exists(clamScanPath)) {
            throw new IllegalStateException("ClamAV executable not found at: " + clamScanPath);
        }
=======
        if (clamScanPath == null || !Files.exists(clamScanPath)) {
            throw new IllegalStateException("ClamAV executable not found. Please install ClamAV manually.");
        }

        System.out.println("ClamAV is ready at: " + clamScanPath);
>>>>>>> clean-feature-branch
    }

    /**
     * Downloads a file from a URL into the target path.
     *
     * @param url URL to download from
     * @param target local file location where the download should be saved
     * @throws IOException if downloading or writing the file fails
     */
    private static void downloadFile(String url, Path target) throws IOException {
        try (InputStream in = URI.create(url).toURL().openStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Extracts the contents of a ZIP archive into a target directory.
     *
     * @param zipFile the ZIP file to extract
     * @param targetDir directory where the contents should be placed
     * @throws IOException if extraction fails
     */
    private static void unzip(Path zipFile, Path targetDir) throws IOException {
        try (FileSystem fs = FileSystems.newFileSystem(zipFile, (ClassLoader) null)) {
            for (Path root : fs.getRootDirectories()) {
                Files.walk(root).forEach(source -> {
                    try {
                        Path dest = targetDir.resolve(root.relativize(source).toString());
                        if (Files.isDirectory(source)) {
                            Files.createDirectories(dest);
                        } else {
                            Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
    }

    /**
     * Executes a shell command and waits for completion.
     *
     * @param command the command string to execute
     * @throws IOException if the command cannot be executed
     * @throws InterruptedException if the process is interrupted
     */
    private static void runCommand(String command) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(command.split(" ")).inheritIO().start();
        int exit = process.waitFor();
        if (exit != 0) {
<<<<<<< HEAD
            System.err.println("Command failed: " + command);
        }
    }
}
=======
            System.err.println("Command failed with exit code " + exit + ": " + command);
        }
    }
}
>>>>>>> clean-feature-branch
