<<<<<<< HEAD
=======
// NOTE: Commented code uses ImageMagick
//package edu.missouristate.aianalyzer.service.photos;
//
//import dev.brachtendorf.jimagehash.hash.Hash;
//import dev.brachtendorf.jimagehash.hashAlgorithms.AverageHash;
//import dev.brachtendorf.jimagehash.hashAlgorithms.HashingAlgorithm;
//import edu.missouristate.aianalyzer.repository.database.FileRecordRepository;
//import edu.missouristate.aianalyzer.repository.database.LabelHistoryRepository;
//import org.im4java.core.IM4JavaException;
//import org.springframework.stereotype.Service;
//
//import javax.imageio.ImageIO;
//import java.awt.image.BufferedImage;
//import java.io.File;
//import java.io.IOException;
//import java.math.BigInteger;
//import java.util.ArrayList;
//
//import static edu.missouristate.aianalyzer.utility.ai.ReadImageUtil.convertImageToJpg;
//import static org.apache.commons.io.FilenameUtils.getExtension;
//
//@Service
//@lombok.RequiredArgsConstructor
//public class FindDuplicatesService {
//
//    public static BigInteger calculateImageHash(String path) throws IOException {
//        BufferedImage image;
//        String ext = getExtension(path);
//        HashingAlgorithm hasher = new AverageHash(32);
//
//        try {
//            if (ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("jpeg")) {
//                image = ImageIO.read(new File(path));
//            } else {
//                image = ImageIO.read(convertImageToJpg(path));
//            }
//        Hash hash = hasher.hash(image);
//
//        return hash.getHashValue();
//        } catch (IOException | InterruptedException | IM4JavaException e) {
//            throw new RuntimeException(e);
//        }
//    }
//}


>>>>>>> clean-feature-branch
package edu.missouristate.aianalyzer.service.photos;

import dev.brachtendorf.jimagehash.hash.Hash;
import dev.brachtendorf.jimagehash.hashAlgorithms.AverageHash;
import dev.brachtendorf.jimagehash.hashAlgorithms.HashingAlgorithm;
<<<<<<< HEAD
import edu.missouristate.aianalyzer.repository.database.FileRecordRepository;
import edu.missouristate.aianalyzer.repository.database.LabelHistoryRepository;
import org.im4java.core.IM4JavaException;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
=======
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
>>>>>>> clean-feature-branch
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
<<<<<<< HEAD
import java.util.ArrayList;

import static edu.missouristate.aianalyzer.utility.ai.ReadImageUtil.convertImageToJpg;
import static org.apache.commons.io.FilenameUtils.getExtension;
=======
import java.util.Set;
>>>>>>> clean-feature-branch

@Service
@lombok.RequiredArgsConstructor
public class FindDuplicatesService {

<<<<<<< HEAD
    public static BigInteger calculateImageHash(String path) throws IOException {
        BufferedImage image;
        String ext = getExtension(path);
        HashingAlgorithm hasher = new AverageHash(32);

        try {
            if (ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("jpeg")) {
                image = ImageIO.read(new File(path));
            } else {
                image = ImageIO.read(convertImageToJpg(path));
            }
        Hash hash = hasher.hash(image);

        return hash.getHashValue();
        } catch (IOException | InterruptedException | IM4JavaException e) {
            throw new RuntimeException(e);
        }
    }
}
=======
    // Supported image formats for hash calculation
    private static final Set<String> SUPPORTED_IMAGE_FORMATS = Set.of(
        "jpg", "jpeg", "png", "gif", "bmp", "wbmp", "tif", "tiff"
    );

    /**
     * Calculates a perceptual hash of an image file.
     * Works with all image formats supported by Java ImageIO (JPG, PNG, GIF, BMP, etc.)
     *
     * @param path The file path to the image
     * @return BigInteger hash value of the image
     * @throws IOException if the file cannot be read or is not a valid image
     */
    public static BigInteger calculateImageHash(String path) throws IOException {
        File imageFile = new File(path);

        if (!imageFile.exists() || !imageFile.isFile()) {
            throw new IOException("File does not exist or is not a regular file: " + path);
        }

        // Validate file extension before attempting to read
        String extension = getFileExtension(imageFile.getName());
        if (!SUPPORTED_IMAGE_FORMATS.contains(extension.toLowerCase())) {
            throw new IOException("Unsupported image format: " + extension + " for file: " + path);
        }

        // Try to read the image using Java's built-in ImageIO
        BufferedImage image = ImageIO.read(imageFile);

        if (image == null) {
            throw new IOException("Unable to read image file (unsupported format or corrupted): " + path);
        }

        try {
            // Convert to RGB if necessary (some formats like PNG may have alpha channel)
            BufferedImage rgbImage = ensureRGB(image);

            // Calculate the perceptual hash
            HashingAlgorithm hasher = new AverageHash(32);
            Hash hash = hasher.hash(rgbImage);

            return hash.getHashValue();
        } catch (Exception e) {
            throw new IOException("Failed to calculate image hash for: " + path, e);
        }
    }

    /**
     * Checks if a file extension is supported for image hash calculation.
     *
     * @param extension The file extension (without dot)
     * @return true if the format is supported
     */
    public static boolean isSupportedImageFormat(String extension) {
        return SUPPORTED_IMAGE_FORMATS.contains(extension.toLowerCase());
    }

    /**
     * Ensures the image is in RGB format for consistent hash calculation.
     * Some image formats (PNG with transparency, indexed color, etc.) need conversion.
     *
     * @param image The source image
     * @return BufferedImage in RGB format
     */
    private static BufferedImage ensureRGB(BufferedImage image) {
        // If already RGB, return as-is
        if (image.getType() == BufferedImage.TYPE_INT_RGB) {
            return image;
        }

        // Convert to RGB
        BufferedImage rgbImage = new BufferedImage(
                image.getWidth(),
                image.getHeight(),
                BufferedImage.TYPE_INT_RGB
        );

        Graphics2D g = rgbImage.createGraphics();
        try {
            // Draw the original image onto the RGB canvas
            g.drawImage(image, 0, 0, null);
        } finally {
            g.dispose();
        }

        return rgbImage;
    }

    /**
     * Extracts the file extension from a filename.
     *
     * @param filename The name of the file
     * @return The extension (without dot) or empty string if none
     */
    private static String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex > 0 && dotIndex < filename.length() - 1)
            ? filename.substring(dotIndex + 1)
            : "";
    }
}
>>>>>>> clean-feature-branch
