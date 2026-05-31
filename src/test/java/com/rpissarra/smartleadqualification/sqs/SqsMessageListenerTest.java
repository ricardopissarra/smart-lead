package com.rpissarra.smartleadqualification.sqs;

import com.rpissarra.smartleadqualification.huggingface.HuggingFaceLeadAnalyzerService;
import com.rpissarra.smartleadqualification.message.Message;
import com.rpissarra.smartleadqualification.message.MessageService;
import com.rpissarra.smartleadqualification.message.Status;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

import static org.mockito.BDDMockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SqsMessageListenerTest {

    @Mock
    private HuggingFaceLeadAnalyzerService analyzerService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private MessageService messageService;

    @InjectMocks
    private SqsMessageListener underTest;

    @DisplayName("Message listener should create new lead")
    @Test
    void receiveMessageCreatesNewLead() {
        // given
        Message message = Message.builder().id(1L).content("random text").status(Status.PROCESSED).build();
        String content = objectMapper.writeValueAsString(message);
        given(objectMapper.readValue(content, Message.class)).willReturn(message);
        // when
        underTest.receiveMessage(content);
        // then
        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(messageService, times(1)).updateMessage(messageCaptor.capture());
        Message updated = messageCaptor.getValue();
        assertEquals(message.getStatus(), updated.getStatus());
    }

    @DisplayName("Message listener throws exception and updates message as failed")
    @Test
    void receiveMessageCreatesUpdateAsFailed() throws IOException {
        // given
        Message message = Message.builder().id(1L).content("random text").status(Status.FAILED).build();
        String content = objectMapper.writeValueAsString(message);
        given(objectMapper.readValue(content, Message.class)).willReturn(message);
        doThrow(new RuntimeException("Random Error")).when(analyzerService).analyzeMessage(message);
        // when
        underTest.receiveMessage(content);
        // then
        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(messageService, times(1)).updateMessage(messageCaptor.capture());
        Message updated = messageCaptor.getValue();
        assertEquals(message.getStatus(), updated.getStatus());
    }
}