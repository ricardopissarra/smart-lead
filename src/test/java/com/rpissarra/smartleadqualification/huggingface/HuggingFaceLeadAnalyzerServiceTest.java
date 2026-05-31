package com.rpissarra.smartleadqualification.huggingface;

import com.rpissarra.smartleadqualification.configuration.HuggingFacesConfiguration;
import com.rpissarra.smartleadqualification.lead.LeadService;
import com.rpissarra.smartleadqualification.lead.NewLeadRequest;
import com.rpissarra.smartleadqualification.lead.Type;
import com.rpissarra.smartleadqualification.lead.UrgencyLevel;
import com.rpissarra.smartleadqualification.message.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HuggingFaceLeadAnalyzerServiceTest {

    @Mock
    private HuggingFaceService huggingFaceService;

    @Mock
    private LeadService leadService;

    @Mock
    private Resource leadPrompt;

    @Mock
    private HuggingFacesConfiguration hfConfig;

    private ObjectMapper objectMapper;
    private HuggingFaceLeadAnalyzerService underTest;

    private final String mockPromptText = "System prompt instruction";
    private final String mockModel = "hf-meta-llama-3";

    @BeforeEach
    void setUp() throws IOException {
        objectMapper = new ObjectMapper();

        lenient().when(leadPrompt.getContentAsString(StandardCharsets.UTF_8)).thenReturn(mockPromptText);
        lenient().when(hfConfig.getModel()).thenReturn(mockModel);

        underTest = new HuggingFaceLeadAnalyzerService(
                huggingFaceService,
                leadService,
                leadPrompt,
                hfConfig,
                objectMapper
        );
    }

    @DisplayName("Analyze message should create a new lead")
    @Test
    void analyzeMessageShouldCreateLead() throws IOException {
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

        ChatCompletionResponse mockResponse = mock(ChatCompletionResponse.class);
        when(mockResponse.content()).thenReturn(jsonResponse);
        when(huggingFaceService.completion(any(HuggingFaceRequest.class))).thenReturn(mockResponse);

        // when
        underTest.analyzeMessage(incomingMessage);
        // then
        ArgumentCaptor<HuggingFaceRequest> requestCaptor = ArgumentCaptor.forClass(HuggingFaceRequest.class);
        verify(huggingFaceService).completion(requestCaptor.capture());

        HuggingFaceRequest sentRequest = requestCaptor.getValue();
        assertEquals(mockModel, sentRequest.model());
        assertEquals(2, sentRequest.messages().size());
        assertEquals("system", sentRequest.messages().getFirst().role());
        assertEquals(mockPromptText, sentRequest.messages().getFirst().content());
        assertEquals("user", sentRequest.messages().getLast().role());
        assertEquals("Interested in buying 50 units ASAP.", sentRequest.messages().getLast().content());

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
    void analyzeMessageShouldNotCreateLead() throws IOException {
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

        ChatCompletionResponse mockResponse = mock(ChatCompletionResponse.class);
        when(mockResponse.content()).thenReturn(jsonResponse);
        when(huggingFaceService.completion(any(HuggingFaceRequest.class))).thenReturn(mockResponse);

        // when
        underTest.analyzeMessage(incomingMessage);
        // then
        verify(huggingFaceService, times(1)).completion(any());
        verify(leadService, never()).createNewLead(any());
    }
}