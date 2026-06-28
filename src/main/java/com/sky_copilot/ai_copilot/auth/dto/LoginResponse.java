package com.sky_copilot.ai_copilot.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class LoginResponse {

    private Long id;

    private String email;

    private String fullName;

    private String token;

    private Set<String> roles;
}
