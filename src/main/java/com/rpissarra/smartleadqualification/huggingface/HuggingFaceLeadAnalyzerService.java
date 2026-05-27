package com.rpissarra.smartleadqualification.huggingface;

import com.rpissarra.smartleadqualification.configuration.HuggingFacesConfiguration;
import com.rpissarra.smartleadqualification.exception.AiResponseException;
import com.rpissarra.smartleadqualification.lead.Lead;
import com.rpissarra.smartleadqualification.lead.LeadResponse;
import com.rpissarra.smartleadqualification.lead.LeadService;
import com.rpissarra.smartleadqualification.lead.NewLeadRequest;
import com.rpissarra.smartleadqualification.message.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class HuggingFaceLeadAnalyzerService {

    private final HuggingFaceService huggingFaceService;
    private final LeadService leadService;
    private final Resource leadPrompt;
    private final HuggingFacesConfiguration hfConfig;
    private final ObjectMapper objectMapper;

    public HuggingFaceLeadAnalyzerService(
            HuggingFaceService huggingFaceService,
            LeadService leadService,
            @Value("classpath:/prompt/lead-prompt.txt")Resource leadPrompt,
            HuggingFacesConfiguration hfConfig,
            ObjectMapper objectMapper) {
        this.huggingFaceService = huggingFaceService;
        this.leadService = leadService;
        this.leadPrompt = leadPrompt;
        this.hfConfig = hfConfig;
        this.objectMapper = objectMapper;
    }


    @Async
    public CompletableFuture<LeadResponse> analyzeMessage(Message message) {
        try {
            String prompt = leadPrompt.getContentAsString(StandardCharsets.UTF_8);

            HuggingFaceRequest request = new HuggingFaceRequest(
                    hfConfig.getModel(),
                    List.of(
                            new ChatCompletionResponse.Message("system", prompt),
                            new ChatCompletionResponse.Message("user", message.getContent())
                    ),
                    false
            );

            ChatCompletionResponse response = huggingFaceService.completion(request);
            LeadAnalysisResult result = objectMapper.readValue(response.content(), LeadAnalysisResult.class);

            if (result.shouldCreateLead()) {
                NewLeadRequest leadRequest = new NewLeadRequest(
                        result.title(),
                        result.type(),
                        result.urgencyLevel(),
                        result.description()
                );
                Lead newLead = leadService.createNewLead(leadRequest);
                return CompletableFuture.completedFuture(LeadResponse.toLeadResponse(newLead));
            }

            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Unexpected error while analyzing message with AI: {}", e.getMessage(), e);
            throw new AiResponseException("Error analyzing message with id %d using AI".formatted(message.getId()), HttpStatus.BAD_GATEWAY);
        }
    }
}
