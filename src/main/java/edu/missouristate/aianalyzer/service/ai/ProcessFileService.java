package edu.missouristate.aianalyzer.service.ai;


import edu.missouristate.aianalyzer.model.FileInterpretation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

import static edu.missouristate.aianalyzer.model.FileInterpretation.SUPPORTED_FILE_TYPES;
import static edu.missouristate.aianalyzer.model.FileInterpretation.VIRUS_FILE_TYPES;
import static edu.missouristate.aianalyzer.service.ai.ReadFileService.*;
import static edu.missouristate.aianalyzer.service.ai.UploadFileService.uploadObject;

/**
 * This service is responsible for processing files and interacting with an AI service for analysis.
 * It differentiates between small and large files, processing them in a memory-efficient manner
 * by reading large files in chunks to avoid loading the entire file into memory.
 */
@Service
@RequiredArgsConstructor
public class ProcessFileService {
    //Scan for virus
    private final ScanForVirusService scanForVirusService;
    //AI query service
    private final AiQueryService AiQueryService;
    //Size of file
    static long fileSize;
    //Max file size before entering into Google Cloud (8MB)
    static final int maxFileSize = 8 * 1024 * 1024; // 8MB

    /**
     * Determines whether to process the file as small or large based on its size and gets the AI response.
     *
     * @param filePath   The path to the file to be processed.
     * @param fileType   The type of file being processed.
     * @return The AI's response as a String, or an error message.
     * @throws IOException If an error occurs during file processing.
     */
    public String processFileAIResponse(Path filePath, String fileType) throws IOException {
        if (!Files.exists(filePath)) {
            return "File does not exist: " + filePath;
        }
        //Call virus scan BEFORE any file readings happen
        try {
            boolean infected = scanForVirusService.scanFileWithClam(filePath);
            if (infected) {
                return "Virus detected in file: " + filePath.getFileName();
            }
        } catch (Exception e) {
            return "Error scanning file: " + e.getMessage();
        }

        fileSize = filePath.toFile().length();
        try {
            if (fileSize <= maxFileSize && SUPPORTED_FILE_TYPES.contains(fileType.toLowerCase())) {
                return processSmallFileAIResponse(filePath, fileType);
            } else if (SUPPORTED_FILE_TYPES.contains(fileType.toLowerCase())) {
                return processLargeFileAIResponse(filePath, fileType);
            } else {
                return "This file type cannot be processed: " + fileType;
            }
        } catch (IOException e) {
            return "Error processing file: " + e.getMessage();
        }
    }

    /**
     * Processes files smaller than or equal to the maxFileSize by reading the entire content into memory.
     *
     * @param filePath   The path to the small file.
     * @param fileType   The type of file being processed.
     * @return The AI's response as a String.
     * @throws IOException If an error occurs while reading the file.
     */
    private String processSmallFileAIResponse(Path filePath, String fileType) throws IOException {
        try {
            String fileContent = ReadFileService.readFileAsString(filePath, fileType);
            return AiQueryService.activeResponseFromFile(fileContent);
        } catch (IOException e) {
            return "Error processing file: " + e.getMessage();
        }
    }

    /**
     * Processes files larger than maxFileSize by reading them in sequential, memory-mapped chunks.
     * It analyzes each chunk and can short-circuit if a non-"Safe" classification is found.
     *
     * @param filePath   The path to the large file.
     * @return The normalized AI response, combining classification and description.
     * @throws IOException If an I/O error occurs.
     */
    public String processLargeFileAIResponse(Path filePath, String fileType) throws IOException {
        if (!Files.exists(filePath)) {
            return "File does not exist: " + filePath;
        }
        try {
            Path parentDir = filePath.getParent();
            String newFileName = filePath.getFileName().toString().replaceFirst("\\.[^.]+$", ".txt");
            Path newFilePath = parentDir.resolve(newFileName).toAbsolutePath();
            uploadFile(filePath, fileType);

            return AiQueryService.activeResponseFromLargeFile("gs://aianalyser/files" + newFilePath, readDocumentType(fileType));
        } catch (IOException e) {
            return "Error processing file: " + e.getMessage();
        }
    }

    /**
     * A static helper method to format the AI's classification and description into a single, delimited string.
     *
     * @param description    The textual description from the AI.
     * @param classification The classification category from the AI.
     * @return A single string in the format "classification|description".
     */
    static String normalizeResponse(String description, String classification) {
        return classification + "|" + description;
    }
}
