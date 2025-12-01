package edu.missouristate.aianalyzer.utility.ai;

import org.im4java.process.ProcessStarter;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ImageMagickDownloadUtil {
    public static Path magickPath;
    private static final String MAGICK_WINDOWS_URL = "https://imagemagick.org/archive/binaries/ImageMagick-7.1.1-portable-Q16-x64.zip";

    /**
     * Ensures ImageMagick (and im4java tool configuration) is installed and configured.
     */
    public static void ensureImageMagickInstalled() throws IOException, InterruptedException {
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        Path installDir = Paths.get(System.getProperty("user.home"), "imagemagick");
        Files.createDirectories(installDir);

        if (os.contains("win")) {
            magickPath = installDir.resolve("ImageMagick-7.1.1-portable-Q16-x64/magick.exe");
            if (!Files.exists(magickPath)) {
                System.out.println("Downloading ImageMagick for Windows...");
                Path zipPath = installDir.resolve("imagemagick.zip");
                downloadFile(MAGICK_WINDOWS_URL, zipPath);
                unzip(zipPath, installDir);
            }
            if (!Files.exists(magickPath)) {
                throw new IOException("Failed to locate ImageMagick executable at " + magickPath);
            }
            ProcessStarter.setGlobalSearchPath(magickPath.getParent().toString());
            System.out.println("ImageMagick configured: " + magickPath);

        } else if (os.contains("mac")) {
            magickPath = Paths.get("/usr/local/bin/magick"); // Intel
            if (!Files.exists(magickPath)) {
                magickPath = Paths.get("/opt/homebrew/bin/magick"); // Apple Silicon
            }

            if (!Files.exists(magickPath)) {
                System.out.println("Installing ImageMagick via Homebrew...");
                runCommand("brew install imagemagick");
                magickPath = Paths.get("/usr/local/bin/magick");
                if (!Files.exists(magickPath)) {
                    magickPath = Paths.get("/opt/homebrew/bin/magick");
                }
            }

            if (!Files.exists(magickPath)) {
                throw new IOException("Failed to install or locate ImageMagick on macOS.");
            }

            ProcessStarter.setGlobalSearchPath(magickPath.getParent().toString());
            System.out.println("ImageMagick configured: " + magickPath);

        } else {
            magickPath = Paths.get("/usr/bin/magick");
            if (!Files.exists(magickPath)) {
                System.out.println("Installing ImageMagick via package manager...");
                runCommand("sudo apt-get update && sudo apt-get install -y imagemagick");
            }

            if (!Files.exists(magickPath)) {
                throw new IOException("Failed to install ImageMagick on Linux.");
            }

            ProcessStarter.setGlobalSearchPath(magickPath.getParent().toString());
            System.out.println("ImageMagick configured: " + magickPath);
        }
    }

    /**
     * Downloads a file from a URL.
     */
    private static void downloadFile(String url, Path target) throws IOException {
        System.out.println("Downloading from: " + url);
        try (InputStream in = URI.create(url).toURL().openStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Unzips a .zip archive into a target directory.
     */
    private static void unzip(Path zipFile, Path targetDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path newFile = targetDir.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(newFile);
                } else {
                    Files.createDirectories(newFile.getParent());
                    Files.copy(zis, newFile, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    /**
     * Runs a shell command (used for Linux installs).
     */
    private static void runCommand(String command) throws IOException {
        try {
            Process process = new ProcessBuilder("bash", "-c", command)
                    .inheritIO()
                    .start();
            int exit = process.waitFor();
            if (exit != 0) {
                throw new IOException("Command failed: " + command);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Command interrupted: " + command, e);
        }
    }
}
