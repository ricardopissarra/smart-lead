package com.rpissarra.smartleadqualification.journey;

import com.rpissarra.smartleadqualification.lead.LeadResponse;
import com.rpissarra.smartleadqualification.message.MessageRequest;
import com.rpissarra.smartleadqualification.message.MessageResponse;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureWebTestClient
class SmartLeadIT extends BaseIntegrationTest {

    @Autowired
    private SqsAsyncClient sqsClient;
    // Wired automatically via Spring Cloud AWS using our Dynamic properties

    @Autowired
    private WebTestClient webTestClient;

    private static final String MESSAGE_URI = "api/v1/messages";
    private static final String LEAD_URI = "api/v1/leads";


    @Order(1)
    @DisplayName("Test that the testcontainers are running")
    @Test
    void testContainersAreRunningAndHealthy() {
        // Verify Postgres is up
        assertThat(postgres.isRunning()).isTrue();

        // Verify SQS is up and queue exists
        var queues = sqsClient.listQueues();

        assertThat(queues.join().queueUrls())
                .anyMatch(url -> url.contains("my-test-queue"));
    }

    @Order(2)
    @DisplayName("Posting a message")
    @Test
    void shouldBeAbleToPostMessage() {

        MessageRequest messageRequest = new MessageRequest("This is a new message");

        webTestClient.post()
                .uri(MESSAGE_URI)
                .bodyValue(messageRequest)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(MessageResponse.class);

        List<MessageResponse> messageResponseList = webTestClient.get()
                .uri(MESSAGE_URI)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(new ParameterizedTypeReference<MessageResponse>() {})
                .returnResult()
                .getResponseBody();

        assertThat(messageResponseList)
                .isNotEmpty()
                .contains(new MessageResponse(messageRequest.content()));
    }

    @Order(3)
    @DisplayName("Getting message list")
    @Test
    void shouldBeAbleGetMessagesWithoutError() {

        List<MessageResponse> messageResponseList = webTestClient.get()
                .uri(MESSAGE_URI)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(new ParameterizedTypeReference<MessageResponse>() {})
                .returnResult()
                .getResponseBody();

        assertThat(messageResponseList).isNotEmpty();
    }

    @Order(4)
    @DisplayName("Getting leads list")
    @Test
    void shouldBeAbleGetLeadsWithoutError() {
        List<LeadResponse> leadResponseList = webTestClient.get()
                .uri(LEAD_URI)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(new ParameterizedTypeReference<LeadResponse>() {})
                .returnResult()
                .getResponseBody();

        assertThat(leadResponseList).isNotEmpty();
    }

    @Order(5)
    @DisplayName("Getting lead by id")
    @Test
    void shouldBeAbleGetLeadById() {

        List<LeadResponse> leadResponseList = webTestClient.get()
                .uri(LEAD_URI)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(new ParameterizedTypeReference<LeadResponse>() {
                })
                .returnResult()
                .getResponseBody();

        long leadId = leadResponseList.getFirst().id();

        LeadResponse lead = webTestClient.get()
                .uri(LEAD_URI+"/{id}", leadId)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(LeadResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(lead).isNotNull();
        assertThat(lead.id()).isEqualTo(leadId);
    }

    @DisplayName("Getting lead by id returns not found")
    @Test
    void shouldNotBeAbleGetLeadById() {

        List<LeadResponse> leadResponseList = webTestClient.get()
                .uri(LEAD_URI)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(new ParameterizedTypeReference<LeadResponse>() {
                })
                .returnResult()
                .getResponseBody();

        long leadId = leadResponseList.getLast().id() + 1;

        webTestClient.get()
                .uri(LEAD_URI + "/{id}", leadId)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.NOT_FOUND);
    }
}
