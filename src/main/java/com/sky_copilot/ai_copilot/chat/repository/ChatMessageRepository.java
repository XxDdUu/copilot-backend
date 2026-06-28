package com.sky_copilot.ai_copilot.chat.repository;

import com.sky_copilot.ai_copilot.chat.entity.ChatMessage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository
        extends JpaRepository<ChatMessage, Long> {
        List<ChatMessage> findBySessionIdOrderByCreatedAtAsc(Long sessionId);
}