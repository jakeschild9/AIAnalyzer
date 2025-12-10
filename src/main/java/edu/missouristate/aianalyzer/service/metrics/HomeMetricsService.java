package edu.missouristate.aianalyzer.service.metrics;

import edu.missouristate.aianalyzer.model.database.FileRecord;
import edu.missouristate.aianalyzer.repository.database.FileRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HomeMetricsService {

    private final FileRecordRepository fileRecordRepository;

    // Change this to a Map<Category, List<Extensions>> so its easier to query
    private static final Map<String, List<String>> CATEGORY_DEFINITIONS = new LinkedHashMap<>(); // Linked to preserve order

    static {
        CATEGORY_DEFINITIONS.put("Images", List.of("jpg", "jpeg", "png", "gif", "bmp", "webp", "svg", "tiff", "ico"));
        CATEGORY_DEFINITIONS.put("Videos", List.of("mp4", "mkv", "avi", "mov", "wmv", "flv", "webm", "m4v"));
        CATEGORY_DEFINITIONS.put("Documents", List.of("pdf", "doc", "docx", "txt", "rtf", "odt", "xls", "xlsx", "ppt", "pptx", "csv", "md"));
        CATEGORY_DEFINITIONS.put("Archives", List.of("zip", "rar", "7z", "tar", "gz", "iso", "cab"));
        CATEGORY_DEFINITIONS.put("Code", List.of("java", "py", "js", "ts", "html", "css", "c", "cpp", "h", "cs", "php", "json", "xml", "yaml", "yml", "sql", "sh", "bat"));
        CATEGORY_DEFINITIONS.put("Executables", List.of("exe", "msi", "dll", "app", "jar", "bin"));
        CATEGORY_DEFINITIONS.put("Audio", List.of("mp3", "wav", "flac", "aac", "ogg", "wma"));
        // "Others" is special case handled in logic
    }

    public Map<String, CategoryStats> getCategoryStats() {
        Map<String, CategoryStats> stats = new HashMap<>();
        CATEGORY_DEFINITIONS.keySet().forEach(cat -> stats.put(cat, new CategoryStats(cat, 0, 0)));
        stats.put("Others", new CategoryStats("Others", 0, 0));

        List<Object[]> rawData = fileRecordRepository.getStatsByExtension();

        // Create a reverse map for fast lookup
        Map<String, String> extToCat = new HashMap<>();
        CATEGORY_DEFINITIONS.forEach((cat, exts) -> exts.forEach(e -> extToCat.put(e, cat)));

        for (Object[] row : rawData) {
            String ext = (String) row[0];
            Long count = (Long) row[1];
            Long size = (Long) row[2];
            if (ext == null) ext = "unknown";
            String category = extToCat.getOrDefault(ext.toLowerCase(), "Others");
            stats.get(category).add(count, size == null ? 0 : size);
        }
        return stats;
    }

    public List<FileRecord> getFilesByCategory(String category) {
        if ("Others".equals(category)) {
            // "Others" is hard to query with an IN clause, simplification: return empty or implement specific logic
            return new ArrayList<>();
        }

        // Special handling for duplicate images category
        if ("Duplicate Images".equals(category)) {
            List<String> imageExts = CATEGORY_DEFINITIONS.getOrDefault("Images", List.of());
            if (imageExts.isEmpty()) {
                return new ArrayList<>();
            }
            return fileRecordRepository.findDuplicateImages(imageExts);
        }


        List<String> extensions = CATEGORY_DEFINITIONS.get(category);
        if (extensions == null || extensions.isEmpty()) return new ArrayList<>();

        return fileRecordRepository.findByExtensionIn(extensions);
    }

    public static class CategoryStats {
        public String name;
        public long count;
        public long sizeBytes;

        public CategoryStats(String name, long count, long sizeBytes) {
            this.name = name;
            this.count = count;
            this.sizeBytes = sizeBytes;
        }

        public void add(long count, long size) {
            this.count += count;
            this.sizeBytes += size;
        }
    }
}