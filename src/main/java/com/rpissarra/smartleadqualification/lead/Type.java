package com.rpissarra.smartleadqualification.lead;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Defines the type of lead", example = "PRICING_INQUIRY")
public enum Type {
    DEMO_REQUEST,
    PRICING_INQUIRY,
    PARTNERSHIP,
    SUPPORT,
    OTHER
}