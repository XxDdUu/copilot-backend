package com.sky_copilot.ai_copilot.email.controller;

import com.sky_copilot.ai_copilot.email.dto.SendEmailRequest;
import com.sky_copilot.ai_copilot.email.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/send")
    public ResponseEntity<String> sendEmail(
            @Valid @RequestBody SendEmailRequest request
    ) {
        try {
            emailService.send(request);
            return ResponseEntity.ok("Email sent successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to send email: " + e.getMessage());
        }   
    }
}