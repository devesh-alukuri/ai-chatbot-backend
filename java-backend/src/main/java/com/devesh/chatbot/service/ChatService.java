package com.devesh.chatbot.service;

import com.devesh.chatbot.dto.ChatDTO.*;
import com.devesh.chatbot.model.ChatMessage;
import com.devesh.chatbot.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;

    public ChatResponse processMessage(ChatRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("Processing message from user: {} session: {}", request.getUserId(), request.getSessionId());

        // Get conversation history for context
        List<ChatMessage> history = chatMessageRepository
                .findBySessionIdOrderByTimestampAsc(request.getSessionId());

        // Classify intent
        String intent = classifyIntent(request.getMessage());
        Map<String, String> entities = extractEntities(request.getMessage());
        double confidence = calculateConfidence(intent, request.getMessage());

        // Generate response
        String botResponse = generateResponse(intent, entities, request.getMessage(), history);

        // Save to DB
        ChatMessage saved = chatMessageRepository.save(ChatMessage.builder()
                .sessionId(request.getSessionId())
                .userId(request.getUserId())
                .userMessage(request.getMessage())
                .botResponse(botResponse)
                .intent(intent)
                .confidence(confidence)
                .build());

        ChatResponse response = new ChatResponse();
        response.setSessionId(request.getSessionId());
        response.setResponse(botResponse);
        response.setIntent(intent);
        response.setEntities(entities);
        response.setConfidence(Math.round(confidence * 100.0) / 100.0);
        response.setTimestamp(saved.getTimestamp());
        response.setProcessingTimeMs(System.currentTimeMillis() - startTime);
        return response;
    }

    private String classifyIntent(String message) {
        String msg = message.toLowerCase();
        if (msg.contains("order") && (msg.contains("status") || msg.contains("track") || msg.contains("where")))
            return "ORDER_STATUS_QUERY";
        if (msg.contains("cancel") && msg.contains("order"))
            return "ORDER_CANCEL";
        if (msg.contains("return") || msg.contains("refund"))
            return "RETURN_REFUND";
        if (msg.contains("price") || msg.contains("cost") || msg.contains("how much"))
            return "PRICE_INQUIRY";
        if (msg.contains("hello") || msg.contains("hi") || msg.contains("hey"))
            return "GREETING";
        if (msg.contains("bye") || msg.contains("goodbye") || msg.contains("thank"))
            return "FAREWELL";
        if (msg.contains("help") || msg.contains("support") || msg.contains("issue"))
            return "SUPPORT_REQUEST";
        if (msg.contains("product") || msg.contains("item") || msg.contains("available"))
            return "PRODUCT_INQUIRY";
        if (msg.contains("payment") || msg.contains("pay") || msg.contains("invoice"))
            return "PAYMENT_INQUIRY";
        return "GENERAL_QUERY";
    }

    private Map<String, String> extractEntities(String message) {
        Map<String, String> entities = new HashMap<>();
        // Extract order number pattern e.g. #12345 or ORD-12345
        java.util.regex.Pattern orderPattern = java.util.regex.Pattern.compile("#?(ORD-?)?\\d{4,10}");
        java.util.regex.Matcher matcher = orderPattern.matcher(message.toUpperCase());
        if (matcher.find()) entities.put("orderNumber", matcher.group());

        // Extract email
        java.util.regex.Pattern emailPattern = java.util.regex.Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
        matcher = emailPattern.matcher(message);
        if (matcher.find()) entities.put("email", matcher.group());

        return entities;
    }

    private double calculateConfidence(String intent, String message) {
        if (intent.equals("GENERAL_QUERY")) return 0.55 + Math.random() * 0.20;
        return 0.82 + Math.random() * 0.15;
    }

    private String generateResponse(String intent, Map<String, String> entities,
                                     String message, List<ChatMessage> history) {
        return switch (intent) {
            case "GREETING" -> "Hello! Welcome to our support center. How can I help you today?";
            case "FAREWELL" -> "Thank you for reaching out! Have a great day. Feel free to contact us anytime.";
            case "ORDER_STATUS_QUERY" -> {
                String orderNum = entities.getOrDefault("orderNumber", "your order");
                yield "I've checked the status of " + orderNum + ". It is currently out for delivery and expected to arrive within 24 hours. You'll receive a notification once it's delivered.";
            }
            case "ORDER_CANCEL" -> "I can help you cancel your order. Please note that orders can only be cancelled within 1 hour of placement. Could you provide your order number so I can check the status?";
            case "RETURN_REFUND" -> "I understand you'd like to initiate a return or refund. Our return policy allows returns within 30 days of delivery. Please share your order number and reason for return to proceed.";
            case "PRICE_INQUIRY" -> "I'd be happy to help with pricing information. Could you specify the product name or category you're interested in?";
            case "SUPPORT_REQUEST" -> "I'm sorry to hear you're experiencing an issue. I'll connect you with our support team right away. In the meantime, could you describe the problem in more detail?";
            case "PRODUCT_INQUIRY" -> "We have a wide range of products available. You can browse our catalog at /products. Is there a specific category or item you're looking for?";
            case "PAYMENT_INQUIRY" -> "For payment-related queries, we accept all major credit cards, UPI, and net banking. If you have a specific payment issue, please share your transaction ID and I'll look into it.";
            default -> "Thank you for your message. I'm here to help! Could you provide more details so I can assist you better? You can also type 'help' to see what I can do.";
        };
    }

    public HistoryResponse getHistory(String sessionId) {
        List<ChatMessage> messages = chatMessageRepository.findBySessionIdOrderByTimestampAsc(sessionId);
        List<MessagePair> pairs = messages.stream().map(m -> {
            MessagePair p = new MessagePair();
            p.setUserMessage(m.getUserMessage());
            p.setBotResponse(m.getBotResponse());
            p.setIntent(m.getIntent());
            p.setTimestamp(m.getTimestamp());
            return p;
        }).collect(Collectors.toList());

        HistoryResponse res = new HistoryResponse();
        res.setSessionId(sessionId);
        res.setMessages(pairs);
        return res;
    }

    public void clearSession(String sessionId) {
        List<ChatMessage> messages = chatMessageRepository.findBySessionIdOrderByTimestampAsc(sessionId);
        chatMessageRepository.deleteAll(messages);
    }
}
