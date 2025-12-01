package edu.missouristate.aianalyzer.ui.service;


import edu.missouristate.aianalyzer.service.database.PreferenceChangedEvent;
import edu.missouristate.aianalyzer.service.database.PreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ThemeManager {
    private final PreferenceService prefs;

    public void loadInitialTheme() {
        String theme = prefs.getString("ui","theme").orElse("system");
        applyTheme(theme);
    }

    @EventListener
    public void onPrefChange(PreferenceChangedEvent evt) {
        if ("ui".equals(evt.namespace) && "theme".equals(evt.key)) {
            prefs.getString("ui","theme").ifPresent(this::applyTheme);
        }
    }

    private void applyTheme(String theme) {
    }
}
