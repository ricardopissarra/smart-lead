package com.rpissarra.smartleadqualification.huggingface;

import com.rpissarra.smartleadqualification.lead.Lead;
import com.rpissarra.smartleadqualification.lead.LeadService;
import com.rpissarra.smartleadqualification.lead.NewLeadRequest;
import com.rpissarra.smartleadqualification.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Service("huggingFaceLeadAnalyzerService")
@ConditionalOnProperty(
        name = "enable.fake.hf",
        havingValue = "false",
        matchIfMissing = true
)
public class HuggingFaceLeadAnalyzerService implements HuggingFaceLeadAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(HuggingFaceLeadAnalyzerService.class);

    private final LeadService leadService;
    private final Resource leadPrompt;
    private final ChatClient chatClient;

    public HuggingFaceLeadAnalyzerService(
            LeadService leadService,
            @Value("classpath:/prompt/lead-prompt.txt") Resource leadPrompt,
            ChatClient chatClient
    ) {
        this.leadService = leadService;
        this.leadPrompt = leadPrompt;
        this.chatClient = chatClient;
    }


    @Override
    public Optional<Lead> analyzeMessage(Message message) {
        log.info("Analyzing message: {}", message.getContent());

        String prompt;
        try {
            prompt = leadPrompt.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage());
        }

        Optional<LeadAnalysisResult> result = Optional.ofNullable(chatClient.prompt()
                .system(prompt)
                .user(message.getContent())
                .call()
                .entity(LeadAnalysisResult.class));

        if (result.isPresent() && result.get().shouldCreateLead()) {
            NewLeadRequest leadRequest = new NewLeadRequest(
                    result.get().title(),
                    result.get().type(),
                    result.get().urgencyLevel(),
                    result.get().description(),
                    message
            );
            return Optional.of(leadService.createNewLead(leadRequest));
        }

        return Optional.empty();
    }
}
