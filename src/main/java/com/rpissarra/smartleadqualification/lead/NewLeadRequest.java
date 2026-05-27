package com.rpissarra.smartleadqualification.lead;

public record NewLeadRequest(String title,
                             Type type,
                             UrgencyLevel urgencyLevel,
                             String description) {

    public static Lead toLead(NewLeadRequest request) {
        return Lead.builder()
                .title(request.title())
                .type(request.type())
                .urgencyLevel(request.urgencyLevel())
                .description(request.description())
                .build();
    }
}
