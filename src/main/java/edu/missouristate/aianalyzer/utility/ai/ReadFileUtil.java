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

import static edu.missouristate.aianalyzer.utility.ai.ReadImageUtil.changeExtension;
import static edu.missouristate.aianalyzer.utility.ai.UploadFileUtil.uploadObject;

@Service
public class ReadFileUtil {

    /**
     * Reads a file as a string by automatically selecting
     * the appropriate extraction method based on file type.
     *
     * @param filePath the path to the file to read
     * @param fileType the type of the file (e.g., "pdf", "docx", "txt")
     * @return the full text content of the file
     * @throws IOException if the file type is unsupported or cannot be read
     */
    public static String readFileAsString(Path filePath, String fileType) throws IOException {
        Path path = Paths.get(filePath.toUri());

        return switch (fileType.toLowerCase()) {
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
    }

    public static String readDocumentType(String type) throws IOException {
        return switch (type.toLowerCase()) {
            case "txt", "md", "csv", "json", "sql" -> "text/plain";
            case "doc", "docx", "xls", "xlsx", "ppt", "pptx" -> "text/plain";
            case "pdf" -> "application/pdf";

            default -> throw new IllegalArgumentException("Unknown document type: " + type);
        };
    }

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
        }
    }

    /**
     * Reads plain text-based files into a string.
     *
     * @param filePath the path to a text, CSV, or JSON file
     * @return the file content as a string
     * @throws IOException if the file cannot be read
     */
    private static String readFileAsString(Path filePath) throws IOException {
        return Files.readString(filePath);
    }

    /**
     * Extracts text from Microsoft Word documents (.docx format).
     *
     * @param filePath the path to a DOCX file
     * @return extracted text from the DOCX document
     * @throws IOException if the file cannot be opened or parsed
     */
    private static String readDocxAsString(Path filePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath.toFile());
             XWPFDocument doc = new XWPFDocument(fis);
             XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
            return extractor.getText();
        }
    }

    /**
     * Extracts text from legacy Microsoft Word documents (.doc format).
     *
     * @param filePath the path to a DOC file
     * @return extracted text from the DOC document
     * @throws IOException if the file cannot be opened or parsed
     */
    private static String readDocAsString(Path filePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath.toFile());
             HWPFDocument doc = new HWPFDocument(fis);
             WordExtractor extractor = new WordExtractor(doc)) {
            return extractor.getText();
        }
    }

    /**
     * Extracts text content from Excel files (.xls or .xlsx).
     * Iterates through all sheets, rows, and cells, preserving tabular structure with tabs and newlines.
     *
     * @param filePath the path to the Excel file
     * @return extracted cell data as a formatted string
     * @throws IOException if the Excel file cannot be read or parsed
     */
    public static String getExcelDataAsString(String filePath) throws IOException {
        StringBuilder sb = new StringBuilder();
        FileInputStream fis = new FileInputStream(filePath);
        Workbook workbook = WorkbookFactory.create(fis);

        // Iterate through each sheet
        for (Sheet sheet : workbook) {
            // Iterate through each row in the sheet
            for (Row row : sheet) {
                // Iterate through each cell in the row
                for (Cell cell : row) {
                    // Get cell value as a string, handling different cell types
                    switch (cell.getCellType()) {
                        case STRING:
                            sb.append(cell.getStringCellValue());
                            break;
                        case NUMERIC:
                            if (DateUtil.isCellDateFormatted(cell)) {
                                sb.append(cell.getDateCellValue());
                            } else {
                                sb.append(cell.getNumericCellValue());
                            }
                            break;
                        case BOOLEAN:
                            sb.append(cell.getBooleanCellValue());
                            break;
                        case FORMULA:
                            // Evaluate formula to get its result as a string
                            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
                            CellValue cellValue = evaluator.evaluate(cell);
                            sb.append(cellValue.getStringValue());
                            break;
                        case BLANK:
                            sb.append(""); // Or handle as desired
                            break;
                        default:
                            sb.append(cell.toString()); // Fallback for other types
                    }
                    sb.append("\t"); // Use tab as a delimiter between cells
                }
                sb.append("\n"); // Newline after each row
            }
        }

        workbook.close();
        fis.close();
        return sb.toString();
    }

    /**
     * Extracts text from PowerPoint presentations (.pptx or .ppt).
     * Reads text from all slides and text boxes, concatenating content with line breaks.
     *
     * @param filePath the path to the PowerPoint file
     * @return extracted text from all slides
     * @throws IOException if the PowerPoint file cannot be read
     */
    public static String getPptDataAsString(Path filePath) throws IOException {
        StringBuilder textBuilder = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(String.valueOf(filePath));
             XMLSlideShow ppt = new XMLSlideShow(fis)) {

            for (XSLFSlide slide : ppt.getSlides()) {
                for (XSLFShape shape : slide.getShapes()) {
                    if (shape instanceof XSLFTextShape) {
                        XSLFTextShape textShape = (XSLFTextShape) shape;
                        String text = textShape.getText();
                        if (text != null && !text.trim().isEmpty()) {
                            textBuilder.append(text).append("\n"); // Add new line for separation
                        }
                    }
                    // You might need to handle other shape types like tables if they contain text
                }
            }
        }
        return textBuilder.toString().trim(); // Remove trailing newlines
    }

    /**
     * Extracts text from PDF documents.
     * Uses Apache PDFBox to parse text content from each page.
     *
     * @param filePath the path to the PDF file
     * @return the extracted text from the PDF
     * @throws IOException if the file cannot be opened or parsed
     */
    private static String readPdfAsString(Path filePath) throws IOException {
        try (var inputStream = Files.newInputStream(filePath);
             var document = PDDocument.load(inputStream)) {

            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    /**
     * Reads an SQL file and returns its full contents as a string.
     * Uses a buffered reader to read the file line by line and preserve formatting.
     *
     * @param filePath the path to the SQL file
     * @return the complete SQL script as a string
     * @throws IOException if the file cannot be opened or read
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
    public static String readJsonAsString(Path filePath) throws IOException {
        return Files.readString(filePath, StandardCharsets.UTF_8);
    }

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

    public static String readXmlAsString(Path filePath) throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setIgnoringComments(true);
            factory.setIgnoringElementContentWhitespace(true);
            factory.setNamespaceAware(true);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new FileReader(filePath.toFile())));

            // Convert XML back to text for display
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

    public static String readHtmlAsString(Path filePath) throws IOException {
        String html = Files.readString(filePath, StandardCharsets.UTF_8);
        return Jsoup.parse(html).text(); // Extracts visible text content
    }
}
