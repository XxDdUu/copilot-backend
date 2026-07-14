package com.sky_copilot.ai_copilot.export.service;

import com.sky_copilot.ai_copilot.document.entity.Document;
import com.sky_copilot.ai_copilot.document.repository.DocumentRepository;
import com.sky_copilot.ai_copilot.export.dto.DocumentTextResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentTextServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private MinioDocumentService minioDocumentService;

    @Mock
    private DocumentTextExtractor documentTextExtractor;

    @InjectMocks
    private DocumentTextService documentTextService;

    @Test
    void shouldReturnDocumentTextResponseForExistingDocument() throws Exception {
        Document document = Document.builder()
                .id(7L)
                .fileName("notes.md")
                .contentType("text/markdown")
                .objectKey("notes.md")
                .build();

        when(documentRepository.findById(7L)).thenReturn(Optional.of(document));
        when(minioDocumentService.getDocument("notes.md"))
                .thenReturn("# Notes\n\n- item".getBytes(StandardCharsets.UTF_8));
        when(documentTextExtractor.extractWithPageCount(
                "# Notes\n\n- item".getBytes(StandardCharsets.UTF_8),
                "text/markdown"
        )).thenReturn(new DocumentTextExtractor.ExtractedText("# Notes\n\n- item", 1));

        DocumentTextResponse response = documentTextService.getDocumentText(7L);

        assertEquals(7L, response.getId());
        assertEquals("notes.md", response.getName());
        assertEquals("text/markdown", response.getContentType());
        assertEquals("# Notes\n\n- item", response.getContent());
        assertEquals(1, response.getPageCount());
    }
}
