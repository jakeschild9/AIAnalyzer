package edu.missouristate.aianalyzer.ui.view.Settings;

import edu.missouristate.aianalyzer.service.config.CloudConfigService;
import edu.missouristate.aianalyzer.ui.service.ThemeService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.core.env.Environment;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * The settings page for the app. Handles theme selection and cloud configuration.
 */
@Component
public class SettingsView extends ScrollPane {
    // Declaring required dependencies as final fields for constructor injection.
    private final ThemeService themeService;
    private final CloudConfigService cloudConfigService;
    private final Environment env;

    // UI elements for cloud configuration input fields.
    private PasswordField projectIdField;
    private PasswordField bucketNameField;
    private TextField scanRootsField;
    private TextField credentialsPathField;
    private CheckBox showProjectIdCheckbox;
    private CheckBox showBucketCheckbox;
    private Label statusLabel;

    // Helper to link a theme's name (like "MSU Maroon") to its file (like "msu-maroon.css").
    private record Theme(String displayName, String cssFileName) {}

    /**
     * Constructor for Spring dependency injection. Initializes all required service beans
     * and constructs the main UI layout.
     * @param themeService Service for managing application themes.
     * @param cloudConfigService Service for managing cloud connection configuration.
     * @param env Spring Environment for accessing application properties.
     */
    @Autowired
    public SettingsView(ThemeService themeService, CloudConfigService cloudConfigService, Environment env) {
        this.themeService = themeService;
        this.cloudConfigService = cloudConfigService;
        this.env = env;

        VBox mainContent = new VBox(30);
        mainContent.setPadding(new Insets(20));
        mainContent.setAlignment(Pos.TOP_LEFT);

        Label header = new Label("Application Settings");
        header.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        header.getStyleClass().add("drive-header-label");

        // === CLOUD CONFIGURATION SECTION ===
        VBox cloudSection = createCloudConfigSection();

        // === THEME SECTION ===
        VBox themeSection = createThemeSection();

        // Add all sections to the main VBox for this view.
        mainContent.getChildren().addAll(
                header,
                createSectionDivider(),
                cloudSection,
                createSectionDivider(),
                themeSection
        );

        this.setContent(mainContent);
        this.setFitToWidth(true);
        this.setFitToHeight(false);
        this.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        this.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        this.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    }

