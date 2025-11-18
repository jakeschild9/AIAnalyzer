package edu.missouristate.aianalyzer.service.photos;

import edu.missouristate.aianalyzer.repository.database.FileRecordRepository;
import edu.missouristate.aianalyzer.repository.database.LabelHistoryRepository;
import org.im4java.core.IM4JavaException;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static edu.missouristate.aianalyzer.utility.ai.ReadImageUtil.convertImageToJpg;
import static org.apache.commons.io.FilenameUtils.getExtension;

@Service
@lombok.RequiredArgsConstructor
public class FindDuplicatesService {

    ArrayList<String> photos = new ArrayList<>();

    public FindDuplicatesService(FileRecordRepository fileRecordRepository, LabelHistoryRepository labelHistoryRepository) {

    }

    public Boolean compareColors(String color1, String color2) {
        String[] color1RGB = color1.split(",");
        String[] color2RGB = color2.split(",");

        int r1 = Integer.parseInt(color1RGB[0]);
        int g1 = Integer.parseInt(color1RGB[1]);
        int b1 = Integer.parseInt(color1RGB[2]);

        int r2 = Integer.parseInt(color2RGB[0]);
        int g2 = Integer.parseInt(color2RGB[1]);
        int b2 = Integer.parseInt(color2RGB[2]);

        // See if photo color averages are within 1 for a near duplicate
        return (r1 == r2 || r1 + 1 == r2 || r1 - 1 == r2) && (g1 == g2 || g1 + 1 == g2 || g1 - 1 == g2) && (b1 == b2 || b1 + 1 == b2 || b1 - 1 == b2);
    }

    public String calculateColor(String path) {
        BufferedImage image;
        String ext = getExtension(path);

        long totalRed = 0;
        long totalGreen = 0;
        long totalBlue = 0;
        int pixelCount = 0;

        int avgRed;
        int avgGreen;
        int avgBlue;

        try {
            if (ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("jpeg")) {
                image = ImageIO.read(new File(path));
            } else {
                image = ImageIO.read(convertImageToJpg(path));
            }

            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int pixel = image.getRGB(x, y);

                    int red = (pixel >> 16) & 0xFF;
                    int green = (pixel >> 8) & 0xFF;
                    int blue = pixel & 0xFF;

                    totalRed += red;
                    totalGreen += green;
                    totalBlue += blue;
                    pixelCount++;
                }
            }

            avgRed = (int) (totalRed / pixelCount);
            avgGreen = (int) (totalGreen / pixelCount);
            avgBlue = (int) (totalBlue / pixelCount);
        } catch (IOException | InterruptedException | IM4JavaException e) {
            throw new RuntimeException(e);
        }

        return avgRed + "," + avgGreen + "," + avgBlue;
    }

}
