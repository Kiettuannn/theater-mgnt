package com.theatermgnt.theatermgnt.websocket.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.theatermgnt.theatermgnt.websocket.entity.WebSocketSession;
import com.theatermgnt.theatermgnt.websocket.repository.WebSocketSessionRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 * WebSocketSessionService - Service for managing WebSocket sessions
 * Handles session creation, retrieval, and cleanup
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class WebSocketSessionService {
    
    WebSocketSessionRepository webSocketSessionRepository;
    
    /**
     * Create a new WebSocket session
     */
    @Transactional
    public WebSocketSession create(WebSocketSession session) {
        log.info("Creating WebSocket session for user: {}", session.getUserId());
        return webSocketSessionRepository.save(session);
    }
    
    /**
     * Find session by socket session ID
     */
    public WebSocketSession findBySocketSessionId(String socketSessionId) {
        return webSocketSessionRepository.findBySocketSessionId(socketSessionId)
                .orElse(null);
    }
    
    /**
     * Find all sessions for a user
     */
    public List<WebSocketSession> findByUserId(String userId) {
        return webSocketSessionRepository.findByUserId(userId);
    }
    
    /**
     * Delete session by socket session ID
     */
    @Transactional
    public void deleteSession(String socketSessionId) {
        log.info("Deleting WebSocket session: {}", socketSessionId);
        webSocketSessionRepository.deleteBySocketSessionId(socketSessionId);
    }
    
    /**
     * Delete all sessions for a user
     */
    @Transactional
    public void deleteAllUserSessions(String userId) {
        log.info("Deleting all WebSocket sessions for user: {}", userId);
        List<WebSocketSession> sessions = webSocketSessionRepository.findByUserId(userId);
        webSocketSessionRepository.deleteAll(sessions);
    }
    
    /**
     * Check if session exists
     */
    public boolean sessionExists(String socketSessionId) {
        return webSocketSessionRepository.existsBySocketSessionId(socketSessionId);
    }
}
