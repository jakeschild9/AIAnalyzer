package edu.missouristate.aianalyzer.utility.ai;

import edu.missouristate.aianalyzer.model.FileInterpretation;
import lombok.RequiredArgsConstructor;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.im4java.process.ProcessStarter;
import org.springframework.stereotype.Service;

import java.io.*;

import static edu.missouristate.aianalyzer.utility.ai.ImageMagickDownloadUtil.ensureImageMagickInstalled;
import static edu.missouristate.aianalyzer.utility.ai.ImageMagickDownloadUtil.magickPath;
import static edu.missouristate.aianalyzer.utility.ai.UploadFileUtil.uploadObject;

/**
 * Utility service for reading and processing image files.
 * Provides methods for detecting image types, converting images to JPG,
 * and uploading images to storage.
 *
 */

@Service
@RequiredArgsConstructor
public class ReadImageUtil {

    /**
     * Determines the internal file type representation based on a common image extension.
     *
     * @param type the image file extension (e.g., "png", "jpg", "webp")
     * @return the corresponding internal FileType string
     * @throws IOException if the image type is unknown or unsupported
     */
    public static FileInterpretation.FileType readImageType(String type) throws IOException {
        return switch (type.toLowerCase()) {
            case "png" -> FileInterpretation.FileType.PNG;
            case "jpg" -> FileInterpretation.FileType.JPG;
            case "jpeg" -> FileInterpretation.FileType.JPEG;
            case "webp" -> FileInterpretation.FileType.WEBP;
            default -> throw new IllegalArgumentException("Unknown image type: " + type);
        };
    }

    /**
     * Converts any image to a JPG format using ImageMagick and uploads it.
     * Ensures ImageMagick is installed, changes the file extension, and uploads
     * the converted file to storage.
     *
     * @param inputFilePath the path to the original image file
     * @throws IOException      if the input file cannot be read or written
     * @throws InterruptedException if the ImageMagick conversion process is interrupted
     * @throws IM4JavaException if an error occurs during ImageMagick execution
     */
    public static void uploadJpgImage(String inputFilePath) throws IOException, InterruptedException, IM4JavaException {

        File outputFile = convertImageToJpg(inputFilePath);

        // Upload converted image
        uploadObject("images" + outputFile.getAbsolutePath(), outputFile.getAbsolutePath());
    }

    public static File convertImageToJpg(String inputFilePath) throws IOException, InterruptedException, IM4JavaException {
        ensureImageMagickInstalled();
        ProcessStarter.setGlobalSearchPath(magickPath.getParent().toString());
        File outputFile = changeExtension(new File(inputFilePath), ".jpg");
        IMOperation op = new IMOperation();
        op.addImage(inputFilePath);
        op.addImage(outputFile.getAbsolutePath());
        ConvertCmd convert = new ConvertCmd(false);
        convert.run(op);
        System.out.println("Converted to " + outputFile.getAbsolutePath());
        return outputFile;
    }

    /**
     * Changes the file extension of the given file to a new extension.
     *
     * @param f            the original file
     * @param newExtension the new file extension (including the dot, e.g., ".jpg")
     * @return a new File object with the updated extension in the same directory
     */
    public static File changeExtension(File f, String newExtension) {
        int i = f.getName().lastIndexOf('.');
        String name = f.getName().substring(0, i);
        return new File(f.getParent(), name + newExtension);
    }
}
