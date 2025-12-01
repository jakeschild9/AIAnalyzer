package edu.missouristate.aianalyzer.model.database;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "image_meta")
public class ImageMeta {

    @Id
    private String path;

    private Integer width;
    private Integer height;
    private Long exifTakenUnix;
    private String cameraMake;
    private String cameraModel;
}
