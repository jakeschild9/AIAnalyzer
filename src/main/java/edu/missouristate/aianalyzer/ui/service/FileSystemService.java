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
<<<<<<< HEAD
 * A service for interacting with the user's file system.
 * Provides methods for scanning directories and calculating sizes.
 * Operations can be slow and should be run on background threads.
=======
 * A service for interacting directly with the user's file system.
 * Provides methods for scanning directories and calculating file/folder sizes.
 * All file I/O operations are potentially slow and should be executed on background threads.
>>>>>>> clean-feature-branch
 */
@Service
public class FileSystemService {

    private static final String[] SIZE_UNITS = {"B", "KB", "MB", "GB", "TB"};

    /**
<<<<<<< HEAD
     * Gets the subdirectories and files for a given path.
     * This should be run on a background thread. It safely handles null paths,
     * paths that are not directories, and directories that cannot be read due to permissions.
=======
     * Gets the immediate subdirectories and files for a given path.
     * This operation should be run on a background thread.
>>>>>>> clean-feature-branch
     *
     * @param path The directory to scan.
     * @return A List of files and folders in the path, or an empty list if the path is invalid or unreadable.
     */
    public List<File> getChildrenForPath(File path) {
<<<<<<< HEAD
        // Guard against null path or a path that isn't a directory
=======
        // Guard against null path or a path that isn't a directory.
>>>>>>> clean-feature-branch
        if (path == null || !path.isDirectory()) {
            return Collections.emptyList();
        }

        File[] children = path.listFiles();

<<<<<<< HEAD
        // listFiles() can return null if there's an I/O error (e.g., permission denied)
        if (children == null) {
            // Optionally, you could log this event
=======
        // listFiles() can return null if there's an I/O error (e.g., permission denied).
        if (children == null) {
>>>>>>> clean-feature-branch
            System.err.println("Could not read directory: " + path.getAbsolutePath());
            return Collections.emptyList();
        }

<<<<<<< HEAD
        // Return the array as a List
=======
        // Return the array as a List.
>>>>>>> clean-feature-branch
        return Arrays.asList(children);
    }

    /**
     * Calculates the total size of a directory recursively.
<<<<<<< HEAD
     * This is a very slow operation and MUST be run on a background thread.
     * It will safely skip any sub-directories it cannot access due to permissions.
=======
     * This is a resource-intensive operation and MUST be run on a background thread.
     * It safely skips any sub-directories or files it cannot access due to permissions.
>>>>>>> clean-feature-branch
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

<<<<<<< HEAD
        // Handle cases where we can't read the directory
        if (children != null) {
            for (File child : children) {
                // Symbolic links can cause infinite loops. A simple check helps prevent this.
                if (Files.isSymbolicLink(child.toPath())) {
                    continue; // Skip symbolic links
=======
        // Handle cases where directory contents cannot be read.
        if (children != null) {
            for (File child : children) {
                // Check if the file is a symbolic link to prevent infinite recursion.
                if (Files.isSymbolicLink(child.toPath())) {
                    continue; // Skip symbolic links.
>>>>>>> clean-feature-branch
                }
                size += calculateDirectorySize(child);
            }
        }
        return size;
    }

    /**
<<<<<<< HEAD
     * Formats a size in bytes into a human-readable string (KB, MB, GB, etc.).
=======
     * Formats a size given in bytes into a human-readable string (KB, MB, GB, etc.).
>>>>>>> clean-feature-branch
     *
     * @param bytes The number of bytes.
     * @return A formatted string like "1.25 GB".
     */
    public static String formatSize(long bytes) {
        if (bytes <= 0) {
            return "0 B";
        }
<<<<<<< HEAD
        // Use logarithm to determine the correct unit
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        // Format to two decimal places
=======
        // Determine the correct unit (B, KB, MB, etc.) using logarithms based on base 1024.
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));

        // Format the size to one decimal place.
>>>>>>> clean-feature-branch
        return new DecimalFormat("#,##0.#")
                .format(bytes / Math.pow(1024, digitGroups)) + " " + SIZE_UNITS[digitGroups];
    }
}