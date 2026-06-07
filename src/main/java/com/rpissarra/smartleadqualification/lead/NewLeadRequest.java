package com.rpissarra.smartleadqualification.lead;

import com.rpissarra.smartleadqualification.message.Message;

public record NewLeadRequest(String title,
                             Type type,
                             UrgencyLevel urgencyLevel,
                             String description,
                             Message message) {

    public static Lead toLead(NewLeadRequest request) {
        return Lead.builder()
                .title(request.title())
                .type(request.type())
                .urgencyLevel(request.urgencyLevel())
                .description(request.description())
                .message(request.message())
                .build();
    }
}
