package edu.missouristate.aianalyzer;

import edu.missouristate.aianalyzer.service.config.CloudConfigService;
import edu.missouristate.aianalyzer.service.database.ActiveScanService;
import edu.missouristate.aianalyzer.ui.JavaFxApplication;
import javafx.application.Application;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


/**
 * The main entry point for the AI Analyzer application.
 * Configures Spring Boot settings for data persistence, asynchronous tasks, and scheduling.
 */
@Slf4j
@SpringBootApplication
@EnableScheduling
@EnableAsync
@EntityScan(basePackages = "edu.missouristate.aianalyzer.model.database")
@EnableJpaRepositories(basePackages = "edu.missouristate.aianalyzer.repository.database")
public class AiAnalyzerApplication {

    /**
     * Delegates application startup to the JavaFX framework.
     * @param args Command-line arguments passed to the application.
     */
    public static void main(String[] args) {
        Application.launch(JavaFxApplication.class, args);
    }

    /**
     * Initializes and starts the application's file scanning system upon Spring Boot completion.
     * This method retrieves the user-configured scan directories and initiates both
     * a continuous file watcher and an initial indexing scan.
     * @param activeScanService Service responsible for file system monitoring and indexing.
     * @param cloudConfigService Service to retrieve configuration values, prioritizing user settings.
     * @return A CommandLineRunner that executes the scanning logic.
     */
    @Bean
    CommandLineRunner startFileScanning(
            ActiveScanService activeScanService,
            @Autowired CloudConfigService cloudConfigService) {

        return args -> {
            // Retrieve configured scan directories (from user preferences or application properties).
            String scanRoots = cloudConfigService.getScanRoots();

            if (scanRoots == null || scanRoots.isBlank()) {
                log.warn("No scan directories configured. File scanning has been disabled.");
                log.warn("Review and configure scan directories within the application Settings view.");
                return;
            }

            // Parse the comma-separated string of paths into a List of Path objects.
            List<Path> roots = Arrays.stream(scanRoots.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Paths::get)
                    .collect(Collectors.toList());

            if (roots.isEmpty()) {
                log.warn("No valid scan directories found. File scanning has been disabled.");
                return;
            }

            log.info("=========================================================");
            log.info("INITIALIZING FILE SCANNING SYSTEM");
            log.info("Scan Roots: {}", roots);
            log.info("=========================================================");

            // Starts the continuous passive file watcher (runs asynchronously).
            activeScanService.startPassiveWatcher(roots);

            // Executes the initial active scan to index existing files (blocks startup briefly).
            activeScanService.performActiveScan(roots);

            log.info("File scanning system initialized successfully.");
        };
    }
}