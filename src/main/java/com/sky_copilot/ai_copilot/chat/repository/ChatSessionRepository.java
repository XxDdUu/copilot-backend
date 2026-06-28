package com.sky_copilot.ai_copilot.chat.repository;
import com.sky_copilot.ai_copilot.chat.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatSessionRepository
        extends JpaRepository<ChatSession, Long> {
    List<ChatSession> findByUserId(Long userId);
    List<ChatSession> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<ChatSession> findByUserIdAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(Long userId, String title);
}