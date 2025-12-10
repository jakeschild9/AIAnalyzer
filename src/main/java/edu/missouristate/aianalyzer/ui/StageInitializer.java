package edu.missouristate.aianalyzer.ui;

import edu.missouristate.aianalyzer.ui.event.StageReadyEvent;
import edu.missouristate.aianalyzer.ui.service.ThemeService;
import edu.missouristate.aianalyzer.ui.view.Home.DriveView;
import edu.missouristate.aianalyzer.ui.view.Metrics.MetricsView;
import edu.missouristate.aianalyzer.ui.view.Settings.SettingsView;
import edu.missouristate.aianalyzer.ui.view.Suggestions.SuggestionsView;
<<<<<<< HEAD
=======
import javafx.application.Platform;
>>>>>>> clean-feature-branch
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
<<<<<<< HEAD
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;


// This class is the main hub for building our UI.
// It waits for the `StageReadyEvent` signal, then pieces together all the different views.
@Component
public class StageInitializer implements ApplicationListener<StageReadyEvent> {

    // All the main pages (views) of our application.
    Scene mainScene;
=======
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

/**
 * Initializes the primary JavaFX stage and sets up the main application window,
 * including dependency injection of views and theme management.
 */
@Component
public class StageInitializer implements ApplicationListener<StageReadyEvent> {

    Scene mainScene;
    BorderPane root;

>>>>>>> clean-feature-branch
    private final DriveView driveView;
    private final MetricsView metricsView;
    private final SettingsView settingsView;
    private final SuggestionsView suggestionsView;
    private final ThemeService themeService;

<<<<<<< HEAD
    // Spring sees this constructor and automatically provides us with all the beans we need.
=======
    private final List<Button> navButtons = new ArrayList<>();

    /**
     * Constructor for Spring dependency injection, providing all required UI components and services.
     */
>>>>>>> clean-feature-branch
    public StageInitializer(DriveView driveView, MetricsView metricsView, SettingsView settingsView, SuggestionsView suggestionsView, ThemeService themeService) {
        this.driveView = driveView;
        this.metricsView = metricsView;
        this.settingsView = settingsView;
        this.suggestionsView = suggestionsView;
<<<<<<< HEAD
        this.themeService  = themeService ;
    }

    // This is where the magic happens. Once `JavaFxApplication` fires the `StageReadyEvent`,
    // this method runs on the main JavaFX thread.@Override
    public void onApplicationEvent(StageReadyEvent event) {
// Get the stage from the event that was published in JavaFxApplication.java
        Stage primaryStage = event.getStage();

        // The BorderPane is our main layout: a top nav bar and a center content area.
        BorderPane root = new BorderPane();
        mainScene = new Scene(root, 1280, 720);

        // Hand the scene over to the theme service so it knows what to apply styles to.
        themeService.setScene(mainScene);

        // --- Navigation Buttons ---
        Button drivesButton = new Button("Drives");
        Button metricsButton = new Button("Metrics");
        Button suggestionsButton = new Button("Suggestions");
        Button settingsButton = new Button("Settings");

        // --- Button Actions ---
        drivesButton.setOnAction(e -> root.setCenter(driveView));
        metricsButton.setOnAction(e -> root.setCenter(metricsView));
        suggestionsButton.setOnAction(e -> root.setCenter(suggestionsView));
        settingsButton.setOnAction(e -> root.setCenter(settingsView));

        // --- Assemble the Nav Bar ---
        HBox navigationBar = new HBox(10, drivesButton, metricsButton, suggestionsButton, settingsButton);
        navigationBar.setAlignment(Pos.CENTER_LEFT);
        navigationBar.setPadding(new Insets(10));
        navigationBar.setStyle("-fx-background-color: -fx-secondary-bg;");

        // --- Put It All Together ---
        root.setTop(navigationBar);
        root.setCenter(driveView);

        themeService.applyTheme("msu-maroon");

        primaryStage.setTitle("AI Analyzer");
        primaryStage.setScene(mainScene);
        primaryStage.show();
    }

=======
        this.themeService = themeService;
    }

