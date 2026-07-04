package com.sky_copilot.ai_copilot.document.dto;

public record SearchDocumentRequest(
        String query,
        Integer topK
) {
        public Integer topK() {
                return topK == null ? 5 : topK;
        }
}