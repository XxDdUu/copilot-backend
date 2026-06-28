package com.sky_copilot.ai_copilot.auth.dto;

import lombok.Data;

@Data
public class LoginRequest {

    private String email;

    private String password;
}
