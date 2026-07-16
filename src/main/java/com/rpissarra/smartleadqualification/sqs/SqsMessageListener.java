package com.rpissarra.smartleadqualification.sqs;

import com.rpissarra.smartleadqualification.huggingface.HuggingFaceLeadAnalyzer;
import com.rpissarra.smartleadqualification.lead.Lead;
import com.rpissarra.smartleadqualification.message.Message;
import com.rpissarra.smartleadqualification.message.MessageRepository;
import com.rpissarra.smartleadqualification.message.Status;
import io.awspring.cloud.sqs.annotation.SqsListener;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;


@Service
public class SqsMessageListener {

    private static final Logger log = LoggerFactory.getLogger(SqsMessageListener.class);

    private final HuggingFaceLeadAnalyzer analyzerService;
    private final ObjectMapper objectMapper;
    private final MessageRepository messageRepository;
    private final OpenTelemetry openTelemetry;

    public SqsMessageListener(
            HuggingFaceLeadAnalyzer analyzerService,
            ObjectMapper objectMapper,
            MessageRepository messageRepository, OpenTelemetry openTelemetry) {
        this.analyzerService = analyzerService;
        this.objectMapper = objectMapper;
        this.messageRepository = messageRepository;
        this.openTelemetry = openTelemetry;
    }

    @SqsListener("${app.queue.name}")
    public void receiveMessage(software.amazon.awssdk.services.sqs.model.Message sqsMessage) {
        TextMapPropagator propagator = openTelemetry.getPropagators().getTextMapPropagator();

        Context extractedContext = propagator.extract(Context.current(), sqsMessage.messageAttributes(),
                new TextMapGetter<>() {
                    public Iterable<String> keys(Map<String, MessageAttributeValue> carrier) {
                        return carrier.keySet();
                    }
                    public String get(Map<String, MessageAttributeValue> carrier, String key) {
                        MessageAttributeValue v = carrier.get(key);
                        return v != null ? v.stringValue() : null;
                    }
                });


        Message message = objectMapper.readValue(sqsMessage.body(), Message.class);
        try (Scope scope = extractedContext.makeCurrent()) {
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
