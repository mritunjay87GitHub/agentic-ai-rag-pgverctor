package com.mks.ai.restapi;


import com.mks.ai.dto.ChatRequest;
import com.mks.ai.dto.ChatResponse;
import com.mks.ai.service.RagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chat")
@Tag(name = "Chat", description = "Ask questions grounded in the uploaded documents")
public class ChatController {

    private final RagService ragService;

    public ChatController(RagService ragService) {
        this.ragService = ragService;
    }

    @Operation(summary = "Ask a question answered strictly from ingested documents")
    @PostMapping
    public ChatResponse ask(@Valid @RequestBody ChatRequest request) {
        return ragService.answer(request.question());
    }
}
