package edu.missouristate.aianalyzer.utility.ai;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.yaml.snakeyaml.Yaml;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.charset.StandardCharsets;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

<<<<<<< HEAD
import static edu.missouristate.aianalyzer.utility.ai.ReadImageUtil.changeExtension;
import static edu.missouristate.aianalyzer.utility.ai.UploadFileUtil.uploadObject;

@Service
public class ReadFileUtil {
=======
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Utility class for reading and processing various file types.
 * Supports conversion of documents, images, and other file formats to plain text.
 */
@Component
@RequiredArgsConstructor
public class ReadFileUtil {
    private final UploadFileUtil uploadFileUtil;
>>>>>>> clean-feature-branch

    /**
     * Reads a file as a string based on its type.
     *
     * @param filePath the path to the file
     * @param fileType the type of the file (e.g., txt, docx, pdf)
     * @return the file content as a string
     * @throws IOException if the file type is unsupported or cannot be read
     */
    public static String readFileAsString(Path filePath, String fileType) throws IOException {
        Path path = Paths.get(filePath.toUri());

<<<<<<< HEAD
        return switch (fileType.toLowerCase()) {
=======
        // Validate file exists and is readable
        if (!Files.exists(path)) {
            throw new IOException("File does not exist: " + filePath);
        }

        if (!Files.isReadable(path)) {
            throw new IOException("File is not readable: " + filePath);
        }

        // Check file size - warn if empty
        long fileSize = Files.size(path);
        if (fileSize == 0) {
            throw new IOException("File is empty (0 bytes): " + filePath);
        }

        String content = switch (fileType.toLowerCase()) {
>>>>>>> clean-feature-branch
            case "txt", "md", "csv" -> readFileAsString(path);
            case "json" -> readJsonAsString(path);
            case "jsonl", "ndjson" -> readJsonlAsString(path);
            case "xml" -> readXmlAsString(path);
            case "yaml", "yml" -> readYamlAsString(path);
            case "html", "htm" -> readHtmlAsString(path);
            case "doc" -> readDocAsString(path);
            case "docx" -> readDocxAsString(path);
            case "xls", "xlsx" -> getExcelDataAsString(String.valueOf(filePath));
            case "ppt", "pptx" -> getPptDataAsString(path);
            case "pdf" -> readPdfAsString(path);
            case "sql" -> readSqlAsString(filePath);
            default -> throw new IOException("Unsupported file type: " + fileType);
        };
<<<<<<< HEAD
    }

=======

        // Final validation
        if (content == null || content.trim().isEmpty()) {
            throw new IOException("Extracted content is empty for file: " + filePath.getFileName());
        }

        return content;
    }


>>>>>>> clean-feature-branch
    /**
     * Returns the MIME type of a document based on its extension.
     *
     * @param type the file type
     * @return the MIME type string
     * @throws IOException if the document type is unknown
     */
    public static String readDocumentType(String type) throws IOException {
        return switch (type.toLowerCase()) {
            case "txt", "md", "csv", "json", "sql" -> "text/plain";
            case "doc", "docx", "xls", "xlsx", "ppt", "pptx" -> "text/plain";
            case "pdf" -> "application/pdf";
            default -> throw new IllegalArgumentException("Unknown document type: " + type);
        };
    }

