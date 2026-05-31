package com.rpissarra.smartleadqualification.sqs;

import com.rpissarra.smartleadqualification.huggingface.HuggingFaceLeadAnalyzerService;
import com.rpissarra.smartleadqualification.message.Message;
import com.rpissarra.smartleadqualification.message.MessageService;
import com.rpissarra.smartleadqualification.message.Status;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;


@Service
@Slf4j
public class SqsMessageListener {

    private final HuggingFaceLeadAnalyzerService analyzerService;
    private final ObjectMapper objectMapper;
    private final MessageService messageService;

    public SqsMessageListener(HuggingFaceLeadAnalyzerService analyzerService, ObjectMapper objectMapper, MessageService messageService) {
        this.analyzerService = analyzerService;
        this.objectMapper = objectMapper;
        this.messageService = messageService;
    }

    @SqsListener("message-analysis-queue")
    public void receiveMessage(String content) {
        Message message = objectMapper.readValue(content, Message.class);
        try {
            analyzerService.analyzeMessage(message);
            message.setStatus(Status.PROCESSED);
            messageService.updateMessage(message);
        } catch (Exception e) {
            log.warn("Error [{}] analyzing message with id {}", e.getClass(), message.getId(), e);
            message.setStatus(Status.FAILED);
            messageService.updateMessage(message);
        }
    }
}
