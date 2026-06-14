package com.devesh.chatbot.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ChatMessage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private String sessionId;
    @Column(nullable = false) private String userId;
    @Column(columnDefinition = "TEXT", nullable = false) private String userMessage;
    @Column(columnDefinition = "TEXT") private String botResponse;
    private String intent;
    private Double confidence;
    @CreationTimestamp private LocalDateTime timestamp;
}
