package com.rpissarra.smartleadqualification.lead;

import com.rpissarra.smartleadqualification.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LeadService {

    private final LeadRepository leadRepository;

    public LeadService(LeadRepository leadRepository) {
        this.leadRepository = leadRepository;
    }

    public List<LeadResponse> getAllLeads() {
        return leadRepository.findAll().stream()
                .map(LeadResponse::toLeadResponse)
                .toList();
    }

    public LeadResponse getLeadById(Long id) {
        return leadRepository.findById(id)
                .map(LeadResponse::toLeadResponse)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Not lead found with id: %d".formatted(id),
                                HttpStatus.NOT_FOUND
                        )
                );
    }

    public Lead createNewLead(NewLeadRequest leadRequest) {
        return leadRepository.save(NewLeadRequest.toLead(leadRequest));
    }
}
