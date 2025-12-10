package edu.missouristate.aianalyzer.ui.service;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.springframework.stereotype.Service;

/**
 * Service dedicated to holding and managing data that needs to be updated
 * across the application, specifically from background threads to the JavaFX UI thread.
 * This ensures thread-safe communication using JavaFX properties.
 */
@Service
public class UIUpdateService {

    // Observable property that holds the path of the file currently being processed.
    private final StringProperty currentFilePath = new SimpleStringProperty("Idle. Ready to scan.");

    /**
     * Gets the current file path property for UI binding.
     */
    public StringProperty currentFilePathProperty() {
        return currentFilePath;
    }

    /**
     * Updates the currently processing file path.
     * @param path the new file path string
     */
    public void setCurrentFilePath(String path) {
        // This method will be called from background threads
        // Platform.runLater() in the caller (FileProcessingService) to ensure thread safety
        currentFilePath.set(path);
    }
}