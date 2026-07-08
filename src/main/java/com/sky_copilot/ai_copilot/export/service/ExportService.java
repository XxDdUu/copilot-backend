package com.sky_copilot.ai_copilot.export.service;

import com.sky_copilot.ai_copilot.export.dto.ExportRequest;
import com.sky_copilot.ai_copilot.export.dto.ExportResponse;
import com.sky_copilot.ai_copilot.export.format.ExportFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Service for handling document export operations
 * Supports multiple export formats (PDF, DOCX, HTML)
 */
@Service
public class ExportService {

    @Autowired
    private PdfExporter pdfExporter;

    @Value("${export.storage.path:./exports}")
    private String exportStoragePath;

    @Value("${export.download.url:http://localhost:8080/api/exports/download}")
    private String downloadBaseUrl;

    /**
     * Exports a document in the specified format
     *
     * @param documentId ID of the document to export
     * @param format Export format (PDF, DOCX, HTML)
     * @param exportRequest Export request containing document data
     * @return ExportResponse with download URL and file metadata
     */
    public ExportResponse exportDocument(Long documentId, ExportFormat format, ExportRequest exportRequest) {
        byte[] fileContent;
        String fileName;

        switch (format) {
            case PDF:
                fileContent = pdfExporter.exportToPdf(
                        exportRequest.title(),
                        exportRequest.content(),
                        exportRequest.author(),
                        exportRequest.description()
                );
                fileName = pdfExporter.generateFileName(exportRequest.title());
                break;

            case DOCX:
                fileContent = exportToDocx(exportRequest);
                fileName = generateFileName(exportRequest.title(), format);
                break;

            case HTML:
                fileContent = exportToHtml(exportRequest);
                fileName = generateFileName(exportRequest.title(), format);
                break;

            default:
                throw new IllegalArgumentException("Unsupported export format: " + format);
        }

        // Save file to storage
        String fileId = UUID.randomUUID().toString();
        saveExportFile(fileId, fileName, fileContent);

        // Generate download URL
        String downloadUrl = generateDownloadUrl(fileId, fileName);

        return new ExportResponse(downloadUrl, fileName, format.getContentType());
    }

    /**
     * Builds a simple PDF document from plain text content.
     *
     * @param title Document title
     * @param content Document text content
     * @return Byte array containing the generated PDF document
     */
    public byte[] buildPdfFromText(String title, String content) {
        return pdfExporter.exportToPdf(title, content, "System", "Generated from document text");
    }

    /**
     * Exports document content to DOCX format
     *
     * @param exportRequest Export request data
     * @return Byte array containing DOCX file
     */
    private byte[] exportToDocx(ExportRequest exportRequest) {
        // TODO: Implement DOCX export using Apache POI
        // Example: Use org.apache.poi.xwpf.usermodel.XWPFDocument
        String content = formatDocumentContent(exportRequest);
        return content.getBytes();
    }

    /**
     * Exports document content to HTML format
     *
     * @param exportRequest Export request data
     * @return Byte array containing HTML file
     */
    private byte[] exportToHtml(ExportRequest exportRequest) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"en\">\n");
        html.append("<head>\n");
        html.append("  <meta charset=\"UTF-8\">\n");
        html.append("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("  <title>").append(escapeHtml(exportRequest.title())).append("</title>\n");
        html.append("  <style>\n");
        html.append("    body { font-family: Arial, sans-serif; margin: 20px; line-height: 1.6; }\n");
        html.append("    .header { border-bottom: 2px solid #333; padding-bottom: 10px; margin-bottom: 20px; }\n");
        html.append("    .metadata { color: #666; font-size: 0.9em; margin-bottom: 20px; }\n");
        html.append("    .content { white-space: pre-wrap; word-wrap: break-word; }\n");
        html.append("  </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("  <div class=\"header\">\n");
        html.append("    <h1>").append(escapeHtml(exportRequest.title())).append("</h1>\n");
        html.append("  </div>\n");
        html.append("  <div class=\"metadata\">\n");
        html.append("    <p><strong>Author:</strong> ").append(escapeHtml(exportRequest.author())).append("</p>\n");
        html.append("    <p><strong>Description:</strong> ").append(escapeHtml(exportRequest.description())).append("</p>\n");
        html.append("  </div>\n");
        html.append("  <div class=\"content\">\n");
        html.append(escapeHtml(exportRequest.content())).append("\n");
        html.append("  </div>\n");
        html.append("</body>\n");
        html.append("</html>\n");

        return html.toString().getBytes();
    }

    /**
     * Saves exported file to storage
     *
     * @param fileId Unique file identifier
     * @param fileName Name of the file
     * @param fileContent File content as byte array
     */
    private void saveExportFile(String fileId, String fileName, byte[] fileContent) {
        try {
            Path storagePath = Paths.get(exportStoragePath, fileId);
            Files.createDirectories(storagePath.getParent());
            Files.write(storagePath, fileContent);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save export file: " + e.getMessage(), e);
        }
    }

    /**
     * Generates a download URL for the exported file
     *
     * @param fileId Unique file identifier
     * @param fileName Name of the file
     * @return Download URL
     */
    private String generateDownloadUrl(String fileId, String fileName) {
        return downloadBaseUrl + "/" + fileId + "/" + fileName;
    }

    /**
     * Generates a filename with proper extension
     *
     * @param title Document title
     * @param format Export format
     * @return Filename with extension
     */
    private String generateFileName(String title, ExportFormat format) {
        String sanitized = title.replaceAll("[^a-zA-Z0-9._-]", "_");
        return sanitized + "_" + System.currentTimeMillis() + format.getFileExtension();
    }

    /**
     * Formats document content for export
     *
     * @param exportRequest Export request data
     * @return Formatted content
     */
    private String formatDocumentContent(ExportRequest exportRequest) {
        return String.format(
                "Title: %s\nAuthor: %s\nDescription: %s\n\n%s",
                exportRequest.title(),
                exportRequest.author(),
                exportRequest.description(),
                exportRequest.content()
        );
    }

    /**
     * Escapes HTML special characters
     *
     * @param text Text to escape
     * @return Escaped text
     */
    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}
