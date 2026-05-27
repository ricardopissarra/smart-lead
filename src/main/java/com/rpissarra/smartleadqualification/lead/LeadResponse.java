package com.rpissarra.smartleadqualification.lead;

public record LeadResponse(
        String title,
        Type type,
        UrgencyLevel urgencyLevel,
        String description
) {

    public static LeadResponse toLeadResponse(Lead lead) {
        return new LeadResponse(
                lead.getTitle(),
                lead.getType(),
                lead.getUrgencyLevel(),
                lead.getDescription()
        );
    }
}