    /**
     * Uploads a file by converting it to a temporary .txt file and sending it to storage.
     *
     * @param filePath the original file path
     * @param fileType the file type
     * @throws IOException if an I/O error occurs
     */
<<<<<<< HEAD
    public static void uploadFile(Path filePath, String fileType) throws IOException {
        if (!Files.exists(filePath)) {
            throw new FileNotFoundException("File not found: " + filePath);
        }

        String data = readFileAsString(filePath, fileType);

        try {
            File outputFile = changeExtension(new File(String.valueOf(filePath)), ".txt");
            Files.writeString(outputFile.toPath(), data);
            uploadObject("files" + outputFile, String.valueOf(outputFile));
            System.out.println("Temporary file created at: " + outputFile.toPath().toAbsolutePath());
        } catch (Exception e) {
            System.err.println("An error occurred while writing to the file: " + e.getMessage());
=======
    public void uploadFile(Path filePath, String fileType) throws IOException {
        // Read the file content as string
        String fileContent = readFileAsString(filePath, fileType);

        // Validate that content is not empty
        if (fileContent == null || fileContent.trim().isEmpty()) {
            throw new IOException("File content is empty or could not be extracted: " + filePath.getFileName());
        }

        // Create a temporary .txt file with the content
        String fileName = filePath.getFileName().toString();
        String txtFileName = fileName.replaceFirst("\\.[^.]+$", ".txt");

        Path tempDir = Files.createTempDirectory("ai-upload");
        Path tempFile = tempDir.resolve(txtFileName);

        try {
            // Write content to temporary txt file
            Files.writeString(tempFile, fileContent);

            // Upload to GCS with correct object name
            String objectName = "files/" + txtFileName;
            uploadFileUtil.uploadObject(objectName, tempFile.toString());

            System.out.println("Successfully uploaded " + fileName + " as " + objectName);
        } finally {
            // Clean up temporary files
            try {
                Files.deleteIfExists(tempFile);
                Files.deleteIfExists(tempDir);
            } catch (IOException e) {
                System.err.println("Warning: Could not delete temp files: " + e.getMessage());
            }
>>>>>>> clean-feature-branch
        }
    }

    /**
     * Reads a plain text file into a string.
     *
     * @param filePath the path to the file
     * @return the content of the file
     * @throws IOException if reading fails
     */
    private static String readFileAsString(Path filePath) throws IOException {
        return Files.readString(filePath);
    }

    /**
     * Extracts text from a DOCX file.
     */
    private static String readDocxAsString(Path filePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath.toFile());
             XWPFDocument doc = new XWPFDocument(fis);
             XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
            return extractor.getText();
        }
    }

