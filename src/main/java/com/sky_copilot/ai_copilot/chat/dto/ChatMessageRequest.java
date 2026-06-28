package com.sky_copilot.ai_copilot.chat.dto;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Builder
@Getter
@Setter
public class ChatMessageRequest {

    private String role;

    private String content;

}