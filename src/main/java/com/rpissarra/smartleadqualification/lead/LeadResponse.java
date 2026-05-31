package com.rpissarra.smartleadqualification.lead;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "LeadResponse", description = "Lead schema returned by the api")
public record LeadResponse(
        @Schema(description = "The id of the lead", example = "1")
        Long id,
        @Schema(description = "Lead title", example = "Pricing lead")
        String title,
        @Schema(description = "Type of the lead", example = "PRICE_INQUIRY")
        Type type,
        @Schema(description = "Urgency level of the lead", example = "HIGH")
        UrgencyLevel urgencyLevel,
        @Schema(description = "The description", example = "User wants to get the pricing for pro plan")
        String description
) {

    public static LeadResponse toLeadResponse(Lead lead) {
        return new LeadResponse(
                lead.getId(),
                lead.getTitle(),
                lead.getType(),
                lead.getUrgencyLevel(),
                lead.getDescription()
        );
    }
}
