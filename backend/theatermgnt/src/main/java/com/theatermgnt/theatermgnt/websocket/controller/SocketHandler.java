package com.theatermgnt.theatermgnt.websocket.controller;

import java.time.Instant;

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
                
                // Extract userId from token (assuming it's in the token claims)
                // You may need to adjust this based on your token structure
                String userId = extractUserIdFromToken(token);
                
                if (userId != null) {
                    // Create and save WebSocket session
                    WebSocketSession session = WebSocketSession.builder()
                        .socketSessionId(client.getSessionId().toString())
                        .userId(userId)
                        .createdAt(Instant.now())
                        .build();
                    
                    webSocketSessionService.create(session);
                    
                    // Join room for targeted messaging
                    client.joinRoom("user:" + userId);
                    
                    log.info("WebSocket session created for user: {}", userId);
                } else {
                    log.warn("Could not extract userId from token");
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
     * Extract userId from JWT token
     * This is a placeholder - implement based on your token structure
     */
    private String extractUserIdFromToken(String token) {
        try {
            // Parse JWT token and extract userId
            // You can use JWT libraries or your existing token parsing logic
            String[] parts = token.split("\\.");
            if (parts.length == 3) {
                // Decode payload and extract userId
                // This is simplified - use proper JWT parsing in production
                String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
                
                // Extract userId from payload JSON
                // Example: {"sub":"userId","exp":1234567890}
                if (payload.contains("\"sub\":")) {
                    int startIndex = payload.indexOf("\"sub\":\"") + 7;
                    int endIndex = payload.indexOf("\"", startIndex);
                    if (endIndex > startIndex) {
                        return payload.substring(startIndex, endIndex);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error extracting userId from token: {}", e.getMessage());
        }
        return null;
    }
}