    /**
     * Creates the cloud configuration section, including input fields for Project ID,
     * Bucket Name, Credentials Path, and Scan Roots.
     */
    private VBox createCloudConfigSection() {
        VBox section = new VBox(15);
        section.setAlignment(Pos.TOP_LEFT);

        Label sectionHeader = new Label("Google Cloud Configuration");
        sectionHeader.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        sectionHeader.getStyleClass().add("card-title");

        Label description = new Label(
                "Configure your Google Cloud credentials. These settings are stored locally and override application defaults."
        );
        description.setWrapText(true);
        description.setMaxWidth(800);
        description.getStyleClass().add("card-subtitle");

        // Grid for form fields
        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(12);
        form.setMaxWidth(800);
        form.setPadding(new Insets(10, 0, 0, 0));

        // Project ID
        Label projectIdLabel = new Label("Project ID:");
        projectIdLabel.getStyleClass().add("settings-label");

        projectIdField = new PasswordField();

        // Lookup project property from environment (e.g., application.properties)
        String projectIdProp = env.getProperty("google.cloud.project");

        // Use the property value as the prompt text, or a generic placeholder if missing.
        String promptValue = (projectIdProp == null || projectIdProp.isBlank())
                ? "your-google-cloud-project-id"
                : projectIdProp;

        // Set prompt text
        projectIdField.setPromptText(promptValue);
        projectIdField.setText(cloudConfigService.getProjectId());
        projectIdField.getStyleClass().add("custom-input-field");
        projectIdField.setMaxWidth(400);

        showProjectIdCheckbox = new CheckBox("Show");
        showProjectIdCheckbox.getStyleClass().add("custom-checkbox");
        showProjectIdCheckbox.setOnAction(e -> togglePasswordVisibility(projectIdField, showProjectIdCheckbox));

        HBox projectIdBox = new HBox(10, projectIdField, showProjectIdCheckbox);
        projectIdBox.setAlignment(Pos.CENTER_LEFT);

        // Bucket Name
        Label bucketLabel = new Label("Bucket Name:");
        bucketLabel.getStyleClass().add("settings-label");

        bucketNameField = new PasswordField();

        // Lookup bucket property from environment
        String bucketProp = env.getProperty("google.cloud.bucket");

        // Fallback to a non-sensitive placeholder if the property is missing
        String bucketPromptValue = (bucketProp == null || bucketProp.isBlank())
                ? "your-project-bucket-name"
                : bucketProp;

        // Set prompt text
        bucketNameField.setPromptText(bucketPromptValue);

        bucketNameField.setText(cloudConfigService.getBucketName());
        bucketNameField.getStyleClass().add("custom-input-field");
        bucketNameField.setMaxWidth(400);

        showBucketCheckbox = new CheckBox("Show");
        showBucketCheckbox.getStyleClass().add("custom-checkbox");
        showBucketCheckbox.setOnAction(e -> togglePasswordVisibility(bucketNameField, showBucketCheckbox));

        HBox bucketBox = new HBox(10, bucketNameField, showBucketCheckbox);
        bucketBox.setAlignment(Pos.CENTER_LEFT);

        // Credentials File Path
        Label credentialsLabel = new Label("Credentials File:");
        credentialsLabel.getStyleClass().add("settings-label");

        credentialsPathField = new TextField();
        credentialsPathField.setPromptText("Path to service account JSON key (optional)");
        credentialsPathField.setText(cloudConfigService.getCredentialsPath().orElse(""));
        credentialsPathField.getStyleClass().add("custom-input-field");
        credentialsPathField.setMaxWidth(400);

        Button browseBtn = new Button("Browse...");
        browseBtn.getStyleClass().add("custom-button");
        browseBtn.setOnAction(e -> browseForCredentialsFile());

        HBox credentialsBox = new HBox(10, credentialsPathField, browseBtn);
        credentialsBox.setAlignment(Pos.CENTER_LEFT);

        // Scan Roots
        Label scanRootsLabel = new Label("Scan Directories:");
        scanRootsLabel.getStyleClass().add("settings-label");

        scanRootsField = new TextField();

        // Lookup scan roots property from environment
        String rootsProp = env.getProperty("scan.roots");
        String rootsPromptValue = (rootsProp == null || rootsProp.isBlank())
                ? "C:/Users/Username/Documents" // Generic, non-sensitive example
                : rootsProp;

        scanRootsField.setPromptText(rootsPromptValue);

        scanRootsField.setText(cloudConfigService.getScanRoots());
        scanRootsField.getStyleClass().add("custom-input-field");
        scanRootsField.setPrefWidth(500);

        Label scanRootsHint = new Label("Comma-separated paths to monitor");
        scanRootsHint.setStyle("-fx-font-size: 10px; -fx-text-fill: -fx-custom-text-muted;");

        // Add to grid
        form.add(projectIdLabel, 0, 0);
        form.add(projectIdBox, 1, 0);

        form.add(bucketLabel, 0, 1);
        form.add(bucketBox, 1, 1);

        form.add(credentialsLabel, 0, 2);
        form.add(credentialsBox, 1, 2);

        form.add(scanRootsLabel, 0, 3);
        form.add(new VBox(5, scanRootsField, scanRootsHint), 1, 3);

        // Status Label
        statusLabel = new Label("");
        statusLabel.getStyleClass().add("custom-status-label");
        statusLabel.setWrapText(true);
        statusLabel.setMaxWidth(800);

        // Action Buttons
        Button saveBtn = new Button("Save Configuration");
        saveBtn.getStyleClass().add("custom-button");
        saveBtn.setStyle("-fx-background-color: -fx-custom-accent; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");
        saveBtn.setOnAction(e -> saveCloudConfig());

        Button testBtn = new Button("Test Connection");
        testBtn.getStyleClass().add("custom-button");
        testBtn.setStyle("-fx-padding: 10 20;");
        testBtn.setOnAction(e -> testCloudConnection());

        HBox buttonBox = new HBox(10, saveBtn, testBtn);
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        section.getChildren().addAll(
                sectionHeader,
                description,
                form,
                buttonBox,
                statusLabel
        );

        return section;
    }

