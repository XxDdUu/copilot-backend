package com.sky_copilot.ai_copilot.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class UserResponse {

    private Long id;

    private String email;

    private String fullName;

    private Set<String> roles;
}
