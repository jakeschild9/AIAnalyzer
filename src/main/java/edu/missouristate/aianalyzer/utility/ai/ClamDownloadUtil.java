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
public class ClamDownloadUtil {

    /** Download URL for the Windows portable ClamAV distribution. */
    private static final String WINDOWS_CLAMAV_URL =
            "https://www.clamav.net/downloads/production/clamav-0.105.1-win-x64-portable.zip";

    /** Path to the clamscan executable once installation or detection is complete. */
    private static Path clamScanPath;

    /**
     * Ensures ClamAV is installed and ready to be used.
     *
     * If ClamAV is already installed and the executable path is known, this
     * method returns immediately. Otherwise, it detects the operating system
     * and performs the appropriate installation steps.
     *
     * For Windows:
     * - Creates a ClamAV directory in the user's home folder.
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
    public static void ensureClamInstalled() throws IOException, InterruptedException {
        if (clamScanPath != null && Files.exists(clamScanPath)) return;

        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);

        if (os.contains("win")) {
            Path installDir = Paths.get(System.getProperty("user.home"), "clamav");
            Files.createDirectories(installDir);

            Path clamscanExe = installDir.resolve("clamav-x64\\clamscan.exe");
            Path freshclamExe = installDir.resolve("clamav-x64\\freshclam.exe");

            if (!Files.exists(clamscanExe)) {
                System.out.println("Downloading ClamAV portable for Windows...");
                Path zipPath = installDir.resolve("clamav.zip");
                downloadFile(WINDOWS_CLAMAV_URL, zipPath);
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

            runCommand(freshclamExe + " --config-file=" + confFile);

            if (!Files.exists(dbDir.resolve("main.cvd")) &&
                    !Files.exists(dbDir.resolve("main.cld"))) {
                throw new IllegalStateException("ClamAV database not found at: " + dbDir);
            }

            clamScanPath = clamscanExe;
        }

        else if (os.contains("mac")) {
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

            runCommand("/opt/homebrew/bin/freshclam");

            Path clamscan = Paths.get("/opt/homebrew/bin/clamscan");
            if (!Files.exists(clamscan)) {
                throw new IllegalStateException("ClamAV executable not found at: " + clamscan);
            }

            if (!Files.exists(dbDir.resolve("main.cvd"))) {
                throw new IllegalStateException("ClamAV database not found at: " + dbDir);
            }

            clamScanPath = clamscan;
        }

        if (!Files.exists(clamScanPath)) {
            throw new IllegalStateException("ClamAV executable not found at: " + clamScanPath);
        }
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
            System.err.println("Command failed: " + command);
        }
    }
}
