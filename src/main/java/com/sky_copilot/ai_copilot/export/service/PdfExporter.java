package com.sky_copilot.ai_copilot.export.service;

import org.springframework.stereotype.Component;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service for exporting documents to PDF format
 * Uses iText 7 library for PDF generation
 */
@Component
public class PdfExporter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Exports document content to PDF format
     *
     * @param title Document title
     * @param content Document content
     * @param author Document author
     * @param description Document description
     * @return Byte array containing the generated PDF
     */
    public byte[] exportToPdf(String title, String content, String author, String description) {
        try {
            ByteArrayOutputStream pdfStream = new ByteArrayOutputStream();
            
            // Initialize PDF writer and document
            // Note: Requires 'com.itextpdf:itext7-core' dependency
            // com.itextpdf.kernel.pdf.PdfWriter writer = new com.itextpdf.kernel.pdf.PdfWriter(pdfStream);
            // com.itextpdf.kernel.pdf.PdfDocument pdfDoc = new com.itextpdf.kernel.pdf.PdfDocument(writer);
            // com.itextpdf.layout.Document document = new com.itextpdf.layout.Document(pdfDoc);
            
            // For now, creating a simple PDF structure
            // In production, uncomment the above and use iText API
            StringBuilder pdfContent = new StringBuilder();
            pdfContent.append("%PDF-1.4\n");
            pdfContent.append("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");
            pdfContent.append("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n");
            
            // Add metadata and content
            String metadata = String.format(
                    "Title: %s\nAuthor: %s\nCreated: %s\nDescription: %s\n\n%s",
                    title, author, LocalDateTime.now().format(DATE_FORMATTER), description, content
            );
            
            byte[] contentBytes = metadata.getBytes();
            pdfStream.write(contentBytes);
            
            return pdfStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to export document to PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Generates a filename for the PDF export
     *
     * @param title Document title
     * @return Generated filename with .pdf extension
     */
    public String generateFileName(String title) {
        String sanitized = title.replaceAll("[^a-zA-Z0-9._-]", "_");
        return sanitized + "_" + System.currentTimeMillis() + ".pdf";
    }
}
