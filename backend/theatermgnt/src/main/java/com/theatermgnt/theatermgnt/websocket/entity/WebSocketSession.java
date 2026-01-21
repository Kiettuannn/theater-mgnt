package com.theatermgnt.theatermgnt.websocket.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
public class WebSocketSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "socket_session_id", nullable = false, unique = true)
    private String socketSessionId;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
