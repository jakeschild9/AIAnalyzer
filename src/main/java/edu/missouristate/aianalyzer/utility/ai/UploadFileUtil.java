package edu.missouristate.aianalyzer.utility.ai;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

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
@Service
@RequiredArgsConstructor
public class UploadFileUtil {

    /**
     * Uploads a local file to a Google Cloud Storage bucket.
     * If the object already exists in the bucket, the method uses a generation match
     * precondition to ensure the correct object version is overwritten safely.
     *
     * @param objectName the destination object name in the GCS bucket (including path if needed)
     * @param filePath   the path to the local file to upload
     * @throws IOException if the file cannot be read or uploaded to GCS
     */
    public static void uploadObject(String objectName, String filePath) throws IOException {
        // The ID of your GCP project
        String projectId = "basic-dispatch-476219-m5";

        // The ID of your GCS bucket
        String bucketName = "aianalyser";

        // Initialize GCS storage client
        Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();

        // Create blob ID and blob info
        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

        // Set precondition: create only if object does not exist or use generation match if it exists
        Storage.BlobWriteOption precondition;
        if (storage.get(bucketName, objectName) == null) {
            precondition = Storage.BlobWriteOption.doesNotExist();
        } else {
            precondition = Storage.BlobWriteOption.generationMatch(
                    storage.get(bucketName, objectName).getGeneration());
        }

        // Upload the file to GCS
        storage.createFrom(blobInfo, Paths.get(filePath), precondition);

        System.out.println(
                "File " + filePath + " uploaded to bucket " + bucketName + " as " + objectName);
    }
}
