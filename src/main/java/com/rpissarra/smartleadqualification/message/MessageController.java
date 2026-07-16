package com.rpissarra.smartleadqualification.message;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Messages API")
@RestController
@RequestMapping("/api/v1/messages")
public class MessageController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageController.class);

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @Operation(summary = "Get all messages", description = "Returns a collection of messages or an empty collection")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                             array = @ArraySchema(schema = @Schema(implementation = MessageResponse.class))
                    )
            )
    })
    @GetMapping
    public List<MessageResponse> getAllMessages(
            @ParameterObject
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.ASC)
            Pageable pageable

    ) {
        LOGGER.info("Getting all messages");
        return messageService.getAllMessages(pageable);
    }

    @Operation(summary = "Create a new message", description = "Creates a new message and returns the MessageResponse schema")
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
                    responseCode = "400",
                    description = "Bad request - malformed request body",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
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
    @PostMapping
    public ResponseEntity<MessageResponse> createNewMessage(
            @Valid @RequestBody MessageRequest messageRequest
    ) {
        LOGGER.info("Creating new message: {}", messageRequest.content());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        messageService.createNewMessage(messageRequest)
                );
    }
}
