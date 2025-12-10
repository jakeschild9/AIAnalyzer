package edu.missouristate.aianalyzer.repository.database;

import edu.missouristate.aianalyzer.model.database.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

<<<<<<< HEAD
/**
 * Provides methods for fetching preferences by user namespace or key, and is used by PreferenceService
 */
=======
>>>>>>> clean-feature-branch
public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {
    Optional<UserPreference> findByUserIdAndNamespaceAndKey(String userId, String namespace, String key);
    List<UserPreference> findByUserIdAndNamespace(String userId, String namespace);
}
