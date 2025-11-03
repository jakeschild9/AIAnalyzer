package edu.missouristate.aianalyzer.service.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.io.*;
import java.net.URI;
import java.nio.file.*;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ScanForVirusService {

    private static final String WINDOWS_CLAMAV_URL = "https://www.clamav.net/downloads/production/clamav-0.105.1-win-x64-portable.zip";
    private static Path clamScanPath;

    private ScanForVirusService(@Value("${clam.scan.path}") String path) {
        this.clamScanPath = Path.of(path);
    }

    public static boolean scanFileWithClam(Path filePath) throws IOException, InterruptedException {
        ensureClamInstalled();  // sets clamScanPath

        ProcessBuilder pb = new ProcessBuilder(clamScanPath.toString(),
                "--infected", "--no-summary", filePath.toString());
        pb.redirectErrorStream(true);

        Process process = pb.start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }
        int exitCode = process.waitFor();
        if (exitCode == 0) System.out.println("No viruses detected.");
        else if (exitCode == 1) System.out.println("Virus detected!");
        else System.out.println("Scan finished with code: " + exitCode);

        return exitCode == 1;
    }

    private static void ensureClamInstalled() throws IOException, InterruptedException {
        if (clamScanPath != null && Files.exists(clamScanPath)) return;

        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        if (os.contains("win")) {
            Path installDir = Paths.get(System.getProperty("user.home"), "clamav");
            Files.createDirectories(installDir);

            Path clamscanExe = installDir.resolve("clamav-x64\\clamscan.exe");
            Path freshclamExe = installDir.resolve("clamav-x64\\freshclam.exe");

            // Download portable ClamAV if missing
            if (!Files.exists(clamscanExe)) {
                System.out.println("Downloading ClamAV portable for Windows...");
                Path zipPath = installDir.resolve("clamav.zip");
                downloadFile(WINDOWS_CLAMAV_URL, zipPath); // e.g. official or trusted mirror
                unzip(zipPath, installDir);
            }

            // Ensure config and DB dirs
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

            if (!Files.exists(dbDir.resolve("main.cvd")) && !Files.exists(dbDir.resolve("main.cld"))) {
                throw new IllegalStateException("ClamAV database not found at: " + dbDir);
            }

            clamScanPath = clamscanExe;
        } else if (os.contains("mac")) {
            runCommand("brew install clamav");

            Path clamDir = Paths.get("/opt/homebrew");
            Path configDir = clamDir.resolve("etc/clamav");
            Path dbDir = clamDir.resolve("var/lib/clamav");
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

            runCommand("/opt/homebrew/bin/freshclam"); // Download definitions

            Path clamscanPath = Paths.get("/opt/homebrew/bin/clamscan");
            if (!Files.exists(clamscanPath)) {
                throw new IllegalStateException("ClamAV executable not found at: " + clamscanPath);
            }

            if (!Files.exists(dbDir.resolve("main.cvd"))) {
                throw new IllegalStateException("ClamAV database not found at: " + dbDir);
            }

            clamScanPath = clamscanPath;
        }
        if (!Files.exists(clamScanPath)) {
            throw new IllegalStateException("ClamAV executable not found at: " + clamScanPath);
        }
    }

    private static void downloadFile(String url, Path target) throws IOException {
        try (InputStream in = URI.create(url).toURL().openStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

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

    private static void runCommand(String command) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(command.split(" ")).inheritIO().start();
        int exit = process.waitFor();
        if (exit != 0) {
            System.err.println("Command failed: " + command);
        }
    }
}
