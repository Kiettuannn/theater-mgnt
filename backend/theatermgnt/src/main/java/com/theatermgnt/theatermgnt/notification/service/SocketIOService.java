package com.theatermgnt.theatermgnt.notification.service;

import org.springframework.stereotype.Service;

import com.corundumstudio.socketio.SocketIOServer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theatermgnt.theatermgnt.notification.dto.response.NotificationDetailResponse;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 * SocketIOService - Service for emitting Socket.IO events
 * Handles real-time notification broadcasting
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SocketIOService {
    SocketIOServer socketServer;
    ObjectMapper objectMapper;

    /**
     * Emit notification to specific user
     * Client should join room with their userId when connecting
     */
    public void emitNotificationToUser(String userId, NotificationDetailResponse notification) {
        log.info("Emitting notification to user {}: {}", userId, notification.getId());

        try {
            String roomName = "user:" + userId;
            
            // Check how many clients are in this room
            var room = socketServer.getRoomOperations(roomName);
            var clients = room.getClients();
            log.info("📡 Room '{}' has {} clients", roomName, clients.size());
            
            if (clients.isEmpty()) {
                log.warn("⚠️ No clients in room '{}' - notification will not be delivered", roomName);
                return;
            }
            
            // Serialize to JSON string first (like chat-service does)
            // This uses Spring's ObjectMapper which already has JavaTimeModule configured
            String notificationJson = objectMapper.writeValueAsString(notification);
            room.sendEvent("notification:new", notificationJson);

            log.info("Notification emitted successfully to room: {}", roomName);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize notification {}: {}", notification.getId(), e.getMessage(), e);
        } catch (Exception e) {
            log.error("Failed to emit notification to user {}: {}", userId, e.getMessage(), e);
        }
    }

    /**
     * Broadcast notification to all connected clients
     */
    public void broadcastNotification(NotificationDetailResponse notification) {
        log.info("Broadcasting notification to all clients: {}", notification.getId());

        try {
            // Serialize to JSON string first
            String notificationJson = objectMapper.writeValueAsString(notification);
            socketServer.getBroadcastOperations().sendEvent("notification:new", notificationJson);
            log.info("Notification broadcasted successfully");
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize notification {}: {}", notification.getId(), e.getMessage(), e);
        } catch (Exception e) {
            log.error("Failed to broadcast notification: {}", e.getMessage(), e);
        }
    }

    /**
     * Check if Socket.IO server is running
     */
    public boolean isServerRunning() {
        return socketServer != null;
    }
}
