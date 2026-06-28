package com.sky_copilot.ai_copilot.chat.controller;

import com.sky_copilot.ai_copilot.chat.dto.ChatMessageResponse;
import com.sky_copilot.ai_copilot.chat.dto.CreateSessionRequest;
import com.sky_copilot.ai_copilot.chat.dto.SendMessageRequest;
import com.sky_copilot.ai_copilot.chat.entity.ChatSession;
import com.sky_copilot.ai_copilot.chat.service.ChatService;
import com.sky_copilot.ai_copilot.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/sessions")
    public ResponseEntity<?> createSession(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestBody CreateSessionRequest request
    ) {
        System.out.println("Creating session for user: " + user.getUsername());
        System.out.println("User ID: " + user.getId());

        return ResponseEntity.ok(
                chatService.createSession(
                        user.getId(),
                        request.getTitle()
                )
        );
    }

    @GetMapping("/sessions")
    public ResponseEntity<?> getSessions(
            @AuthenticationPrincipal UserPrincipal user
    ) {

        return ResponseEntity.ok(
                chatService.getSessions(user.getId())
        );
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<?> getMessages(
            @PathVariable Long sessionId
    ) {

        return ResponseEntity.ok(
                chatService.getMessages(sessionId)
        );
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestBody SendMessageRequest request
    ) {

        return ResponseEntity.ok(
                chatService.sendMessage(
                        user.getId(),
                        request.getSessionId(),
                        request.getContent()
                )
        );
    }
}