package com.sky_copilot.ai_copilot.auth.controller;

import com.sky_copilot.ai_copilot.auth.dto.LoginRequest;
import com.sky_copilot.ai_copilot.auth.dto.LoginResponse;
import com.sky_copilot.ai_copilot.auth.dto.RegisterRequest;
import com.sky_copilot.ai_copilot.auth.dto.RegisterResponse;
import com.sky_copilot.ai_copilot.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(authService.login(request));
    }
}