package com.rpissarra.smartleadqualification.message;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "MessageRequest", description = "Schema to create a new message")
public record MessageRequest(
        @NotBlank(message = "Content cannot be null or empty.")
        @Schema(name = "content", example = "How much does it cost to upgrade to pro plan?")
        String content
) {
}
