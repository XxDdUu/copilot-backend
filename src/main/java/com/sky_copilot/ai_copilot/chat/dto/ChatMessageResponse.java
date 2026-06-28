package com.sky_copilot.ai_copilot.chat.dto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Builder
@Getter
@Setter
public class ChatMessageResponse {

    private String role;

    private String content;

    private LocalDateTime createdAt;
}