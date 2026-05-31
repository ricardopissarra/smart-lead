package com.rpissarra.smartleadqualification.scheduler;

import com.rpissarra.smartleadqualification.huggingface.HuggingFaceLeadAnalyzerService;
import com.rpissarra.smartleadqualification.message.MessageService;
import com.rpissarra.smartleadqualification.message.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RetryMessageAnalysisScheduler {

    private final HuggingFaceLeadAnalyzerService analyzerService;
    private final MessageService messageService;

    public RetryMessageAnalysisScheduler(HuggingFaceLeadAnalyzerService analyzerService, MessageService messageService) {
        this.analyzerService = analyzerService;
        this.messageService = messageService;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void reprocessFailedMessages() {
        messageService.getAllMessagesByStatus(Status.FAILED)
                .forEach(message -> {
                            try {
                                analyzerService.analyzeMessage(message);
                                message.setStatus(Status.PROCESSED);
                                messageService.updateMessage(message);
                            } catch (Exception e) {
                                log.warn("Error [{}] analyzing message with id {}", e.getClass(), message.getId(), e);
                            }
                        }
                );
    }
}
