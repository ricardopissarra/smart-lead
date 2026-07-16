package com.rpissarra.smartleadqualification.message;

import io.micrometer.observation.annotation.Observed;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapPropagator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MessageService {


    private final String queueUrl;
    private final MessageRepository messageRepository;
    private final SqsAsyncClient sqsAsyncClient;
    private final ObjectMapper objectMapper;
    private final OpenTelemetry openTelemetry;


    public MessageService(
            @Value("${aws.queque-url}") String queueUrl,
            MessageRepository messageRepository,
            SqsAsyncClient sqsAsyncClient,
            ObjectMapper objectMapper, OpenTelemetry openTelemetry) {
        this.queueUrl = queueUrl;
        this.messageRepository = messageRepository;
        this.sqsAsyncClient = sqsAsyncClient;
        this.objectMapper = objectMapper;
        this.openTelemetry = openTelemetry;
    }

    public List<MessageResponse> getAllMessages(Pageable pageable) {
        return messageRepository.findAll(pageable).stream()
                .map(MessageResponse::toMessageResponse)
                .toList();
    }

    public MessageResponse createNewMessage(MessageRequest messageRequest) {
        Message newMessage = Message.builder()
                .content(messageRequest.content())
                .status(Status.CREATED)
                .build();
        Message message = messageRepository.save(newMessage);
        sendSqsMessage(objectMapper.writeValueAsString(message));
        return MessageResponse.toMessageResponse(message);
    }

    public void updateMessage(Message message) {
        messageRepository.save(message);
    }

    public List<Message> getAllMessagesByStatus(Status status) {
        return messageRepository.findMessagesByStatus(status);
    }

    public List<Message> getAllMessagesByStatusAndCreateDate(Status status, LocalDateTime time) {
        return messageRepository.findMessagesByStatusAndCreatedAt(status, time);
    }

    @Observed(name = "sqs.send", contextualName = "sqs-send")
    private void sendSqsMessage(String content) {
        Map<String, MessageAttributeValue> attributes = new HashMap<>();

        TextMapPropagator propagator = openTelemetry.getPropagators().getTextMapPropagator();
        propagator.inject(Context.current(), attributes, (carrier, key, value) ->
                carrier.put(key, MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue(value)
                        .build())
        );
        SendMessageRequest request = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(content)
                .messageAttributes(attributes)
                .build();
        sqsAsyncClient.sendMessage(request);
    }
}
