package com.sky_copilot.ai_copilot.auth.dto;
import lombok.Data;

@Data
public class RegisterRequest {

    private String email;

    private String password;

    private String fullName;
}