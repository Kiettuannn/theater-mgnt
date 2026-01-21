package com.theatermgnt.theatermgnt.websocket.controller;

import java.time.Instant;

import com.nimbusds.jwt.SignedJWT;
import org.springframework.stereotype.Component;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.theatermgnt.theatermgnt.authentication.dto.request.IntrospectRequest;
import com.theatermgnt.theatermgnt.authentication.service.AuthenticationService;
import com.theatermgnt.theatermgnt.websocket.entity.WebSocketSession;
import com.theatermgnt.theatermgnt.websocket.service.WebSocketSessionService;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 * SocketHandler - Handles Socket.IO connection events
 * Manages authentication, session lifecycle, and real-time messaging
 */
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SocketHandler {
    
    SocketIOServer server;
    AuthenticationService authenticationService;
    WebSocketSessionService webSocketSessionService;

    /**
     * Handle client connection with JWT authentication
     */
    @OnConnect
    public void clientConnected(SocketIOClient client) {
        // Get token from query parameter
        String token = client.getHandshakeData().getSingleUrlParam("token");
        
        if (token == null || token.isEmpty()) {
            log.warn("Client {} attempted connection without token", client.getSessionId());
            client.disconnect();
            return;
        }

        try {
            // Verify token using authentication service
            var introspectResponse = authenticationService.introspect(
                IntrospectRequest.builder()
                    .token(token)
                    .build()
            );

            // If token is valid, create and persist session
            if (introspectResponse.isValid()) {
                log.info("Client connected: {}", client.getSessionId());
                
                // Extract accountId from token (JWT subject field contains accountId)
                String accountId = extractAccountIdFromToken(token);
                
                if (accountId != null) {
                    String socketSessionId = client.getSessionId().toString();
                    
                    // Check if session already exists (idempotent connection handling)
                    if (!webSocketSessionService.sessionExists(socketSessionId)) {
                        // Create and save WebSocket session
                        WebSocketSession session = WebSocketSession.builder()
                            .socketSessionId(socketSessionId)
                            .userId(accountId) // userId field stores accountId
                            .createdAt(Instant.now())
                            .build();
                        
                        webSocketSessionService.create(session);
                        
                        log.info("WebSocket session created for account: {}", accountId);
                    } else {
                        log.debug("WebSocket session already exists for: {}", socketSessionId);
                    }
                    
                    // Join room for targeted messaging (using accountId)
                    // Safe to call multiple times - Socket.IO handles duplicates
                    String roomName = "user:" + accountId;
                    client.joinRoom(roomName);
                    log.info("✅ Client {} joined room: {}", client.getSessionId(), roomName);
                } else {
                    log.warn("Could not extract accountId from token");
                    client.disconnect();
                }
            } else {
                log.error("Authentication failed for client: {}", client.getSessionId());
                client.disconnect();
            }
        } catch (Exception e) {
            log.error("Error during client authentication: {}", e.getMessage(), e);
            client.disconnect();
        }
    }

    /**
     * Handle client disconnection
     */
    @OnDisconnect
    public void clientDisconnected(SocketIOClient client) {
        log.info("Client disconnected: {}", client.getSessionId());
        
        try {
            // Delete session from database
            webSocketSessionService.deleteSession(client.getSessionId().toString());
            log.info("WebSocket session deleted: {}", client.getSessionId());
        } catch (Exception e) {
            log.error("Error deleting session: {}", e.getMessage(), e);
        }
    }

    /**
     * Start Socket.IO server when Spring Boot starts
     */
    @PostConstruct
    public void startServer() {
        server.start();
        server.addListeners(this);
        log.info("Socket.IO server started on port: {}", server.getConfiguration().getPort());
    }

    /**
     * Stop Socket.IO server when Spring Boot shuts down
     */
    @PreDestroy
    public void stopServer() {
        server.stop();
        log.info("Socket.IO server stopped.");
    }

    /**
     * Extract accountId from JWT token
     * The JWT subject field contains the accountId (from Account.getId())
     * 
     * Note: Using proper JWT parsing with SignedJWT library
     */
    private String extractAccountIdFromToken(String token) {
        try {
            // Parse JWT token using Nimbus JOSE library
            SignedJWT signedJWT = com.nimbusds.jwt.SignedJWT.parse(token);
            
            // Extract subject claim which contains accountId
            String accountId = signedJWT.getJWTClaimsSet().getSubject();
            
            if (accountId != null && !accountId.isEmpty()) {
                log.debug("Extracted accountId from token: {}", accountId);
                return accountId;
            } else {
                log.warn("Token subject (accountId) is null or empty");
            }
        } catch (Exception e) {
            log.error("Error extracting accountId from token: {}", e.getMessage(), e);
        }
        return null;
    }
}
