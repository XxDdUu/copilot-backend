package com.sky_copilot.ai_copilot.document.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class DocumentUploadResponse {

    private Long id;

    private String fileName;

    private String contentType;

    private String objectKey;

    private Long uploadedById;

    private LocalDateTime createdAt;
}
