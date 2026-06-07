package com.rpissarra.smartleadqualification.sqs;

import com.rpissarra.smartleadqualification.huggingface.HuggingFaceLeadAnalyzer;
import com.rpissarra.smartleadqualification.lead.Lead;
import com.rpissarra.smartleadqualification.message.Message;
import com.rpissarra.smartleadqualification.message.MessageService;
import com.rpissarra.smartleadqualification.message.Status;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;


@Service
public class SqsMessageListener {

    private static final Logger log = LoggerFactory.getLogger(SqsMessageListener.class);

    private final HuggingFaceLeadAnalyzer analyzerService;
    private final ObjectMapper objectMapper;
    private final MessageService messageService;

    public SqsMessageListener(
            HuggingFaceLeadAnalyzer analyzerService,
            ObjectMapper objectMapper,
            MessageService messageService) {
        this.analyzerService = analyzerService;
        this.objectMapper = objectMapper;
        this.messageService = messageService;
    }

    @SqsListener("${app.queue.name}")
    public void receiveMessage(String content) {
        Message message = objectMapper.readValue(content, Message.class);
        try {
            Lead lead = analyzerService.analyzeMessage(message);
            message.setStatus(Status.PROCESSED);
            message.setLead(lead);
            messageService.updateMessage(message);
        } catch (Exception e) {
            log.error("Error [{}] analyzing message with id {}", e.getClass(), message.getId(), e);
            message.setStatus(Status.FAILED);
            messageService.updateMessage(message);
        }
    }
}
