package com.rpissarra.smartleadqualification.huggingface;

import com.rpissarra.smartleadqualification.lead.Lead;
import com.rpissarra.smartleadqualification.message.Message;


public interface HuggingFaceLeadAnalyzer {


    Lead analyzeMessage(Message message);
}
