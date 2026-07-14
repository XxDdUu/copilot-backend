package com.sky_copilot.ai_copilot.export.service;

import com.sky_copilot.ai_copilot.document.entity.Document;
import com.sky_copilot.ai_copilot.document.repository.DocumentRepository;
import com.sky_copilot.ai_copilot.export.dto.DocumentTextResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocumentTextService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentTextService.class);

    private final DocumentRepository documentRepository;
    private final MinioDocumentService minioDocumentService;
    private final DocumentTextExtractor documentTextExtractor;

    public DocumentTextResponse getDocumentText(Long documentId) {

        logger.info("Retrieving text content for document ID: {}", documentId);

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> {
                    logger.warn("Document not found: {}", documentId);
                    return new IllegalArgumentException(
                            "Document not found: " + documentId);
                });

        try {

            byte[] content =
                    minioDocumentService.getDocument(document.getObjectKey());

            DocumentTextExtractor.ExtractedText extractedText =
                    documentTextExtractor.extractWithPageCount(
                            content,
                            document.getContentType());

            String textContent = extractedText.content();

            return DocumentTextResponse.builder()
                    .id(document.getId())
                    .name(document.getFileName())
                    .contentType(document.getContentType())
                    .size((long) content.length)
                    .characterCount(textContent.length())
                    .pageCount(extractedText.pageCount())
                    .content(textContent)
                    .build();

        } catch (Exception e) {

            logger.error(
                    "Failed to extract text from document {}",
                    documentId,
                    e
            );

            throw new RuntimeException(
                    "Failed to retrieve document text",
                    e
            );
        }
    }
}
