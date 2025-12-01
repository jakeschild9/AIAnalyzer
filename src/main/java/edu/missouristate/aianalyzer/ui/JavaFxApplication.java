package edu.missouristate.aianalyzer.ui;

import edu.missouristate.aianalyzer.AiAnalyzerApplication;
import edu.missouristate.aianalyzer.ui.event.StageReadyEvent;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import java.util.List;

// This is the main entry point for the JavaFX part of the app.
// It's what connects the UI to the Spring backend.
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class JavaFxApplication extends Application {
    private ConfigurableApplicationContext applicationContext;
    private static List<String> launchArguments;

    // 1. JavaFX calls this `init()` method first.
    // I'm using it to boot up the whole Spring application in the background.
    @Override
    public void init() {
        // Capture command-line arguments before Spring starts
        launchArguments = getParameters().getRaw();

        applicationContext = new SpringApplicationBuilder(AiAnalyzerApplication.class)
                .headless(false)
                .run();
    }

    @Override
    public void start(Stage stage) {
        try {
            System.out.println("JavaFxApplication.start(): Firing StageReadyEvent...");

            StageReadyEvent event = new StageReadyEvent(stage);
            event.setCommandLineArgs(launchArguments);

            applicationContext.publishEvent(event);

            // [ADD THIS] Force wait to see if error happens later
            Thread.sleep(1000);
            System.out.println("[DEBUG] 1 second after event published, checking if stage is showing...");

        } catch (Exception e) {
            System.err.println("============================================");
            System.err.println("ERROR DURING APPLICATION START:");
            System.err.println("============================================");
            e.printStackTrace();

            // [ADD THIS] Print the CAUSE chain
            Throwable cause = e.getCause();
            while (cause != null) {
                System.err.println("--------------- CAUSED BY ---------------");
                cause.printStackTrace();
                cause = cause.getCause();
            }

            System.err.println("============================================");
            Platform.exit();
            System.exit(1);
        }
    }



    // 3. This gets called when the user closes the window.
    // It's just to make sure Spring and JavaFX shut down cleanly.
    @Override
    public void stop() {
        applicationContext.close();
        Platform.exit();
    }

    public static List<String> getLaunchArguments() {
        return launchArguments;
    }
}
