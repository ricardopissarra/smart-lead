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
import tools.jackson.databind.ObjectMapper;

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

    private ObjectMapper objectMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChatClient chatClient;

    private HuggingFaceLeadAnalyzerService underTest;

    private final String mockPromptText = "System prompt instruction";

    @BeforeEach
    void setUp() throws IOException {
        objectMapper = new ObjectMapper();

        lenient().when(leadPrompt.getContentAsString(StandardCharsets.UTF_8)).thenReturn(mockPromptText);

        underTest = new HuggingFaceLeadAnalyzerService(
                leadService,
                leadPrompt,
                objectMapper,
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

        String jsonResponse = """
                {
                    "shouldCreateLead": true,
                    "title": "Bulk Purchase Inquiry",
                    "type": "PRICING_INQUIRY",
                    "urgencyLevel": "HIGH",
                    "description": "Client wants 50 units immediately."
                }
                """;


        when(chatClient.prompt()
                .system(mockPromptText)
                .user(incomingMessage.getContent())
                .call()
                .content()
        ).thenReturn(jsonResponse);

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

        String jsonResponse = """
                {
                    "shouldCreateLead": false,
                    "title": null,
                    "type": null,
                    "urgencyLevel": null,
                    "description": null
                }
                """;

        when(chatClient.prompt()
                .system(mockPromptText)
                .user(incomingMessage.getContent())
                .call()
                .content()
        ).thenReturn(jsonResponse);

        // when
        underTest.analyzeMessage(incomingMessage);
        // then
        verify(leadService, never()).createNewLead(any());
    }

    @DisplayName("Analyze message should not create a new lead")
    @SneakyThrows
    @Test
    void analyzeMessageShouldThrowError() {
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