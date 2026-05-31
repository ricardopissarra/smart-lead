package com.rpissarra.smartleadqualification.message;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name ="MessageResponse", description = "Message schema returned by the api")
public record MessageResponse(
        @Schema(name = "message", example = "How much does it cost to upgrade to pro plan?") String message
) {
    public static MessageResponse toMessageResponse(Message message) {
        return new MessageResponse(message.getContent());
    }
}
