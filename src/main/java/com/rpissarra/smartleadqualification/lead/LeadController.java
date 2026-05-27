package com.rpissarra.smartleadqualification.lead;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.swing.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/leads")
public class LeadController {

    private final LeadService leadService;

    public LeadController(LeadService leadService) {
        this.leadService = leadService;
    }

    @GetMapping
    public List<LeadResponse> getAllLeads(
            @PageableDefault(direction = Sort.Direction.ASC) Pageable pageable
            ) {
        return leadService.getAllLeads(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeadResponse> getLeadById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(leadService.getLeadById(id));
    }
}
