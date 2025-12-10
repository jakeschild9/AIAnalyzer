package edu.missouristate.aianalyzer.service.database;

import edu.missouristate.aianalyzer.model.database.PreferenceAudit;
import edu.missouristate.aianalyzer.model.database.UserPreference;
import edu.missouristate.aianalyzer.repository.database.PreferenceAuditRepository;
import edu.missouristate.aianalyzer.repository.database.UserPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.function.Consumer;

<<<<<<< HEAD
/**
 * Service to read and write preferences of the user.
 * Uses getters for STRING, BOOl, INTS and JSON values.
 */
=======
>>>>>>> clean-feature-branch
@Service
@RequiredArgsConstructor
public class PreferenceService {

    private final UserPreferenceRepository prefsRepo;
    private final PreferenceAuditRepository auditRepo;
    private final ApplicationEventPublisher publisher;

    private static final String DEFAULT_USER = "local";

    // ------- READS
    public Optional<String>  getString(String ns, String key){ return get(ns,key).map(UserPreference::getValueString); }
    public Optional<Boolean> getBool  (String ns, String key){ return get(ns,key).map(UserPreference::getValueBool); }
    public Optional<Integer> getInt   (String ns, String key){ return get(ns,key).map(UserPreference::getValueInt); }
    public Optional<Double>  getDouble(String ns, String key){ return get(ns,key).map(UserPreference::getValueDouble); }
    public Optional<String>  getJson  (String ns, String key){ return get(ns,key).map(UserPreference::getJsonValue); }

    private Optional<UserPreference> get(String ns, String key) {
        return prefsRepo.findByUserIdAndNamespaceAndKey(DEFAULT_USER, ns, key);
    }

    // ------- WRITES
    @Transactional public void setString(String ns, String key, String v, String src){ upsert(ns,key,"STRING", p->p.setValueString(v), src, v); }
    @Transactional public void setBool  (String ns, String key, boolean v,String src){ upsert(ns,key,"BOOL",   p->p.setValueBool(v),   src, String.valueOf(v)); }
    @Transactional public void setInt   (String ns, String key, int v,   String src){ upsert(ns,key,"INT",    p->p.setValueInt(v),    src, String.valueOf(v)); }
    @Transactional public void setDouble(String ns, String key, double v,String src){ upsert(ns,key,"DOUBLE", p->p.setValueDouble(v), src, String.valueOf(v)); }
    @Transactional public void setJson  (String ns, String key, String j,String src){ upsert(ns,key,"JSON",   p->p.setJsonValue(j),   src, j); }

    private void upsert(String ns, String key, String type,
                        Consumer<UserPreference> setter,
                        String source, String snapshotValue) {

        var pref = prefsRepo.findByUserIdAndNamespaceAndKey(DEFAULT_USER, ns, key)
                .orElseGet(() -> UserPreference.builder()
                        .userId(DEFAULT_USER).namespace(ns).key(key).valueType(type).build());

        pref.setValueType(type);
        setter.accept(pref);
        pref.setUpdatedBy(source);
        pref.setUpdatedAt(Instant.now());
        prefsRepo.save(pref);

        // write audit row
        auditRepo.save(PreferenceAudit.builder()
                .userId(DEFAULT_USER)
                .namespace(ns)
                .key(key)
                .valueSnapshot(snapshotValue)
                .source(source)
                .createdAt(Instant.now())
                .build());

        // notify listeners
        publisher.publishEvent(new PreferenceChangedEvent(this, DEFAULT_USER, ns, key));
    }
}

