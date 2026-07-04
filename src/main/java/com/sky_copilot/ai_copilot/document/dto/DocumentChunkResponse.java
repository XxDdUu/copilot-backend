package com.sky_copilot.ai_copilot.document.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentChunkResponse {

    private Long id;

    private Long documentId;

    private Integer chunkIndex;

    private String content;

    private Double score;

    private LocalDateTime createdAt;
}