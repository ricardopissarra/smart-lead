package com.rpissarra.smartleadqualification.configuration;

import com.rpissarra.smartleadqualification.huggingface.HuggingFaceService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.ImportHttpServices;
import tools.jackson.databind.ObjectMapper;

@Configuration
@ImportHttpServices(group = "huggingFace", types = HuggingFaceService.class)
public class HttpClientConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public RestClientHttpServiceGroupConfigurer restClientHttpServiceGroupConfigurer(HuggingFacesConfiguration huggingFacesConfiguration) {
        return groups ->
            groups
                    .filterByName("huggingFace")
                    .forEachClient((g, builder) ->
                        builder.baseUrl("https://router.huggingface.co/v1")
                                .defaultHeader("Authorization", "Bearer %s".formatted(huggingFacesConfiguration.getToken()))
                                .build()
                    );
    }
}
