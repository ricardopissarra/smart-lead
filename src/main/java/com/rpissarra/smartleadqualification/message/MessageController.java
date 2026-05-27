package com.rpissarra.smartleadqualification.message;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping
    public List<MessageResponse> getAllMessages() {
        return messageService.getAllMessages();
    }

    @PostMapping
    public ResponseEntity<MessageResponse> createNewMessage(
            @Valid @RequestBody MessageRequest messageRequest
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        messageService.createNewMessage(messageRequest)
                );
    }
}
