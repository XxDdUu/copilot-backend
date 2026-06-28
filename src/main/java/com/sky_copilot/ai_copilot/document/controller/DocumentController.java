package com.sky_copilot.ai_copilot.document.controller;

import com.sky_copilot.ai_copilot.document.dto.DocumentUploadResponse;
import com.sky_copilot.ai_copilot.document.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/upload")
    public ResponseEntity<DocumentUploadResponse> upload(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails

    ) {
        DocumentUploadResponse response =
                documentService.upload(file, userDetails.getUsername());

        return ResponseEntity.ok(response);
    }
}
