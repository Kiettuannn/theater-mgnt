package com.theatermgnt.theatermgnt.websocket.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * WebSocketSession - Entity for tracking active WebSocket connections
 * Maps Socket.IO session IDs to user IDs for targeted messaging
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WebSocketSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    
    @Column(name = "socket_session_id", nullable = false, unique = true)
    String socketSessionId;
    
    @Column(name = "user_id", nullable = false)
    String userId;
    
    @Column(name = "created_at", nullable = false)
    Instant createdAt;
}
