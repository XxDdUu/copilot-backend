package com.sky_copilot.ai_copilot.email.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record SendEmailRequest(

        @Email
        @NotBlank
        String to,

        @NotBlank
        String subject,

        @NotBlank
        String body,

        List<@NotNull Long> documentIds
) {
}