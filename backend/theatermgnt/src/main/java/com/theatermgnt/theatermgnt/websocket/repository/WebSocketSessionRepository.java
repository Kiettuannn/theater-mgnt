package com.theatermgnt.theatermgnt.websocket.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.theatermgnt.theatermgnt.websocket.entity.WebSocketSession;

/**
 * WebSocketSessionRepository - Repository for WebSocketSession entity
 */
@Repository
public interface WebSocketSessionRepository extends JpaRepository<WebSocketSession, String> {
    
    /**
     * Find session by socket session ID
     */
    Optional<WebSocketSession> findBySocketSessionId(String socketSessionId);
    
    /**
     * Find all sessions for a specific user
     */
    List<WebSocketSession> findByUserId(String userId);
    
    /**
     * Delete session by socket session ID
     */
    void deleteBySocketSessionId(String socketSessionId);
    
    /**
     * Check if a socket session exists
     */
    boolean existsBySocketSessionId(String socketSessionId);
}
