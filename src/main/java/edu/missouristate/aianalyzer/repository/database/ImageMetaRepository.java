package edu.missouristate.aianalyzer.repository.database;

import edu.missouristate.aianalyzer.model.database.ImageMeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageMetaRepository extends JpaRepository<ImageMeta, String> {
    // The primary key is a String (the path), so we use String here.
}
