package edu.missouristate.aianalyzer.service.database;

import org.springframework.context.ApplicationEvent;

/**
 * Spring App event for a user preference change
 */
public class PreferenceChangedEvent extends ApplicationEvent {
    public final String userId, namespace, key;
    public PreferenceChangedEvent(Object src, String userId, String ns, String key) {
        super(src); this.userId = userId; this.namespace = ns; this.key = key;
    }
}

