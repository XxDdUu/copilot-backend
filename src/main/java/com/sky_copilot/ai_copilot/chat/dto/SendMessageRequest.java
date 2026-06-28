package com.sky_copilot.ai_copilot.chat.dto;

import lombok.Data;

@Data
public class SendMessageRequest {

    private Long sessionId;

    private String content;
}