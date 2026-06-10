package com.rpissarra.smartleadqualification.sqs;

import com.rpissarra.smartleadqualification.huggingface.HuggingFaceLeadAnalyzer;
import com.rpissarra.smartleadqualification.lead.Lead;
import com.rpissarra.smartleadqualification.message.Message;
import com.rpissarra.smartleadqualification.message.MessageRepository;
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
    private final MessageRepository messageRepository;

    public SqsMessageListener(
            HuggingFaceLeadAnalyzer analyzerService,
            ObjectMapper objectMapper,
            MessageRepository messageRepository) {
        this.analyzerService = analyzerService;
        this.objectMapper = objectMapper;
        this.messageRepository = messageRepository;
    }

    @SqsListener("${app.queue.name}")
    public void receiveMessage(String content) {
        Message message = objectMapper.readValue(content, Message.class);
        try {
            message.setStatus(Status.PROCESSING);
            messageRepository.save(message);
            Lead lead = analyzerService.analyzeMessage(message)
                    .orElse(null);
            message.setStatus(Status.PROCESSED);
            message.setLead(lead);
            messageRepository.save(message);
        } catch (Exception e) {
            log.error("Error [{}] analyzing message with id {}", e.getClass(), message.getId(), e);
            message.setStatus(Status.FAILED);
            messageRepository.save(message);
        }
    }
}
