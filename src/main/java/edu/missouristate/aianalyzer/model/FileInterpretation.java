package edu.missouristate.aianalyzer.model;

import java.util.Set;

// Class to hold AI results
public class FileInterpretation {
    public enum SearchType {
        ACTIVE,
        PASSIVE
    }

    public static final Set<String> SUPPORTED_FILE_TYPES = Set.of(
            "txt", "md", "csv", "sql", "json", "jsonl", "ndjson", "xml", "yaml", "yml", "html", "htm",
            "doc", "docx", "xls", "xlsx", "ppt", "pptx", "pdf"
    );

    public static final Set<String> VIRUS_FILE_TYPES = Set.of(
            "exe", "bat", "cmd", "com", "msi", "vbs", "vbe", "scr", "pif", "jar", "wsf",
            "js", "jse", "ps1", "psm1", "msp", "hta", "cpl", "gadget",
            "docm", "dotm", "xlsm", "xltm", "pptm", "ppam", "potm", "doc", "xls", "ppt",
            "zip", "rar", "7z", "tar", "gz", "iso",
            "apk", "dmg", "bin", "vxd"
    );

    public static final Set<String> IMAGE_TYPES = Set.of(
            // Raster formats
            "png", "apng", "jpg", "jpeg", "jpe", "jfif", "jxl", "gif", "bmp", "dib",
            "tif", "tiff", "webp", "avif", "heic", "heif", "ico", "cur", "psd", "exr",
            "hdr", "j2k", "jp2", "jpf", "jpx", "jpm", "mj2", "dds", "tga", "icb", "vda",
            "vst", "sgi", "rgb", "rgba", "pcx", "pgm", "ppm", "pbm", "pnm", "xwd",
            "sun", "ras", "fax",

            // Camera RAW formats (ImageMagick reads through dcraw/libraw)
            "dng", "cr2", "cr3", "nef", "nrw", "arw", "srf", "sr2",
            "orf", "rw2", "raf", "pef", "kdc",

            // Vector formats (still images)
            "svg", "svgz",
            "wmf", "emf",
            "pdf",

            "xbm", "xpm"
    );



    public static final Set<String> SUPPORTED_IMAGE_TYPES = Set.of(
            "jpg","jpeg","png","webp"
    );

    public enum FileType {
        // Image Types
        JPEG("image/jpeg"),
        JPG("image/jpeg"),
        PNG("image/png"),
        WEBP("image/webp"),

        // Document/Text Types
        PDF("application/pdf"),
        TXT("text/plain");

        private final String type;

        FileType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }
}
