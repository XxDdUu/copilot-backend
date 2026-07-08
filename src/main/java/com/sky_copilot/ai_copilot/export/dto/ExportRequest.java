package com.sky_copilot.ai_copilot.export.dto;

/**
 * Request DTO for document export operations
 */
public record ExportRequest(
        String title,
        String content,
        String author,
        String description
) {
}
