package com.rpissarra.smartleadqualification.lead;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/leads")
public class LeadController {

    private final LeadService leadService;

    public LeadController(LeadService leadService) {
        this.leadService = leadService;
    }

    @GetMapping
    public List<LeadResponse> getAllLeads() {
        return leadService.getAllLeads();
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeadResponse> getLeadById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(leadService.getLeadById(id));
    }
}
