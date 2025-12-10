package edu.missouristate.aianalyzer.ui.service;

<<<<<<< HEAD
import javafx.scene.Scene;
import org.springframework.stereotype.Service;

import java.net.URL;

@Service
public class ThemeService {

    private Scene mainScene;

    /**
     * Stores the main scene so we can apply stylesheets to it later.
     */
=======
import edu.missouristate.aianalyzer.service.database.PreferenceService;
import javafx.scene.Scene;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service responsible for managing and applying application themes (CSS stylesheets)
 * to the main JavaFX scene, and persisting the user's selected theme preference.
 */
@Service
public class ThemeService {

    private final PreferenceService preferenceService;
    private Scene mainScene;

    public ThemeService(PreferenceService preferenceService) {
        this.preferenceService = preferenceService;
    }

    // Stores the main scene so stylesheets can be applied to it later.
>>>>>>> clean-feature-branch
    public void setScene(Scene scene) {
        this.mainScene = scene;
    }

<<<<<<< HEAD
    /**
     * This is the exact same logic you had in StageInitializer, now moved here.
     */
    public void applyTheme(String themeName) {
        if (mainScene == null) return;
=======
    // Loads the saved theme preference from the database. Defaults to "dev-dark" if no preference is found.
    public String getSavedTheme() {
        return preferenceService.getString("ui", "theme")
                .orElse("dev-dark");
    }

    /**
     * Parses a theme CSS file and extracts color values defined as CSS variables.
     * This is used to display theme previews in the settings panel.
     *
     * @param themeName The theme file name (e.g., "dev-dark").
     * @return Map of CSS variable names to color values.
     */
    public Map<String, String> parseThemeColors(String themeName) {
        Map<String, String> colors = new HashMap<>();

        try {
            String themeCssPath = "/styles/themes/" + themeName + ".css";
            InputStream is = getClass().getResourceAsStream(themeCssPath);

            if (is == null) {
                System.err.println("[ThemeService] Could not find theme file: " + themeCssPath);
                return colors;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            // Regex to match CSS variables like: -fx-custom-background: #1e1e1e;
            Pattern pattern = Pattern.compile("(-fx-custom-[\\w-]+):\\s*([#\\w(),\\s.]+);");

            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line.trim());
                if (matcher.find()) {
                    String varName = matcher.group(1);
                    String value = matcher.group(2).trim();
                    colors.put(varName, value);
                }
            }

            reader.close();

        } catch (Exception e) {
            System.err.println("[ThemeService] Error parsing theme file: " + themeName);
            e.printStackTrace();
        }

        return colors;
    }

    // Applies the specified theme stylesheet to the main scene and saves the preference to the database.
    public void applyTheme(String themeName) {
        if (mainScene == null) {
            System.err.println("[ThemeService] ERROR: mainScene is null! Cannot apply theme.");
            return;
        }

        // Clear existing stylesheets before applying new ones.
>>>>>>> clean-feature-branch
        mainScene.getStylesheets().clear();

        String commonCssPath = "/styles/common.css";
        URL commonUrl = getClass().getResource(commonCssPath);
<<<<<<< HEAD
        if (commonUrl != null) {
            mainScene.getStylesheets().add(commonUrl.toExternalForm());
        } else {
            System.err.println("CRITICAL ERROR: Could not find common.css");
=======

        if (commonUrl != null) {
            // Load base common styling.
            try {
                mainScene.getStylesheets().add(commonUrl.toExternalForm());
            } catch (Exception e) {
                System.err.println("[ThemeService] ERROR loading common.css:");
                e.printStackTrace();
            }
        } else {
            System.err.println("[ThemeService] CRITICAL ERROR: Could not find common.css at: " + commonCssPath);
>>>>>>> clean-feature-branch
        }

        String themeCssPath = "/styles/themes/" + themeName + ".css";
        URL themeUrl = getClass().getResource(themeCssPath);
<<<<<<< HEAD
        if (themeUrl != null) {
            mainScene.getStylesheets().add(themeUrl.toExternalForm());
            System.out.println("Theme switched to: " + themeName);
        } else {
            System.err.println("Error: Could not find theme stylesheet: " + themeCssPath);
=======

        if (themeUrl != null) {
            // Load the specific theme stylesheet.
            try {
                mainScene.getStylesheets().add(themeUrl.toExternalForm());

                // Save the theme preference to the database
                saveThemePreference(themeName);

            } catch (Exception e) {
                System.err.println("[ThemeService] ERROR loading theme '" + themeName + "':");
                e.printStackTrace();
            }
        } else {
            System.err.println("[ThemeService] Error: Could not find theme stylesheet: " + themeCssPath);
        }
    }

    // Saves the theme preference to the database using the PreferenceService.
    private void saveThemePreference(String themeName) {
        try {
            preferenceService.setString("ui", "theme", themeName, "ThemeService");
        } catch (Exception e) {
            System.err.println("[ThemeService] ERROR saving theme preference:");
            e.printStackTrace();
>>>>>>> clean-feature-branch
        }
    }
}