package com.sky_copilot.ai_copilot.export.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentTextResponse {

    private Long id;

    private String name;

    private String contentType;

    private Long size;

    private Integer pageCount;

    private Integer characterCount;

    private String content;
}