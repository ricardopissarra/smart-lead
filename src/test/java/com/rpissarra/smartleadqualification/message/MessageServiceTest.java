package com.rpissarra.smartleadqualification.message;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import tools.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;
    @Mock
    private SqsAsyncClient sqsAsyncClient;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private OpenTelemetry openTelemetry;

    @InjectMocks
    private MessageService underTest;

    @DisplayName("Find all should return result")
    @Test
    void getAllMessagesShouldReturnResult() {
        // given
        Pageable pageable = PageRequest.of(0, 3);
        List<Message> messages = List.of(
                Message.builder().id(1L).content("message").status(Status.CREATED).build(),
                Message.builder().id(2L).content("create lead").status(Status.PROCESSED).build(),
                Message.builder().id(3L).content("failed").status(Status.FAILED).build()
        );
        Page<Message> messagePage = new PageImpl<>(messages, pageable, messages.size());
        given(messageRepository.findAll(pageable)).willReturn(messagePage);
        // when
        List<MessageResponse> actual = underTest.getAllMessages(pageable);
        // then
        List<MessageResponse> expected = messages.stream().map(MessageResponse::toMessageResponse).toList();
        assertEquals(expected.size(), actual.size());
        assertEquals(expected, actual);
    }

    @DisplayName("Find all should return empty list")
    @Test
    void getAllMessagesShouldReturnEmptyList() {
        // given
        Pageable pageable = PageRequest.of(0, 3);
        List<Message> messages = Collections.EMPTY_LIST;
        Page<Message> messagePage = new PageImpl<>(messages, pageable, messages.size());
        given(messageRepository.findAll(pageable)).willReturn(messagePage);
        // when
        List<MessageResponse> actual = underTest.getAllMessages(pageable);
        // then
        List<MessageResponse> expected = messages.stream().map(MessageResponse::toMessageResponse).toList();
        assertEquals(expected.size(), actual.size());
        assertEquals(expected, actual);
    }

    @DisplayName("Create a new message")
    @Test
    void createNewMessage() {
        // given
        MessageRequest messageRequest = new MessageRequest("This is a new message");
        Message message = Message.builder()
                .content(messageRequest.content())
                .status(Status.CREATED)
                .build();
        given(messageRepository.save(message)).willReturn(message);

        // Mock the OTel propagation chain so extract() doesn't NPE
        ContextPropagators contextPropagators = mock(ContextPropagators.class);
        TextMapPropagator textMapPropagator = mock(TextMapPropagator.class);
        given(openTelemetry.getPropagators()).willReturn(contextPropagators);
        given(contextPropagators.getTextMapPropagator()).willReturn(textMapPropagator);
        // when
        MessageResponse actual = underTest.createNewMessage(messageRequest);
        // then
        MessageResponse expected = MessageResponse.toMessageResponse(message);
        assertEquals(expected, actual);
    }

    @DisplayName("Update a new message")
    @Test
    void updateMessage() {
        // given
        Message message = Message.builder()
                .content("updated message")
                .status(Status.FAILED)
                .build();
        given(messageRepository.save(message)).willReturn(message);
        // when
        underTest.updateMessage(message);
        // then
        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository, times(1)).save(messageCaptor.capture());
        Message expected = messageCaptor.getValue();
        assertEquals(expected, message);
    }

    @DisplayName("Find message by status returns result")
    @Test
    void findAllMessagesByStatus() {
        // given
        List<Message> messages = List.of(
                Message.builder().id(1L).content("message").status(Status.FAILED).build(),
                Message.builder().id(2L).content("create lead").status(Status.FAILED).build(),
                Message.builder().id(3L).content("failed").status(Status.FAILED).build()
        );
        given(messageRepository.findMessagesByStatus(Status.FAILED)).willReturn(messages);
        // when
        List<Message> actual = underTest.getAllMessagesByStatus(Status.FAILED);
        // then
        assertEquals(messages, actual);
    }

    @DisplayName("Find message by status returns empty")
    @Test
    void findAllMessagesByStatusReturnsEmpty() {
        // given
        List<Message> messages = Collections.EMPTY_LIST;
        given(messageRepository.findMessagesByStatus(Status.FAILED)).willReturn(messages);
        // when
        List<Message> actual = underTest.getAllMessagesByStatus(Status.FAILED);
        // then
        assertEquals(messages, actual);
    }
}