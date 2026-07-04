package com.sky_copilot.ai_copilot.document.controller;

import com.sky_copilot.ai_copilot.document.dto.DocumentUploadResponse;
import com.sky_copilot.ai_copilot.document.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import com.sky_copilot.ai_copilot.document.entity.Document;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.sky_copilot.ai_copilot.document.dto.SearchDocumentRequest;
import com.sky_copilot.ai_copilot.document.dto.DocumentChunkResponse;
import com.sky_copilot.ai_copilot.document.service.SearchDocumentService;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final SearchDocumentService searchDocumentService;

    @GetMapping
    public List<Document> getAllDocuments() {
        return documentService.getAllDocuments();
    }

    @PostMapping("/upload")
    public ResponseEntity<DocumentUploadResponse> upload(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails

    ) {
        DocumentUploadResponse response =
                documentService.upload(file, userDetails.getUsername());

        return ResponseEntity.ok(response);
    }
    @PostMapping("/search")
    public List<DocumentChunkResponse> search(
            @RequestBody SearchDocumentRequest request
    ) {

        return searchDocumentService.search(
                request.query(),
                request.topK()
        );
    }
}
