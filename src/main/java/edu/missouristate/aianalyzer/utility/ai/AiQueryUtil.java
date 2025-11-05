package edu.missouristate.aianalyzer.utility.ai;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * Service responsible for making API calls to the Google Gemini AI model.
 * It provides various methods to get different types of analysis on file content.
 */
@Service
@RequiredArgsConstructor
public class AiQueryUtil {
    private final Client client;

    /**
     * Sends the entire file content to the AI for an "ACTIVE" analysis.
     * The AI will provide a summary of the document and a security classification.
     *
     * @param file The complete content of the file as a string.
     * @return The AI's classification and summary, separated by a '|' to split data.
     */
    public String activeResponseFromFile(String file) {
        CompletableFuture<GenerateContentResponse> responseFuture =
                client.async.models.generateContent(
                        "gemini-2.0-flash",
                        "Provide a single, up to 40-word sentence summarizing the main point or summary of the following " +
                                "file content. " +
                                file,
                        null);
        return responseFuture
                .thenApply(GenerateContentResponse::text)
                .join();
    }

    public String activeResponseFromLargeFile(String file, String fileInterpretation) {
        Content content = Content.fromParts(
                Part.fromText("Provide a single, up to 40-word sentence summarizing the main point or summary of the following " +
                        "file content. "),
                Part.fromUri(file, fileInterpretation));

        CompletableFuture<GenerateContentResponse> responseFuture =
                client.async.models.generateContent(
                        "gemini-2.0-flash",
                        content,
                        null);
        return responseFuture
                .thenApply(GenerateContentResponse::text)
                .join();
    }


    public String respondWithImageCategory(String image, String fileInterpretation) throws IOException {
        Content content = Content.fromParts(
                Part.fromText("Provide the word single, two, or group based on the amount of human faces in this photo" +
                        "If there are not human faces respond with miscellaneous. "),
                Part.fromUri(image, fileInterpretation));

        CompletableFuture<GenerateContentResponse> responseFuture =
                client.async.models.generateContent(
                        "gemini-2.0-flash",
                        content,
                        null);
        return responseFuture
                .thenApply(GenerateContentResponse::text)
                .join();
    }
}
