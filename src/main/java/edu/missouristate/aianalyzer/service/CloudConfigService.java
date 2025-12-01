package edu.missouristate.aianalyzer.service.config;

import edu.missouristate.aianalyzer.service.database.PreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service to manage cloud configuration settings (Google Cloud Project ID, Bucket Name, etc.).
 * This service ensures that user-defined settings stored in the database take priority
 * over the default values provided in the application.properties file.
 */
@Service
@RequiredArgsConstructor
public class CloudConfigService {

    private final PreferenceService preferenceService;

    // Default project ID read from application.properties (used as the lowest priority fallback).
    @Value("${google.cloud.project:}")
    private String defaultProjectId;

    // Default bucket name read from application.properties.
    @Value("${google.cloud.bucket:}")
    private String defaultBucketName;

    // Default file scan roots read from application.properties.
    @Value("${scan.roots:}")
    private String defaultScanRoots;

    // Namespace key used for storing all cloud configuration settings in the database.
    private static final String NAMESPACE = "cloud";

    // --- GETTERS (Prefer user preferences, fall back to defaults) ---

    // Retrieves the Google Cloud Project ID.
    public String getProjectId() {
        return preferenceService.getString(NAMESPACE, "project-id")
                .orElse(defaultProjectId);
    }

    // Retrieves the Google Cloud Bucket Name.
    public String getBucketName() {
        return preferenceService.getString(NAMESPACE, "bucket-name")
                .orElse(defaultBucketName);
    }

    // Retrieves the comma-separated list of directories to be scanned.
    public String getScanRoots() {
        return preferenceService.getString(NAMESPACE, "scan-roots")
                .orElse(defaultScanRoots);
    }

    // Retrieves the optional path to the service account credentials JSON file.
    public Optional<String> getCredentialsPath() {
        return preferenceService.getString(NAMESPACE, "credentials-path");
    }

    // --- SETTERS (Save to user preferences) ---

    // Sets and saves the Google Cloud Project ID to the user preferences.
    public void setProjectId(String projectId) {
        preferenceService.setString(NAMESPACE, "project-id", projectId, "SettingsView");
    }

    // Sets and saves the Google Cloud Bucket Name to the user preferences.
    public void setBucketName(String bucketName) {
        preferenceService.setString(NAMESPACE, "bucket-name", bucketName, "SettingsView");
    }

    // Sets and saves the scan roots string to the user preferences.
    public void setScanRoots(String scanRoots) {
        preferenceService.setString(NAMESPACE, "scan-roots", scanRoots, "SettingsView");
    }

    // Sets and saves the credentials file path to the user preferences.
    public void setCredentialsPath(String path) {
        preferenceService.setString(NAMESPACE, "credentials-path", path, "SettingsView");
    }

    // --- VALIDATION ---

    // Checks if the minimum required cloud configuration (Project ID and Bucket Name) is set.
    public boolean isConfigured() {
        String projectId = getProjectId();
        String bucket = getBucketName();
        // Configuration is valid if both values are present and not empty/blank.
        return projectId != null && !projectId.isBlank() &&
                bucket != null && !bucket.isBlank();
    }

    // --- CLEAR (For testing/reset) ---

    // Clears all stored cloud configuration settings in the database.
    public void clearConfiguration() {
        // Set all preference values to empty strings to effectively clear the stored user setting.
        preferenceService.setString(NAMESPACE, "project-id", "", "SettingsView");
        preferenceService.setString(NAMESPACE, "bucket-name", "", "SettingsView");
        preferenceService.setString(NAMESPACE, "scan-roots", "", "SettingsView");
        preferenceService.setString(NAMESPACE, "credentials-path", "", "SettingsView");
    }
}