    /**
     * Creates the theme selection section.
     */
    private VBox createThemeSection() {
        VBox section = new VBox(15);
        section.setAlignment(Pos.TOP_LEFT);

        Label themeHeader = new Label("Application Theme");
        themeHeader.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        themeHeader.getStyleClass().add("card-title");

        // List of all the themes available to the user.
        List<Theme> themes = List.of(
                new Theme("Dev Dark (Default)", "dev-dark"),
                new Theme("Light", "light"),
                new Theme("MSU Maroon", "msu-maroon"),
                new Theme("Monolith", "monolith"),
                new Theme("Clay & Slate", "clay-slate"),
                new Theme("Forest Edge", "forest-edge"),
                new Theme("Ocean's Depth", "oceans-depth"),
                new Theme("Cloud Atlas", "cloud-atlas"),
                new Theme("Quantum Ether", "quantum-ether"),
                new Theme("Canyon Dusk", "canyon-dusk"),
                new Theme("Obsidian", "obsidian"),
                new Theme("Velocity Red", "velocity-red")
        );

        // Use a TilePane to create a grid layout for the theme preview cards.
        TilePane themeGrid = new TilePane();
        themeGrid.setHgap(15);
        themeGrid.setVgap(15);
        themeGrid.setPadding(new Insets(10, 0, 10, 0));
        themeGrid.setPrefColumns(4);

        // Iterate through each theme and create a clickable preview card.
        for (Theme theme : themes) {
            Node themeCard = createThemeCard(theme);
            themeGrid.getChildren().add(themeCard);
        }

        section.getChildren().addAll(themeHeader, themeGrid);
        return section;
    }

    /**
     * Saves cloud configuration settings to the application preferences.
     */
    private void saveCloudConfig() {
        try {
            String projectId = projectIdField.getText().trim();
            String bucket = bucketNameField.getText().trim();
            String scanRoots = scanRootsField.getText().trim();
            String credentialsPath = credentialsPathField.getText().trim();

            // Validation check to ensure required fields are not empty
            if (projectId.isEmpty() || bucket.isEmpty()) {
                statusLabel.setText("Project ID and Bucket Name are required.");
                statusLabel.setStyle("-fx-text-fill: #F44336;");
                return;
            }

            // Persist the configuration values to application preferences
            cloudConfigService.setProjectId(projectId);
            cloudConfigService.setBucketName(bucket);
            cloudConfigService.setScanRoots(scanRoots);

            if (!credentialsPath.isEmpty()) {
                cloudConfigService.setCredentialsPath(credentialsPath);
            }

            statusLabel.setText("Configuration saved successfully! Restart the application for changes to take effect.");
            statusLabel.setStyle("-fx-text-fill: #4CAF50;");

        } catch (Exception ex) {
            statusLabel.setText("Error saving configuration: " + ex.getMessage());
            statusLabel.setStyle("-fx-text-fill: #F44336;");
            ex.printStackTrace();
        }
    }

