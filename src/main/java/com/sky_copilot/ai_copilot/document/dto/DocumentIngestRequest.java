package com.sky_copilot.ai_copilot.document.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentIngestRequest {
    private Long documentId;
    private String bucketName;
    private String objectKey;
}
