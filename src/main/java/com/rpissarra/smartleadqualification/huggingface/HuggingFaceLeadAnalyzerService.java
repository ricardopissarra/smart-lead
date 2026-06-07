package com.rpissarra.smartleadqualification.huggingface;

import com.rpissarra.smartleadqualification.configuration.HuggingFacesConfiguration;
import com.rpissarra.smartleadqualification.lead.Lead;
import com.rpissarra.smartleadqualification.lead.LeadService;
import com.rpissarra.smartleadqualification.lead.NewLeadRequest;
import com.rpissarra.smartleadqualification.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service("huggingFaceLeadAnalyzerService")
@ConditionalOnProperty(
        name = "enable.fake.hf",
        havingValue = "false",
        matchIfMissing = true
)
public class HuggingFaceLeadAnalyzerService implements HuggingFaceLeadAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(HuggingFaceLeadAnalyzerService.class);

    private final HuggingFaceService huggingFaceService;
    private final LeadService leadService;
    private final Resource leadPrompt;
    private final HuggingFacesConfiguration hfConfig;
    private final ObjectMapper objectMapper;

    public HuggingFaceLeadAnalyzerService(
            HuggingFaceService huggingFaceService,
            LeadService leadService,
            @Value("classpath:/prompt/lead-prompt.txt") Resource leadPrompt,
            HuggingFacesConfiguration hfConfig,
            ObjectMapper objectMapper) {
        this.huggingFaceService = huggingFaceService;
        this.leadService = leadService;
        this.leadPrompt = leadPrompt;
        this.hfConfig = hfConfig;
        this.objectMapper = objectMapper;
    }


    @Override
    public Lead analyzeMessage(Message message) {
        log.info("Analyzing message: {}", message.getContent());

        String prompt;
        try {
            prompt = leadPrompt.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage());
        }

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
                    result.description(),
                    message
            );
            return leadService.createNewLead(leadRequest);
        }

        return null;
    }
}
