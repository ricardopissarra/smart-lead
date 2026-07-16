package com.rpissarra.smartleadqualification.sqs;

import com.rpissarra.smartleadqualification.huggingface.HuggingFaceLeadAnalyzerService;
import com.rpissarra.smartleadqualification.message.Message;
import com.rpissarra.smartleadqualification.message.MessageRepository;
import com.rpissarra.smartleadqualification.message.Status;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.util.Collections;

import static org.mockito.BDDMockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SqsMessageListenerTest {

    @Mock
    private HuggingFaceLeadAnalyzerService analyzerService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    OpenTelemetry openTelemetry;

    @InjectMocks
    private SqsMessageListener underTest;

    @DisplayName("Message listener should create new lead")
    @Test
    void receiveMessageCreatesNewLead() {
        // given
        Message message = Message.builder().id(1L).content("random text").build();
        String content = objectMapper.writeValueAsString(message);
        software.amazon.awssdk.services.sqs.model.Message sqsMessage = software.amazon.awssdk.services.sqs.model.Message.builder()
                .messageId("1")
                .body(content)
                .attributes(Collections.EMPTY_MAP)
                .build();
        given(objectMapper.readValue(sqsMessage.body(), Message.class)).willReturn(message);

        // Mock the OTel propagation chain so extract() doesn't NPE
        ContextPropagators contextPropagators = mock(ContextPropagators.class);
        TextMapPropagator textMapPropagator = mock(TextMapPropagator.class);
        given(openTelemetry.getPropagators()).willReturn(contextPropagators);
        given(contextPropagators.getTextMapPropagator()).willReturn(textMapPropagator);
        given(textMapPropagator.extract(any(), any(), any())).willReturn(Context.root());

        // when
        underTest.receiveMessage(sqsMessage);
        // then
        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository, times(2)).save(messageCaptor.capture());
        Message updated = messageCaptor.getValue();
        assertEquals(Status.PROCESSED, updated.getStatus());
    }

    @DisplayName("Message listener throws exception and updates message as failed")
    @Test
    void receiveMessageCreatesUpdateAsFailed() {
        // given
        Message message = Message.builder().id(1L).content("random text").build();
        String content = objectMapper.writeValueAsString(message);
        software.amazon.awssdk.services.sqs.model.Message sqsMessage = software.amazon.awssdk.services.sqs.model.Message.builder()
                .messageId("1")
                .body(content)
                .attributes(Collections.EMPTY_MAP)
                .build();
        given(objectMapper.readValue(sqsMessage.body(), Message.class)).willReturn(message);

        // Mock the OTel propagation chain so extract() doesn't NPE
        ContextPropagators contextPropagators = mock(ContextPropagators.class);
        TextMapPropagator textMapPropagator = mock(TextMapPropagator.class);
        given(openTelemetry.getPropagators()).willReturn(contextPropagators);
        given(contextPropagators.getTextMapPropagator()).willReturn(textMapPropagator);
        given(textMapPropagator.extract(any(), any(), any())).willReturn(Context.root());

        doThrow(new RuntimeException("Random Error")).when(analyzerService).analyzeMessage(message);
        // when
        underTest.receiveMessage(sqsMessage);
        // then
        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository, times(2)).save(messageCaptor.capture());
        Message updated = messageCaptor.getValue();
        assertEquals(Status.FAILED, updated.getStatus());
    }
}