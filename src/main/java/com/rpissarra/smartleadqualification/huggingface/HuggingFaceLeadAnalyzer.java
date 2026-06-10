package com.rpissarra.smartleadqualification.huggingface;

import com.rpissarra.smartleadqualification.lead.Lead;
import com.rpissarra.smartleadqualification.message.Message;

import java.util.Optional;


public interface HuggingFaceLeadAnalyzer {


    Optional<Lead> analyzeMessage(Message message);
}
