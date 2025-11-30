package edu.missouristate.aianalyzer.service.photos;

import dev.brachtendorf.jimagehash.hash.Hash;
import dev.brachtendorf.jimagehash.hashAlgorithms.AverageHash;
import dev.brachtendorf.jimagehash.hashAlgorithms.HashingAlgorithm;
import edu.missouristate.aianalyzer.repository.database.FileRecordRepository;
import edu.missouristate.aianalyzer.repository.database.LabelHistoryRepository;
import org.im4java.core.IM4JavaException;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;

import static edu.missouristate.aianalyzer.utility.ai.ReadImageUtil.convertImageToJpg;
import static org.apache.commons.io.FilenameUtils.getExtension;

@Service
@lombok.RequiredArgsConstructor
public class FindDuplicatesService {

    public static BigInteger calculateImageHash(String path) throws IOException {
        BufferedImage image;
        String ext = getExtension(path);
        HashingAlgorithm hasher = new AverageHash(32);

        try {
            if (ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("jpeg")) {
                image = ImageIO.read(new File(path));
            } else {
                image = ImageIO.read(convertImageToJpg(path));
            }
        Hash hash = hasher.hash(image);

        return hash.getHashValue();
        } catch (IOException | InterruptedException | IM4JavaException e) {
            throw new RuntimeException(e);
        }
    }
}
