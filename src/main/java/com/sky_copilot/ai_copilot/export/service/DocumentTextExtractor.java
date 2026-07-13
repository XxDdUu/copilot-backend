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

    public record ExtractedText(String content, Integer pageCount) {
    }

    public String extract(byte[] content, String contentType) throws Exception {
        return extractWithPageCount(content, contentType).content();
    }

    public ExtractedText extractWithPageCount(byte[] content, String contentType) throws Exception {

        switch (contentType) {

            case "application/pdf":
                return extractPdf(content);

            case "text/plain":
            case "text/markdown":
                return new ExtractedText(new String(content, StandardCharsets.UTF_8), null);

            default:
                throw new UnsupportedOperationException(contentType);
        }
    }

    private ExtractedText extractPdf(byte[] content) throws IOException {

        try (PDDocument document = Loader.loadPDF(content)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return new ExtractedText(stripper.getText(document), document.getNumberOfPages());
        }
    }
}
