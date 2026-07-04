package com.sky_copilot.ai_copilot.document.mapper;

import com.sky_copilot.ai_copilot.document.dto.DocumentChunkResponse;
import com.sky_copilot.ai_copilot.document.repository.projection.DocumentChunkProjection;


public final class DocumentChunkMapper {

    private DocumentChunkMapper() {
    }

    public static DocumentChunkResponse toResponse(DocumentChunkProjection chunk) {
        return DocumentChunkResponse.builder()
                .id(chunk.getId())
                .documentId(chunk.getDocumentId())
                .chunkIndex(chunk.getChunkIndex())
                .content(chunk.getContent())
                .score(chunk.getScore())
                .createdAt(chunk.getCreatedAt())
                .build();
    }
}