package com.rpissarra.smartleadqualification.journey;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.CreateQueueResponse;

import java.io.IOException;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseIntegrationTest {

    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:18-alpine")
    );

    // Pinned to a community release: the `latest` tag now requires a LOCALSTACK_AUTH_TOKEN (Pro)
    // and exits with code 55 on startup. SQS runs free on the community image.
    static final LocalStackContainer localStack = new LocalStackContainer(
            DockerImageName.parse("localstack/localstack:3.8.1")
    ).withServices(LocalStackContainer.Service.SQS);

    static {
        postgres.start();
        localStack.start();
        try {
            localStack.execInContainer("awslocal", "sqs", "create-queue", "--queue-name", "my-test-queue");
            localStack.execInContainer("awslocal", "sqs", "list-queues");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error creating queue", e);
        }

    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Postgres Configuration
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        // SQS Configuration
        registry.add("spring.cloud.aws.credentials.access-key", localStack::getAccessKey);
        registry.add("spring.cloud.aws.credentials.secret-key", localStack::getSecretKey);
        registry.add("spring.cloud.aws.region.static", localStack::getRegion);
        registry.add("spring.cloud.aws.sqs.endpoint",
                () -> localStack.getEndpointOverride(LocalStackContainer.Service.SQS).toString());
        // The test's application.properties shadows the main one, so aws.queque-url must be supplied
        // here. Points at the "my-test-queue" created in the test's @BeforeAll on LocalStack.
        registry.add("aws.queque-url",
                () -> createQueueAndGetUrl());

    }

    private static String createQueueAndGetUrl() {
        try (SqsClient sqsClient = SqsClient.builder()
                .endpointOverride(localStack.getEndpointOverride(LocalStackContainer.Service.SQS))
                .region(Region.of(localStack.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(localStack.getAccessKey(), localStack.getSecretKey())))
                .build()) {

            CreateQueueResponse response = sqsClient.createQueue(
                    CreateQueueRequest.builder().queueName("my-test-queue").build());

            return response.queueUrl();
        }
    }
}
