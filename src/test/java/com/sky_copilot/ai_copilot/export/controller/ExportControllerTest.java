package com.sky_copilot.ai_copilot.export.controller;

import com.sky_copilot.ai_copilot.config.MinioProperties;
import com.sky_copilot.ai_copilot.document.entity.Document;
import com.sky_copilot.ai_copilot.document.repository.DocumentRepository;
import com.sky_copilot.ai_copilot.export.service.ExportService;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExportControllerTest {

    @Mock
    private ExportService exportService;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private MinioClient minioClient;

    @Mock
    private MinioProperties minioProperties;

    @InjectMocks
    private ExportController exportController;

    @BeforeEach
    void setUp() {
        when(minioProperties.getBucket()).thenReturn("documents");
    }

    @Test
    void shouldReturnPdfBytesForBrowserContentEndpoint() throws Exception {
        Document document = Document.builder()
                .id(1L)
                .fileName("sample.txt")
                .contentType("text/plain")
                .objectKey("sample.txt")
                .build();

        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));
        GetObjectResponse pdfResponse = mock(GetObjectResponse.class);
        when(pdfResponse.readAllBytes()).thenReturn("# Heading\n\nHello world".getBytes(StandardCharsets.UTF_8));
        when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(pdfResponse);
        when(exportService.buildPdfFromText("sample.txt", "# Heading\n\nHello world"))
                .thenReturn("%PDF-1.4\n% test".getBytes(StandardCharsets.UTF_8));

        ResponseEntity<byte[]> response = exportController.showDocumentContent(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_PDF_VALUE, response.getHeaders().getContentType().toString());
        assertArrayEquals("%PDF-1.4\n% test".getBytes(StandardCharsets.UTF_8), response.getBody());
    }

    @Test
    void shouldReturnMarkdownTextForAiEndpoint() throws Exception {
        Document document = Document.builder()
                .id(2L)
                .fileName("notes.md")
                .contentType("text/markdown")
                .objectKey("notes.md")
                .build();

        when(documentRepository.findById(2L)).thenReturn(Optional.of(document));
        GetObjectResponse textResponse = mock(GetObjectResponse.class);
        when(textResponse.readAllBytes()).thenReturn("# Notes\n\n- item".getBytes(StandardCharsets.UTF_8));
        when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(textResponse);

        ResponseEntity<String> response = exportController.showDocumentText(2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("# Notes\n\n- item", response.getBody());
        assertEquals(MediaType.TEXT_MARKDOWN_VALUE, response.getHeaders().getContentType().toString());
    }
}
