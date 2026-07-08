package com.sky_copilot.ai_copilot.export.dto;

/**
 * Response DTO for document export operations
 * Contains download URL and file metadata
 */
public record ExportResponse(
        String downloadUrl,
        String fileName,
        String contentType
) {
}