    /**
     * Handles the StageReadyEvent, setting up the main UI components, themes, and navigation.
     * @param event The stage ready event containing the primary Stage and command line arguments.
     */
    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        try {
            Stage primaryStage = event.getStage();
            List<String> args = event.getCommandLineArgs();

            // Set up the root container and the main scene dimensions.
            root = new BorderPane();
            mainScene = new Scene(root, 1280, 720);

            // Initialize Theme System and Load Saved Theme
            themeService.setScene(mainScene);

            // Load the saved theme preference, defaulting if none is found.
            String savedTheme = themeService.getSavedTheme();
            System.out.println("[StageInitializer] Loading saved theme: " + savedTheme);
            themeService.applyTheme(savedTheme);

            // Create Header UI
            Label appTitle = new Label("AI Analyzer");
            // Apply a dedicated CSS class for styling the application title.
            appTitle.getStyleClass().add("app-title");

            // Create Navigation Buttons with their respective view actions.
            Button drivesButton = createNavButton("Drives", () -> root.setCenter(driveView));
            Button metricsButton = createNavButton("Metrics", () -> root.setCenter(metricsView));
            Button suggestionsButton = createNavButton("Suggestions", () -> root.setCenter(suggestionsView));
            Button settingsButton = createNavButton("Settings", () -> root.setCenter(settingsView));

            // Use a Region to push the navigation buttons to the right.
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            // Configure the header layout using HBox.
            HBox header = new HBox(15);
            header.setAlignment(Pos.CENTER_LEFT);
            header.setPadding(new Insets(15, 30, 15, 30));

            // Apply a dedicated CSS class for styling the header background and border.
            header.getStyleClass().add("app-header");

            header.getChildren().addAll(appTitle, spacer, drivesButton, metricsButton, suggestionsButton, settingsButton);

            root.setTop(header);

            // Set MetricsView as the default landing page.
            setActiveButton(metricsButton);
            root.setCenter(metricsView);

            // Configure and display the main application window.
            primaryStage.setTitle("AI Analyzer");
            primaryStage.setScene(mainScene);
            primaryStage.show();

            // Check command line arguments for a context menu launch path.
            if (args != null && !args.isEmpty()) {
                handleContextMenuScan(args.get(0));
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize stage", e);
        }
    }

    /**
     * Creates a standardized navigation button with a click action.
     * @param text The display text for the button.
     * @param action The Runnable action to execute when the button is pressed.
     * @return The configured Button object.
     */
    private Button createNavButton(String text, Runnable action) {
        Button btn = new Button(text);

        // Apply shared CSS styling for all navigation buttons.
        btn.getStyleClass().add("nav-button");

        btn.setOnAction(e -> {
            action.run();
            setActiveButton(btn);
        });

        navButtons.add(btn);
        return btn;
    }

    /**
     * Manages the active state of navigation buttons by applying a CSS class.
     * @param activeBtn The button that should be set to the active state.
     */
    private void setActiveButton(Button activeBtn) {
        // Iterate through all navigation buttons to remove the active state.
        for (Button btn : navButtons) {
            btn.getStyleClass().remove("active");
        }
        // Add the 'active' CSS class to the currently selected button.
        activeBtn.getStyleClass().add("active");
    }

    /**
     * Handles the scenario where the application is launched via a context menu,
     * directing the user to the Drives view with the specified path.
     * @param path The file path passed via command line.
     */
    private void handleContextMenuScan(String path) {
        Platform.runLater(() -> {
            navButtons.stream().filter(b -> b.getText().equals("Drives")).findFirst().ifPresent(this::setActiveButton);
            root.setCenter(driveView);
            System.out.println("Context menu scan requested for: " + path);
        });
    }
>>>>>>> clean-feature-branch
}