    /**
     * Extracts text from a DOC file.
     */
    private static String readDocAsString(Path filePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath.toFile());
             HWPFDocument doc = new HWPFDocument(fis);
             WordExtractor extractor = new WordExtractor(doc)) {
            return extractor.getText();
        }
    }

    /**
     * Extracts text from an Excel file (.xls or .xlsx).
     */
    public static String getExcelDataAsString(String filePath) throws IOException {
        StringBuilder sb = new StringBuilder();
        FileInputStream fis = new FileInputStream(filePath);
        Workbook workbook = WorkbookFactory.create(fis);

        for (Sheet sheet : workbook) {
            for (Row row : sheet) {
                for (Cell cell : row) {
                    switch (cell.getCellType()) {
                        case STRING -> sb.append(cell.getStringCellValue());
                        case NUMERIC -> {
                            if (DateUtil.isCellDateFormatted(cell)) {
                                sb.append(cell.getDateCellValue());
                            } else {
                                sb.append(cell.getNumericCellValue());
                            }
                        }
                        case BOOLEAN -> sb.append(cell.getBooleanCellValue());
                        case FORMULA -> {
                            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
                            CellValue cellValue = evaluator.evaluate(cell);
                            sb.append(cellValue.getStringValue());
                        }
                        case BLANK -> sb.append("");
                        default -> sb.append(cell.toString());
                    }
                    sb.append("\t");
                }
                sb.append("\n");
            }
        }

        workbook.close();
        fis.close();
        return sb.toString();
    }

    /**
     * Extracts text from a PowerPoint file (.ppt or .pptx).
     */
    public static String getPptDataAsString(Path filePath) throws IOException {
        StringBuilder textBuilder = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(String.valueOf(filePath));
             XMLSlideShow ppt = new XMLSlideShow(fis)) {

            for (XSLFSlide slide : ppt.getSlides()) {
                for (XSLFShape shape : slide.getShapes()) {
                    if (shape instanceof XSLFTextShape textShape) {
                        String text = textShape.getText();
                        if (text != null && !text.trim().isEmpty()) {
                            textBuilder.append(text).append("\n");
                        }
                    }
                }
            }
        }
        return textBuilder.toString().trim();
    }

    /**
     * Extracts text from a PDF file using PDFBox.
<<<<<<< HEAD
=======
     * Returns an informative message if the PDF contains no extractable text.
>>>>>>> clean-feature-branch
     */
    private static String readPdfAsString(Path filePath) throws IOException {
        try (var inputStream = Files.newInputStream(filePath);
             var document = PDDocument.load(inputStream)) {

<<<<<<< HEAD
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
=======
            // Check if PDF has pages
            if (document.getNumberOfPages() == 0) {
                throw new IOException("PDF file has no pages: " + filePath.getFileName());
            }

            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            // Check if extracted text is meaningful
            if (text == null || text.trim().isEmpty()) {
                return "[PDF contains " + document.getNumberOfPages() +
                        " page(s) but no extractable text. This may be an image-based PDF that requires OCR.]";
            }

            return text;
        } catch (IOException e) {
            throw new IOException("Failed to read PDF file: " + e.getMessage(), e);
>>>>>>> clean-feature-branch
        }
    }

    /**
     * Reads an SQL file and returns the content as a string.
     */
    public static String readSqlAsString(Path filePath) throws IOException {
        StringBuilder sqlContent = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(String.valueOf(filePath)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sqlContent.append(line).append("\n");
            }
        }
        return sqlContent.toString();
    }

    /**
     * Reads a JSON file into a string.
     */
    public static String readJsonAsString(Path filePath) throws IOException {
        return Files.readString(filePath, StandardCharsets.UTF_8);
    }

    /**
     * Reads a JSONL/NDJSON file into a string.
     */
    public static String readJsonlAsString(Path filePath) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line.trim()).append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * Reads an XML file and returns its content as a string.
     */
    public static String readXmlAsString(Path filePath) throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setIgnoringComments(true);
            factory.setIgnoringElementContentWhitespace(true);
            factory.setNamespaceAware(true);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new FileReader(filePath.toFile())));

            StringWriter writer = new StringWriter();
            writeNode(document, "", writer);
            return writer.toString();
        } catch (SAXException | IOException | RuntimeException | javax.xml.parsers.ParserConfigurationException e) {
            throw new IOException("Failed to parse XML file: " + e.getMessage(), e);
        }
    }

    private static void writeNode(org.w3c.dom.Node node, String indent, Writer writer) throws IOException {
        writer.write(indent + "<" + node.getNodeName() + ">");
        if (node.hasChildNodes()) {
            writer.write("\n");
            for (int i = 0; i < node.getChildNodes().getLength(); i++) {
                org.w3c.dom.Node child = node.getChildNodes().item(i);
                if (child.getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
                    writer.write(indent + "  " + child.getTextContent().trim() + "\n");
                } else {
                    writeNode(child, indent + "  ", writer);
                }
            }
        }
        writer.write(indent + "</" + node.getNodeName() + ">\n");
    }

    /**
     * Reads a YAML file and converts its contents into a string representation.
     */
    public static String readYamlAsString(Path filePath) throws IOException {
        Yaml yaml = new Yaml();
        try (InputStream inputStream = Files.newInputStream(filePath)) {
            Iterable<Object> yamlObjects = yaml.loadAll(inputStream);
            StringBuilder sb = new StringBuilder();
            for (Object obj : yamlObjects) {
                if (obj instanceof Map<?, ?> map) {
                    sb.append(map.toString()).append("\n");
                } else {
                    sb.append(String.valueOf(obj)).append("\n");
                }
            }
            return sb.toString();
        }
    }

    /**
     * Reads an HTML file and extracts the visible text content.
     */
    public static String readHtmlAsString(Path filePath) throws IOException {
        String html = Files.readString(filePath, StandardCharsets.UTF_8);
        return Jsoup.parse(html).text();
    }
}
