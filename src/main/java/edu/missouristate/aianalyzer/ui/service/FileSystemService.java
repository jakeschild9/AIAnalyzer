package edu.missouristate.aianalyzer.ui.service;

import org.springframework.stereotype.Service;
import java.io.File;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A service for interacting directly with the user's file system.
 * Provides methods for scanning directories and calculating file/folder sizes.
 * All file I/O operations are potentially slow and should be executed on background threads.
 */
@Service
public class FileSystemService {

    private static final String[] SIZE_UNITS = {"B", "KB", "MB", "GB", "TB"};

    /**
     * Gets the immediate subdirectories and files for a given path.
     * This operation should be run on a background thread.
     *
     * @param path The directory to scan.
     * @return A List of files and folders in the path, or an empty list if the path is invalid or unreadable.
     */
    public List<File> getChildrenForPath(File path) {
        // Guard against null path or a path that isn't a directory.
        if (path == null || !path.isDirectory()) {
            return Collections.emptyList();
        }

        File[] children = path.listFiles();

        // listFiles() can return null if there's an I/O error (e.g., permission denied).
        if (children == null) {
            System.err.println("Could not read directory: " + path.getAbsolutePath());
            return Collections.emptyList();
        }

        // Return the array as a List.
        return Arrays.asList(children);
    }

    /**
     * Calculates the total size of a directory recursively.
     * This is a resource-intensive operation and MUST be run on a background thread.
     * It safely skips any sub-directories or files it cannot access due to permissions.
     *
     * @param directory The file or directory to calculate the size of.
     * @return The total size in bytes. Returns 0 if the file/directory doesn't exist.
     */
    public long calculateDirectorySize(File directory) {
        if (directory == null || !directory.exists()) {
            return 0;
        }

        // Base case: if it's a file, return its length.
        if (directory.isFile()) {
            return directory.length();
        }

        // Recursive step: if it's a directory, sum the size of its children.
        long size = 0;
        File[] children = directory.listFiles();

        // Handle cases where directory contents cannot be read.
        if (children != null) {
            for (File child : children) {
                // Check if the file is a symbolic link to prevent infinite recursion.
                if (Files.isSymbolicLink(child.toPath())) {
                    continue; // Skip symbolic links.
                }
                size += calculateDirectorySize(child);
            }
        }
        return size;
    }

    /**
     * Formats a size given in bytes into a human-readable string (KB, MB, GB, etc.).
     *
     * @param bytes The number of bytes.
     * @return A formatted string like "1.25 GB".
     */
    public static String formatSize(long bytes) {
        if (bytes <= 0) {
            return "0 B";
        }
        // Determine the correct unit (B, KB, MB, etc.) using logarithms based on base 1024.
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));

        // Format the size to one decimal place.
        return new DecimalFormat("#,##0.#")
                .format(bytes / Math.pow(1024, digitGroups)) + " " + SIZE_UNITS[digitGroups];
    }
}