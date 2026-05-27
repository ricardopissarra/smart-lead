package com.rpissarra.smartleadqualification.message;

import com.rpissarra.smartleadqualification.huggingface.HuggingFaceLeadAnalyzerService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final HuggingFaceLeadAnalyzerService leadAnalyzerService;

    public MessageService(MessageRepository messageRepository, HuggingFaceLeadAnalyzerService leadAnalyzerService) {
        this.messageRepository = messageRepository;
        this.leadAnalyzerService = leadAnalyzerService;
    }

    public List<MessageResponse> getAllMessages() {
        return messageRepository.findAll().stream()
                .map(MessageResponse::toMessageResponse)
                .toList();
    }

    public MessageResponse createNewMessage(MessageRequest messageRequest) {
        Message newMessage = Message.builder()
                .content(messageRequest.content())
                .build();
        Message message = messageRepository.save(newMessage);

        leadAnalyzerService.analyzeMessage(message);
        return MessageResponse.toMessageResponse(message);
    }
}
