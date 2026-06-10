package com.rpissarra.smartleadqualification.configuration;

import org.springframework.ai.chat.client.ChatClient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class AiConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("""
                        You are a lead qualification assistant. Analyze the incoming
                        message and determine if it is a qualified sales lead or just a general
                        inquiry.
                        """)
                .build();
    }
}
