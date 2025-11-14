package edu.missouristate.aianalyzer.service.ai;

import edu.missouristate.aianalyzer.utility.ai.AiQueryUtil;
import edu.missouristate.aianalyzer.utility.ai.ReadImageUtil;
import lombok.RequiredArgsConstructor;
import org.im4java.core.IM4JavaException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static edu.missouristate.aianalyzer.model.FileInterpretation.SUPPORTED_IMAGE_TYPES;
import static edu.missouristate.aianalyzer.utility.ai.ReadImageUtil.uploadJpgImage;
import static edu.missouristate.aianalyzer.utility.ai.UploadFileUtil.uploadObject;

/**
 * Service responsible for processing local image files and sending them to the AI
 * model for categorization or interpretation.
 *
 * This service:
 * - Validates that the image exists.
 * - Converts unsupported file types to JPEG.
 * - Uploads the image to cloud storage.
 * - Requests an image-category response from the AI.
 *
 * Supported image types are defined in FileInterpretation.SUPPORTED_IMAGE_TYPES.
 */
@Service
@RequiredArgsConstructor
public class ProcessImageService {

    /** Utility class for making content-generation requests to the AI model. */
    private final AiQueryUtil AiQueryService;

    /**
     * Processes an image file and submits it to the AI model.
     *
     * Steps performed by this method:
     * 1. Ensures the file exists.
     * 2. Converts unsupported image formats to JPEG.
     * 3. Uploads the image to cloud storage.
     * 4. Requests an AI-generated category or response for the uploaded image.
     *
     * @param filePath the local filesystem path of the image
     * @param fileType the file extension (for example: "jpg", "png", "webp")
     * @return an AI-generated description or category for the image, or an error message
     * @throws IOException if reading the file or uploading it fails
     * @throws RuntimeException if image conversion fails due to IM4Java issues
     */
    public String processImageAIResponse(Path filePath, String fileType) throws IOException {
        if (!Files.exists(filePath)) {
            return "File does not exist: " + filePath;
        }

        try {
            Path parentDir = filePath.getParent();
            String newFileName = filePath.getFileName().toString().replaceFirst("\\.[^.]+$", ".jpg");
            Path newFilePath = parentDir.resolve(newFileName).toAbsolutePath();

            // Unsupported image types are converted to JPG and then uploaded
            if (!SUPPORTED_IMAGE_TYPES.contains(fileType)) {
                uploadJpgImage(String.valueOf(filePath));
                return AiQueryService.respondWithImageCategory(
                        "gs://aianalyser/images" + newFilePath,
                        "image/jpeg"
                );
            }

            // Supported types are uploaded directly
            uploadObject("images" + filePath, String.valueOf(filePath));
            return AiQueryService.respondWithImageCategory(
                    "gs://aianalyser/images" + filePath,
                    ReadImageUtil.readImageType(fileType)
            );

        } catch (IOException e) {
            return "Error processing file: " + e.getMessage();
        } catch (InterruptedException | IM4JavaException e) {
            throw new RuntimeException(e);
        }
    }
}