    /**
     * Tests the cloud connection status based on the configured settings.
     */
    private void testCloudConnection() {
        statusLabel.setText("Testing connection...");
        statusLabel.setStyle("-fx-text-fill: -fx-custom-accent;");

        // Implementation of the actual connection test is pending
        // For now, validation relies on fields being filled
        new Thread(() -> {
            try {
                Thread.sleep(1000); // Simulate network latency/test duration

                javafx.application.Platform.runLater(() -> {
                    if (cloudConfigService.isConfigured()) {
                        statusLabel.setText("Configuration appears valid. Full connection test implementation is pending.");
                        statusLabel.setStyle("-fx-text-fill: #4CAF50;");
                    } else {
                        statusLabel.setText("Configuration incomplete.");
                        statusLabel.setStyle("-fx-text-fill: #F44336;");
                    }
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    statusLabel.setText("Test failed: " + e.getMessage());
                    statusLabel.setStyle("-fx-text-fill: #F44336;");
                });
            }
        }).start();
    }

    /**
     * Opens a file chooser dialog to select the Google Cloud credentials file.
     */
    private void browseForCredentialsFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Google Cloud Credentials File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON Files", "*.json")
        );

        File file = fileChooser.showOpenDialog(this.getScene().getWindow());
        if (file != null) {
            credentialsPathField.setText(file.getAbsolutePath());
        }
    }

    /**
     * Toggles the visibility of a PasswordField by temporarily replacing it with a TextField.
     * @param passwordField The PasswordField to toggle.
     * @param checkbox The CheckBox controlling visibility.
     */
    private void togglePasswordVisibility(PasswordField passwordField, CheckBox checkbox) {
        if (checkbox.isSelected()) {
            // Replace PasswordField with TextField to show the contents
            TextField textField = new TextField(passwordField.getText());
            textField.getStyleClass().addAll(passwordField.getStyleClass());
            textField.setPromptText(passwordField.getPromptText());
            textField.setMaxWidth(passwordField.getMaxWidth());

            // Replace in parent HBox
            HBox parent = (HBox) passwordField.getParent();
            int index = parent.getChildren().indexOf(passwordField);
            parent.getChildren().set(index, textField);

            // Store original PasswordField reference on the TextField for toggling back
            textField.setUserData(passwordField);
        } else {
            // Find the TextField and retrieve the original PasswordField
            HBox parent = (HBox) checkbox.getParent();
            TextField textField = (TextField) parent.getChildren().get(0);
            PasswordField original = (PasswordField) textField.getUserData();
            original.setText(textField.getText());
            parent.getChildren().set(0, original);
        }
    }

    /**
     * Creates a standard visual separator between sections in the settings view.
     */
    private Node createSectionDivider() {
        Separator separator = new Separator();
        separator.setMaxWidth(800);
        separator.setStyle("-fx-background-color: -fx-custom-border;");
        return separator;
    }

    /**
     * Builds a single theme preview card that the user can click to select a theme.
     * @param theme The Theme record containing display name and CSS file name.
     * @return A Node representing the clickable theme card.
     */
    private Node createThemeCard(Theme theme) {
        // Parse the actual theme file to get real colors for the preview
        Map<String, String> colors = themeService.parseThemeColors(theme.cssFileName());

        // Extract the colors needed for the preview, with fallbacks
        String bgColor = colors.getOrDefault("-fx-custom-background", "#1e1e1e");
        String surfaceColor = colors.getOrDefault("-fx-custom-surface", "#2b2b2b");
        String textColor = colors.getOrDefault("-fx-custom-text-primary", "#e0e0e0");
        String accentColor = colors.getOrDefault("-fx-custom-accent", "#009688");

        Color primaryBg = parseColor(bgColor);
        Color secondaryBg = parseColor(surfaceColor);
        Color primaryText = parseColor(textColor);
        Color accent = parseColor(accentColor);

        // The main background of the little preview window.
        Rectangle cardBg = new Rectangle(180, 100);
        cardBg.setFill(primaryBg);
        cardBg.setArcWidth(8);
        cardBg.setArcHeight(8);

        // A small header bar for the preview.
        Rectangle headerBar = new Rectangle(180, 25);
        headerBar.setFill(secondaryBg);
        headerBar.setArcWidth(8);
        headerBar.setArcHeight(8);

        // Fake "text lines" to simulate content within the preview.
        Rectangle textLine1 = new Rectangle(100, 8);
        textLine1.setFill(primaryText);
        Rectangle textLine2 = new Rectangle(140, 8);
        textLine2.setFill(primaryText);

        // A fake "button" to showcase the accent color.
        Rectangle button = new Rectangle(60, 18);
        button.setFill(accent);
        button.setArcWidth(4);
        button.setArcHeight(4);

        // Arrange the fake content parts.
        VBox cardContent = new VBox(6);
        cardContent.setPadding(new Insets(35, 15, 15, 15));
        cardContent.getChildren().addAll(textLine1, textLine2, button);

        // Layer the preview parts using a StackPane.
        StackPane preview = new StackPane();
        preview.getChildren().addAll(cardBg, headerBar, cardContent);
        StackPane.setAlignment(headerBar, Pos.TOP_CENTER);

        // The label displayed under the preview card.
        Label themeNameLabel = new Label(theme.displayName());
        themeNameLabel.setStyle(
                "-fx-font-size: 13px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: -fx-custom-text-primary; " +
                        "-fx-padding: 8 0 0 0;"
        );
        themeNameLabel.setMaxWidth(180);
        themeNameLabel.setWrapText(true);
        themeNameLabel.setAlignment(Pos.CENTER);

        // The final clickable card, composed of the preview and the label.
        VBox finalCard = new VBox(8);
        finalCard.setPrefWidth(200);
        finalCard.setAlignment(Pos.CENTER);
        finalCard.setPadding(new Insets(12));
        finalCard.setStyle(
                "-fx-background-color: -fx-custom-surface; " +
                        "-fx-background-radius: 8; " +
                        "-fx-border-color: -fx-custom-border; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 8; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 4, 0, 0, 2);"
        );
        finalCard.getChildren().addAll(preview, themeNameLabel);

        // Apply a distinct border and shadow on mouse hover.
        finalCard.setOnMouseEntered(e -> {
            finalCard.setStyle(
                    "-fx-background-color: -fx-custom-surface; " +
                            "-fx-background-radius: 8; " +
                            "-fx-border-color: -fx-custom-accent; " +
                            "-fx-border-radius: 8; " +
                            "-fx-cursor: hand; " +
                            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 8, 0, 0, 4);"
            );
        });

        // Restore the default border and shadow when the mouse exits.
        finalCard.setOnMouseExited(e -> {
            finalCard.setStyle(
                    "-fx-background-color: -fx-custom-surface; " +
                            "-fx-background-radius: 8; " +
                            "-fx-border-color: -fx-custom-border; " +
                            "-fx-border-radius: 8; " +
                            "-fx-cursor: hand; " +
                            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 4, 0, 0, 2);"
            );
        });

        // Apply the new theme when the card is clicked.
        finalCard.setOnMouseClicked(e -> this.themeService.applyTheme(theme.cssFileName()));

        return finalCard;
    }

    /**
     * Parses a color string (hex or rgba) into a JavaFX Color object.
     * @param colorStr The CSS color string.
     * @return A JavaFX Color object.
     */
    private Color parseColor(String colorStr) {
        try {
            if (colorStr.startsWith("rgba")) {
                // Handle rgba color format (e.g., rgba(255, 255, 255, 0.5))
                String values = colorStr.substring(colorStr.indexOf('(') + 1, colorStr.indexOf(')'));
                String[] parts = values.split(",");
                if (parts.length >= 3) {
                    double r = Double.parseDouble(parts[0].trim()) / 255.0;
                    double g = Double.parseDouble(parts[1].trim()) / 255.0;
                    double b = Double.parseDouble(parts[2].trim()) / 255.0;
                    double a = parts.length > 3 ? Double.parseDouble(parts[3].trim()) : 1.0;
                    return new Color(r, g, b, a);
                }
            }
            // Assume hex color format
            return Color.web(colorStr);
        } catch (Exception e) {
            System.err.println("[SettingsView] Error parsing color: " + colorStr);
            return Color.GRAY;
        }
    }
}