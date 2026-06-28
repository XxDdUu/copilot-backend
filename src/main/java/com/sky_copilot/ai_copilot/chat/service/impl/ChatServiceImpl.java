package com.sky_copilot.ai_copilot.chat.service.impl;

import com.sky_copilot.ai_copilot.chat.dto.ChatMessageResponse;
import com.sky_copilot.ai_copilot.chat.entity.ChatMessage;  
import com.sky_copilot.ai_copilot.chat.entity.ChatSession;
import com.sky_copilot.ai_copilot.chat.repository.ChatMessageRepository;
import com.sky_copilot.ai_copilot.chat.repository.ChatSessionRepository;
import com.sky_copilot.ai_copilot.chat.service.RagService;
import com.sky_copilot.ai_copilot.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List; 

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;
    private final RagService ragService;

    @Override
    public ChatSession createSession(
            Long userId,
            String title
    ) {

        ChatSession session = ChatSession.builder()
                .userId(userId)
                .title(title)
                .createdAt(LocalDateTime.now())
                .build();

        return sessionRepository.save(session);
    }

    @Override
    public List<ChatSession> getSessions(Long userId) {

        return sessionRepository
                .findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public List<ChatMessageResponse> getMessages(
            Long sessionId
    ) {

        return messageRepository
                .findBySessionIdOrderByCreatedAtAsc(sessionId)
                .stream()
                .map(m -> ChatMessageResponse.builder()
                        .role(m.getRole())
                        .content(m.getContent())
                        .createdAt(m.getCreatedAt())
                        .build())
                .toList();
    }

    @Override
    public ChatMessageResponse sendMessage(
            Long userId,
            Long sessionId,
            String content
    ) {

        ChatMessage userMessage =
                ChatMessage.builder()
                        .sessionId(sessionId)
                        .role("USER")
                        .content(content)
                        .createdAt(LocalDateTime.now())
                        .build();

        messageRepository.save(userMessage);

        String aiAnswer;
        try {
            aiAnswer = ragService.ask(content);
        } catch (Exception e) {
            aiAnswer = "Lỗi khi kết nối với AI Service: " + e.getMessage();
        }

        ChatMessage assistantMessage =
                ChatMessage.builder()
                        .sessionId(sessionId)
                        .role("ASSISTANT")
                        .content(aiAnswer)
                        .createdAt(LocalDateTime.now())
                        .build();

        messageRepository.save(assistantMessage);

        return ChatMessageResponse.builder()
                .role("ASSISTANT")
                .content(aiAnswer)
                .createdAt(LocalDateTime.now())
                .build();
    }
}