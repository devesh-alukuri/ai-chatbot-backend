package com.devesh.chatbot.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ChatDTO {
    @Data public static class ChatRequest {
        @NotBlank private String userId;
        @NotBlank private String sessionId;
        @NotBlank private String message;
    }

    @Data public static class ChatResponse {
        private String sessionId;
        private String response;
        private String intent;
        private Map<String, String> entities;
        private Double confidence;
        private LocalDateTime timestamp;
        private Long processingTimeMs;
    }

    @Data public static class HistoryResponse {
        private String sessionId;
        private List<MessagePair> messages;
    }

    @Data public static class MessagePair {
        private String userMessage;
        private String botResponse;
        private String intent;
        private LocalDateTime timestamp;
    }
}
