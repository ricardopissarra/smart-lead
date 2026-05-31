package com.rpissarra.smartleadqualification.lead;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Defines the urgency level of the lead", example = "HIGH")
public enum UrgencyLevel {
    LOW,
    MEDIUM,
    HIGH
}
