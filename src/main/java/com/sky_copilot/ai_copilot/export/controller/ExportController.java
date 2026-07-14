package com.sky_copilot.ai_copilot.export.controller;

import com.sky_copilot.ai_copilot.export.dto.ExportRequest;
import com.sky_copilot.ai_copilot.export.dto.ExportResponse;
import com.sky_copilot.ai_copilot.export.dto.DocumentTextResponse;
import com.sky_copilot.ai_copilot.export.format.ExportFormat;
import com.sky_copilot.ai_copilot.export.service.ExportService;
import com.sky_copilot.ai_copilot.export.service.DocumentTextService;
import com.sky_copilot.ai_copilot.document.repository.DocumentRepository;
import com.sky_copilot.ai_copilot.document.entity.Document;
import com.sky_copilot.ai_copilot.config.MinioProperties;
import org.springframework.http.ContentDisposition;
import java.nio.charset.StandardCharsets;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import io.minio.GetObjectArgs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.sky_copilot.ai_copilot.export.service.DocumentTextExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for document export operations
 * Handles export requests in multiple formats (PDF, DOCX, HTML)
 */
@RequiredArgsConstructor
@RestController
@RequestMapping({"/api/documents", "/documents"})
public class ExportController {

    private static final Logger logger = LoggerFactory.getLogger(ExportController.class);

    @Autowired
    private ExportService exportService;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinioProperties minioProperties;

    @Autowired
    private final DocumentTextExtractor documentTextExtractor;

    @Autowired
    private DocumentTextService documentTextService;
    /**
     * Exports a document in the specified format
     *
     * @param documentId ID of the document to export
     * @param format Export format (PDF, DOCX, HTML)
     * @return ExportResponse containing download URL and file metadata
     */
    @PostMapping("/{documentId}/export")
    public ResponseEntity<ExportResponse> export(
            @PathVariable Long documentId,
            @RequestParam ExportFormat format) {

        ExportResponse response = exportService.exportDocument(documentId, format);
        return ResponseEntity.ok(response);
    }

    /**
     * Returns a PDF representation of the document for browser viewing.
     *
     * @param id Document ID
     * @return PDF bytes with application/pdf content type
     */
    @GetMapping("/{id}/content")
    public ResponseEntity<byte[]> showDocumentContent(@PathVariable("id") Long id) {
        try {
            logger.info("Retrieving PDF content for document ID: {}", id);

            Document document = documentRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.warn("Document not found: {}", id);
                        return new IllegalArgumentException("Document not found: " + id);
                    });

            byte[] originalContent = retrieveDocumentFromMinIO(document.getObjectKey());
            String textContent = new String(originalContent, StandardCharsets.UTF_8);
            byte[] pdfContent = exportService.buildPdfFromText(document.getFileName(), textContent);

            String pdfFileName = buildPdfFileName(document.getFileName());
            ContentDisposition disposition = ContentDisposition.inline()
                    .filename(pdfFileName, StandardCharsets.UTF_8)
                    .build();

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                    .body(pdfContent);

        } catch (IllegalArgumentException e) {
            logger.error("Document not found: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Error generating PDF for document {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Returns the document text/Markdown content for AI consumption.
     *
     * @param id Document ID
     * @return Text content as markdown or plain text
     */
    @GetMapping("/{id}/text")
    public ResponseEntity<DocumentTextResponse> showDocumentText(
            @PathVariable Long id) {

        try {
            DocumentTextResponse response = documentTextService.getDocumentText(id);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.error("Document not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            logger.error("Error retrieving text for document {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Retrieves raw document bytes from MinIO storage
     *
     * @param objectKey Object key in MinIO bucket
     * @return Document content as byte array
     * @throws RuntimeException if retrieval fails
     */
    private byte[] retrieveDocumentFromMinIO(String objectKey) {
        try {
            logger.debug("Fetching object from MinIO: bucket={}, objectKey={}",
                    minioProperties.getBucket(), objectKey);

            try (GetObjectResponse response = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .object(objectKey)
                            .build())) {

                byte[] content = response.readAllBytes();
                logger.debug("Successfully read {} bytes from MinIO", content.length);
                return content;
            }
        } catch (Exception e) {
            logger.error("Failed to retrieve document from MinIO (objectKey={}): {}",
                    objectKey, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve document from MinIO: " + e.getMessage(), e);
        }
    }

    private boolean isMarkdownDocument(Document document) {
        String fileName = document.getFileName();
        String contentType = document.getContentType();
        return (fileName != null && fileName.toLowerCase().endsWith(".md"))
                || (contentType != null && contentType.toLowerCase().contains("markdown"));
    }

    private String buildPdfFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "document.pdf";
        }
        int lastDot = fileName.lastIndexOf('.');
        String baseName = lastDot > 0 ? fileName.substring(0, lastDot) : fileName;
        return baseName + ".pdf";
    }

    /**
     * Lists all supported export formats
     *
     * @return Array of supported export formats
     */
    @GetMapping("/export/formats")
    public ResponseEntity<ExportFormat[]> listExportFormats() {
        return ResponseEntity.ok(ExportFormat.values());
    }
}
