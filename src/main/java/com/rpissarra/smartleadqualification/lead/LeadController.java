package com.rpissarra.smartleadqualification.lead;

import com.rpissarra.smartleadqualification.message.MessageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Leads API")
@RestController
@RequestMapping("/api/v1/leads")
public class LeadController {

    private final LeadService leadService;

    public LeadController(LeadService leadService) {
        this.leadService = leadService;
    }

    @Operation(summary = "Get all leads", description = "Returns a collection of leads or an empty collection")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = LeadResponse.class))
                    )
            )
    })
    @GetMapping
    public List<LeadResponse> getAllLeads(
            @ParameterObject
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.ASC)
            Pageable pageable
            ) {
        return leadService.getAllLeads(pageable);
    }

    @Operation(summary = "Get a lead by id", description = "Returns a lead as per the id")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MessageResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not found - The lead was not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<LeadResponse> getLeadById(
            @PathVariable @Parameter(name = "id", description = "Lead id", example = "1") Long id
    ) {
        return ResponseEntity.ok(leadService.getLeadById(id));
    }
}
