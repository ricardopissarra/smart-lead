package com.rpissarra.smartleadqualification.huggingface;

import com.rpissarra.smartleadqualification.lead.*;
import com.rpissarra.smartleadqualification.message.Message;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HuggingFaceLeadAnalyzerServiceTest {


    @Mock
    private LeadService leadService;

    @Mock
    private Resource leadPrompt;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChatClient chatClient;

    private HuggingFaceLeadAnalyzerService underTest;

    private final String mockPromptText = "System prompt instruction";

    @BeforeEach
    void setUp() throws IOException {

        lenient().when(leadPrompt.getContentAsString(StandardCharsets.UTF_8)).thenReturn(mockPromptText);

        underTest = new HuggingFaceLeadAnalyzerService(
                leadService,
                leadPrompt,
                chatClient
        );
    }

    @DisplayName("Analyze message should create a new lead")
    @Test
    void analyzeMessageShouldCreateLead() {
        // given
        Message incomingMessage = Message.builder()
                .content("Interested in buying 50 units ASAP.")
                .build();

        when(chatClient.prompt()
                .system(mockPromptText)
                .user(incomingMessage.getContent())
                .call()
                .entity(LeadAnalysisResult.class)
        ).thenReturn(
                new LeadAnalysisResult(
                        true,
                        "Bulk Purchase Inquiry",
                        Type.PRICING_INQUIRY,
                        UrgencyLevel.HIGH,
                        "Client wants 50 units immediately."
                )
        );

        when(leadService.createNewLead(any())).thenReturn(
                Lead.builder()
                        .title("Bulk Purchase Inquiry")
                        .type(Type.PRICING_INQUIRY)
                        .urgencyLevel(UrgencyLevel.HIGH)
                        .description("Client wants 50 units immediately.")
                        .build()
        );
        // when
        underTest.analyzeMessage(incomingMessage);
        // then

        ArgumentCaptor<NewLeadRequest> leadCaptor = ArgumentCaptor.forClass(NewLeadRequest.class);
        verify(leadService, times(1)).createNewLead(leadCaptor.capture());

        NewLeadRequest actual = leadCaptor.getValue();
        assertEquals("Bulk Purchase Inquiry", actual.title());
        assertEquals(Type.PRICING_INQUIRY, actual.type());
        assertEquals(UrgencyLevel.HIGH, actual.urgencyLevel());
        assertEquals("Client wants 50 units immediately.", actual.description());
    }

    @DisplayName("Analyze message should not create a new lead")
    @Test
    void analyzeMessageShouldNotCreateLead() {
        // given
        Message incomingMessage = Message.builder()
                .content("Interested in buying 50 units ASAP.")
                .build();


        when(chatClient.prompt()
                .system(mockPromptText)
                .user(incomingMessage.getContent())
                .call()
                .entity(LeadAnalysisResult.class)
        ).thenReturn(
                new LeadAnalysisResult(
                        false,
                        null,
                        null,
                        null,
                        null
                )
        );

        // when
        underTest.analyzeMessage(incomingMessage);
        // then
        verify(leadService, never()).createNewLead(any());
    }

    @DisplayName("Analyze message should throw exception")
    @SneakyThrows(IOException.class)
    @Test
    void analyzeMessageShouldThrowException() {
        // given
        when(leadPrompt.getContentAsString(StandardCharsets.UTF_8)).thenThrow(
                new IllegalStateException("Error reading prompt file")
        );
       // then
        verify(leadService, never()).createNewLead(any());
        assertThrows(IllegalStateException.class,
                () -> underTest.analyzeMessage(Message.builder().content("test").build())
        );
    }
}