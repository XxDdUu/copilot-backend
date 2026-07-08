package com.sky_copilot.ai_copilot.export.service;

import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
@Service
public class DocumentTextExtractor {

    public String extract(byte[] content, String contentType) throws Exception {

        switch (contentType) {

            case "application/pdf":
                return extractPdf(content);

            case "text/plain":
            case "text/markdown":
                return new String(content, StandardCharsets.UTF_8);

            default:
                throw new UnsupportedOperationException(contentType);
        }
    }

    private String extractPdf(byte[] content) throws IOException {

        try (PDDocument document = Loader.loadPDF(content)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }
}