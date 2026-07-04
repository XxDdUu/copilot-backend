package com.sky_copilot.ai_copilot.document.repository.projection;

import java.time.LocalDateTime;

public interface DocumentChunkProjection {

    Long getId();

    Long getDocumentId();

    Integer getChunkIndex();

    String getContent();

    Double getScore();

    LocalDateTime getCreatedAt();
}