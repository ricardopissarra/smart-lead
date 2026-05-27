package com.rpissarra.smartleadqualification.message;

import jakarta.validation.constraints.NotBlank;

public record MessageRequest(
        @NotBlank(message = "Content cannot be null or empty.")
        String content
) {
}
