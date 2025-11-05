package edu.missouristate.aianalyzer.utility.ai;


import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class UploadFileUtil {
    public static void uploadObject(String objectName, String filePath) throws IOException {
        // The ID of your GCP project
        String projectId = "basic-dispatch-476219-m5";

        // The ID of your GCS bucket
        String bucketName = "aianalyser";

        Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();
        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

        Storage.BlobWriteOption precondition;
        if (storage.get(bucketName, objectName) == null) {
            precondition = Storage.BlobWriteOption.doesNotExist();
        } else {
            precondition =
                    Storage.BlobWriteOption.generationMatch(
                            storage.get(bucketName, objectName).getGeneration());
        }
        storage.createFrom(blobInfo, Paths.get(filePath), precondition);

        System.out.println(
                "File " + filePath + " uploaded to bucket " + bucketName + " as " + objectName);
    }
}
