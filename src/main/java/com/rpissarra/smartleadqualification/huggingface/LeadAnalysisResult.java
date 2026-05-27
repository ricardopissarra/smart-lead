package com.rpissarra.smartleadqualification.huggingface;

import com.rpissarra.smartleadqualification.lead.Type;
import com.rpissarra.smartleadqualification.lead.UrgencyLevel;

public record LeadAnalysisResult(
        boolean shouldCreateLead,
        String title,
        Type type,
        UrgencyLevel urgencyLevel,
        String description
) {
}
