package com.rpissarra.smartleadqualification.huggingface;

import java.util.List;
import com.rpissarra.smartleadqualification.huggingface.ChatCompletionResponse.Message;

public record HuggingFaceRequest(
        String model,
        List<Message> messages,
        boolean stream
) {
}
