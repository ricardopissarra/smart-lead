package com.rpissarra.smartleadqualification.scheduler;

import com.rpissarra.smartleadqualification.huggingface.HuggingFaceLeadAnalyzer;
import com.rpissarra.smartleadqualification.message.MessageService;
import com.rpissarra.smartleadqualification.message.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class RetryMessageAnalysisScheduler {

    private static final Logger log = LoggerFactory.getLogger(RetryMessageAnalysisScheduler.class);

    private final HuggingFaceLeadAnalyzer analyzerService;
    private final MessageService messageService;

    public RetryMessageAnalysisScheduler(
            HuggingFaceLeadAnalyzer analyzerService,
            MessageService messageService) {
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
                                log.error("Error [{}] analyzing message with id {}", e.getClass(), message.getId(), e);
                            }
                        }
                );
    }
}
