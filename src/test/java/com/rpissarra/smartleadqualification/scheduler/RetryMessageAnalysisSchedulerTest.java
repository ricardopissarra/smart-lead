package com.rpissarra.smartleadqualification.scheduler;

import com.rpissarra.smartleadqualification.huggingface.HuggingFaceLeadAnalyzerService;
import com.rpissarra.smartleadqualification.message.Message;
import com.rpissarra.smartleadqualification.message.MessageService;
import com.rpissarra.smartleadqualification.message.Status;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class RetryMessageAnalysisSchedulerTest {

    @Mock
    private HuggingFaceLeadAnalyzerService analyzerService;

    @Mock
    private MessageService messageService;

    @InjectMocks
    private RetryMessageAnalysisScheduler underTest;

    @DisplayName("Scheduler reprocesses failed messages")
    @Test
    void reprocessFailedMessages() {
        // given
        List<Message> messages = List.of(
                Message.builder().id(3L).content("failed").status(Status.FAILED).build(),
                Message.builder().id(4L).content("failed 2").status(Status.FAILED).build()
        );
        given(messageService.getAllMessagesByStatus(Status.FAILED)).willReturn(messages);
        // when
        underTest.reprocessFailedMessages();
        // then
        verify(analyzerService, times(2)).analyzeMessage(any(Message.class));
    }

    @DisplayName("Scheduler reprocesses failed messages")
    @Test
    void reprocessCreatedAndNotProcessedInTheLast15MinMessages() {
        // given
        List<Message> messages = List.of(
                Message.builder().id(3L).content("Created").status(Status.CREATED).build()
        );
        given(messageService.getAllMessagesByStatusAndCreateDate(eq(Status.CREATED), any(LocalDateTime.class))).willReturn(messages);
        // when
        underTest.reprocessUnprocessedMessages();
        // then
        verify(analyzerService, times(1)).analyzeMessage(any(Message.class));
    }
}