package com.sky_copilot.ai_copilot.export.service;

import com.sky_copilot.ai_copilot.document.entity.Document;
import com.sky_copilot.ai_copilot.document.repository.DocumentRepository;
import com.sky_copilot.ai_copilot.export.dto.DocumentTextResponse;
import com.sky_copilot.ai_copilot.export.dto.ExportRequest;
import com.sky_copilot.ai_copilot.export.dto.ExportResponse;
import com.sky_copilot.ai_copilot.export.format.ExportFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExportService {

    private final PdfExporter pdfExporter;
    private final DocumentTextService documentTextService;
    private final DocumentRepository documentRepository;

    @Value("${export.storage.path:./exports}")
    private String exportStoragePath;

    @Value("${export.download.url:http://localhost:8080/api/exports/download}")
    private String downloadBaseUrl;

    public ExportResponse exportDocument(Long documentId, ExportFormat format) {

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Document not found: " + documentId));

        DocumentTextResponse documentText =
                documentTextService.getDocumentText(documentId);

        String author = "System";
        if (document.getUploadedBy() != null) {
            author = document.getUploadedBy().getFullName();
        }

        ExportRequest request = new ExportRequest(
                document.getFileName(),
                documentText.getContent(),
                author,
                "Exported from Enterprise AI Copilot"
        );

        byte[] fileContent;
        String fileName;

        switch (format) {

            case PDF:
                fileContent = pdfExporter.exportToPdf(
                        request.title(),
                        request.content(),
                        request.author(),
                        request.description()
                );
                fileName = pdfExporter.generateFileName(request.title());
                break;

            case DOCX:
                fileContent = exportToDocx(request);
                fileName = generateFileName(request.title(), format);
                break;

            case HTML:
                fileContent = exportToHtml(request);
                fileName = generateFileName(request.title(), format);
                break;

            default:
                throw new IllegalArgumentException(
                        "Unsupported export format: " + format);
        }

        String fileId = UUID.randomUUID().toString();

        saveExportFile(fileId, fileName, fileContent);

        return new ExportResponse(
                generateDownloadUrl(fileId, fileName),
                fileName,
                format.getContentType()
        );
    }

    public byte[] buildPdfFromText(String title, String content) {
        return pdfExporter.exportToPdf(
                title,
                content,
                "System",
                "Generated from document text"
        );
    }

    private byte[] exportToDocx(ExportRequest request) {
        // TODO: Apache POI implementation
        String content = formatDocumentContent(request);
        return content.getBytes();
    }

    private byte[] exportToHtml(ExportRequest request) {

        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>\n")
                .append("<html lang=\"en\">\n")
                .append("<head>\n")
                .append("<meta charset=\"UTF-8\">\n")
                .append("<title>")
                .append(escapeHtml(request.title()))
                .append("</title>\n")
                .append("</head>\n")
                .append("<body>\n")
                .append("<h1>")
                .append(escapeHtml(request.title()))
                .append("</h1>\n")
                .append("<p><strong>Author:</strong> ")
                .append(escapeHtml(request.author()))
                .append("</p>\n")
                .append("<p><strong>Description:</strong> ")
                .append(escapeHtml(request.description()))
                .append("</p>\n")
                .append("<pre>")
                .append(escapeHtml(request.content()))
                .append("</pre>\n")
                .append("</body>\n")
                .append("</html>");

        return html.toString().getBytes();
    }

    private void saveExportFile(
            String fileId,
            String fileName,
            byte[] fileContent) {

        try {
            Path storagePath = Paths.get(exportStoragePath, fileId);

            Files.createDirectories(storagePath.getParent());

            Files.write(storagePath, fileContent);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to save export file",
                    e
            );
        }
    }

    private String generateDownloadUrl(
            String fileId,
            String fileName) {

        return downloadBaseUrl + "/" + fileId + "/" + fileName;
    }

    private String generateFileName(
            String title,
            ExportFormat format) {

        String sanitized =
                title.replaceAll("[^a-zA-Z0-9._-]", "_");

        return sanitized
                + "_"
                + System.currentTimeMillis()
                + format.getFileExtension();
    }

    private String formatDocumentContent(
            ExportRequest request) {

        return String.format(
                """
                Title: %s
                Author: %s
                Description: %s

                %s
                """,
                request.title(),
                request.author(),
                request.description(),
                request.content()
        );
    }

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