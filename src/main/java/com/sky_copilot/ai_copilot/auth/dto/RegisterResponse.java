package com.sky_copilot.ai_copilot.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegisterResponse {

    private Long id;

    private String email;

    private String fullName;

    private String accessToken;
}