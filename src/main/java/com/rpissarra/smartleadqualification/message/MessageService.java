package com.rpissarra.smartleadqualification.message;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageService {


    private final String queueUrl;
    private final MessageRepository messageRepository;
    private final SqsAsyncClient sqsAsyncClient;
    private final ObjectMapper objectMapper;

    public MessageService(
            @Value("${aws.queque-url}") String queueUrl,
            MessageRepository messageRepository,
            SqsAsyncClient sqsAsyncClient,
            ObjectMapper objectMapper) {
        this.queueUrl = queueUrl;
        this.messageRepository = messageRepository;
        this.sqsAsyncClient = sqsAsyncClient;
        this.objectMapper = objectMapper;
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

    private void sendSqsMessage(String content) {
        SendMessageRequest request = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(content)
                .build();
        sqsAsyncClient.sendMessage(request);
    }
}
