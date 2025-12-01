package edu.missouristate.aianalyzer.utility.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import java.nio.file.Paths;

/**
 * Utility service for uploading files to Google Cloud Storage (GCS).
 * Provides a method to upload a file to a specified bucket with optional precondition checks.
 */
@Slf4j
@Service
public class UploadFileUtil {
    private final String projectId;
    private final String bucketName;

    // Constructor injection to set instance fields
    private final edu.missouristate.aianalyzer.service.config.CloudConfigService cloudConfigService;

    public UploadFileUtil(edu.missouristate.aianalyzer.service.config.CloudConfigService cloudConfigService) {
        this.cloudConfigService = cloudConfigService;
        this.projectId = cloudConfigService.getProjectId();
        this.bucketName = cloudConfigService.getBucketName();
        log.info("UploadFileUtil initialized with project: {}, bucket: {}", projectId, bucketName);
    }



    /**
     * Uploads a local file to a Google Cloud Storage bucket.
     * If the object already exists in the bucket, the method uses a generation match
     * precondition to ensure the correct object version is overwritten safely.
     *
     * @param objectName the destination object name in the GCS bucket (including path if needed)
     * @param filePath   the path to the local file to upload
     * @throws IOException if the file cannot be read or uploaded to GCS
     */
    public void uploadObject(String objectName, String filePath) throws IOException {
        // Refresh config in case user updated it
        String currentProjectId = cloudConfigService.getProjectId();
        String currentBucket = cloudConfigService.getBucketName();

        // Validate inputs
        if (currentBucket == null || currentBucket.isEmpty()) {
            throw new IllegalStateException(
                    "Google Cloud bucket name is not configured. Please configure in Settings.");
        }
        if (currentProjectId == null || currentProjectId.isEmpty()) {
            throw new IllegalStateException(
                    "Google Cloud project ID is not configured. Please configure in Settings.");
        }
        if (objectName == null || objectName.isEmpty()) {
            throw new IllegalArgumentException("Object name cannot be null or empty");
        }
        if (filePath == null || filePath.isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }

        // Initialize GCS storage client
        Storage storage = StorageOptions.newBuilder().setProjectId(currentProjectId).build().getService();

        // Create blob ID and blob info
        BlobId blobId = BlobId.of(currentBucket, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

        // Set precondition: create only if object does not exist or use generation match if it exists
        Storage.BlobWriteOption precondition;
        if (storage.get(currentBucket, objectName) == null) {
            precondition = Storage.BlobWriteOption.doesNotExist();
        } else {
            precondition = Storage.BlobWriteOption.generationMatch(storage.get(currentBucket, objectName).getGeneration());
        }

        // Upload the file to GCS
        storage.createFrom(blobInfo, Paths.get(filePath), precondition);

        log.info("File {} uploaded to bucket {} as {}", filePath, currentBucket, objectName);
    }
}
