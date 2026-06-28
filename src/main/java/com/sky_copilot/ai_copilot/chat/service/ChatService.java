package com.sky_copilot.ai_copilot.chat.service;

import com.sky_copilot.ai_copilot.chat.entity.ChatSession;
import com.sky_copilot.ai_copilot.chat.dto.ChatMessageResponse;
import java.util.List;

public interface ChatService {

    ChatSession createSession(Long userId, String title);

    List<ChatSession> getSessions(Long userId);

    List<ChatMessageResponse> getMessages(Long sessionId);

    ChatMessageResponse sendMessage(
            Long userId,
            Long sessionId,
            String content
    );
}