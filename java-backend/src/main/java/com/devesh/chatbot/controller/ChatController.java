package com.devesh.chatbot.controller;

import com.devesh.chatbot.dto.ChatDTO.*;
import com.devesh.chatbot.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "Chatbot API", description = "AI-powered chatbot with NLP intent recognition")
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/message")
    @Operation(summary = "Send a message and get bot response")
    public ResponseEntity<ChatResponse> sendMessage(@Valid @RequestBody ChatRequest request) {
        return ResponseEntity.ok(chatService.processMessage(request));
    }

    @GetMapping("/history/{sessionId}")
    @Operation(summary = "Get full conversation history for a session")
    public ResponseEntity<HistoryResponse> getHistory(@PathVariable String sessionId) {
        return ResponseEntity.ok(chatService.getHistory(sessionId));
    }

    @DeleteMapping("/session/{sessionId}")
    @Operation(summary = "Clear a chat session")
    public ResponseEntity<Void> clearSession(@PathVariable String sessionId) {
        chatService.clearSession(sessionId);
        return ResponseEntity.noContent().build();
    }
}
