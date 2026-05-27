package com.rpissarra.smartleadqualification.message;

public record MessageResponse(
        String message
) {
    public static MessageResponse toMessageResponse(Message message) {
        return new MessageResponse(message.getContent());
    }
}
