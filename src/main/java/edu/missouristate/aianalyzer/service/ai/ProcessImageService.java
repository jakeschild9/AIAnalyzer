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


@Service
@RequiredArgsConstructor
public class ProcessImageService {
    //AI query service
    private final AiQueryUtil AiQueryService;

    public String processImageAIResponse(Path filePath, String fileType) throws IOException {
        if (!Files.exists(filePath)) {
            return "File does not exist: " + filePath;
        }
        try {
            Path parentDir = filePath.getParent();
            String newFileName = filePath.getFileName().toString().replaceFirst("\\.[^.]+$", ".jpg");
            Path newFilePath = parentDir.resolve(newFileName).toAbsolutePath();

            if (!SUPPORTED_IMAGE_TYPES.contains(fileType)) {
                uploadJpgImage(String.valueOf(filePath));
                return AiQueryService.respondWithImageCategory("gs://aianalyser/images" + newFilePath, "image/jpeg");
            } else {
                uploadObject("images" + filePath, String.valueOf(filePath));
                return AiQueryService.respondWithImageCategory("gs://aianalyser/images" + filePath, ReadImageUtil.readImageType(fileType));
            }
        } catch (IOException e) {
            return "Error processing file: " + e.getMessage();
        } catch (InterruptedException | IM4JavaException e) {
            throw new RuntimeException(e);
        }
    }
}
