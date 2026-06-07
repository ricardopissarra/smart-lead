package com.rpissarra.smartleadqualification.message;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
class MessageRepositoryTest {

    @Autowired
    private MessageRepository underTest;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:18-alpine")
    );

    @BeforeEach
    void setUp() {
        underTest.deleteAll();
    }

    @DisplayName("Find message by status returns result")
    @Test
    void findMessagesByStatus() {
        // given
        List<Message> messages = List.of(
                Message.builder().content("message").status(Status.CREATED).build(),
                Message.builder().content("create lead").status(Status.PROCESSED).build(),
                Message.builder().content("failed").status(Status.FAILED).build(),
                Message.builder().content("failed 2").status(Status.FAILED).build()
        );
        underTest.saveAll(messages);
        // when
        List<Message> actual = underTest.findMessagesByStatus(Status.FAILED);
        // then
        assertEquals(2, actual.size());
    }

    @DisplayName("Find message by status returns empty list")
    @Test
    void findMessagesByStatusReturnsEmptyList() {
        // given
        List<Message> messages = List.of(
                Message.builder().content("message").status(Status.CREATED).build(),
                Message.builder().content("create lead").status(Status.CREATED).build(),
                Message.builder().content("failed").status(Status.FAILED).build(),
                Message.builder().content("failed 2").status(Status.FAILED).build()
        );
        underTest.saveAll(messages);
        // when
        List<Message> actual = underTest.findMessagesByStatus(Status.PROCESSED);
        // then
        assertEquals(0, actual.size());
        assertEquals(Collections.EMPTY_LIST, actual);
    }
}