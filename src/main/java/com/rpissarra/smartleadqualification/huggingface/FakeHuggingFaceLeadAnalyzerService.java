package com.rpissarra.smartleadqualification.huggingface;

import com.rpissarra.smartleadqualification.lead.*;
import com.rpissarra.smartleadqualification.message.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service("fakeHuggingFaceLeadAnalyzerService")
@ConditionalOnProperty(
        name = "enable.fake.hf",
        havingValue = "true"
)
@Slf4j
public class FakeHuggingFaceLeadAnalyzerService implements HuggingFaceLeadAnalyzer {

    private final LeadService leadService;

    public FakeHuggingFaceLeadAnalyzerService(
            LeadService leadService
    ) {
        this.leadService = leadService;
    }


    @Override
    public Optional<Lead> analyzeMessage(Message message) {
        log.info("Fake Analyzing message: {}", message.getContent());
        NewLeadRequest leadRequest = new NewLeadRequest(
                message.getContent(),
                Type.OTHER,
                UrgencyLevel.LOW,
                message.getContent(),
                message
        );
        return Optional.of(leadService.createNewLead(leadRequest));
    }
}
