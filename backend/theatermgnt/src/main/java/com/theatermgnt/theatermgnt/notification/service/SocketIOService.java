package com.theatermgnt.theatermgnt.notification.service;

import org.springframework.stereotype.Service;

import com.corundumstudio.socketio.SocketIOServer;
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

    /**
     * Emit notification to specific user
     * Client should join room with their userId when connecting
     */
    public void emitNotificationToUser(String userId, NotificationDetailResponse notification) {
        log.info("Emitting notification to user {}: {}", userId, notification.getId());

        try {
            String roomName = "user:" + userId;
            socketServer.getRoomOperations(roomName).sendEvent("notification:new", notification);

            log.info("Notification emitted successfully to room: {}", roomName);
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
            socketServer.getBroadcastOperations().sendEvent("notification:new", notification);
            log.info("Notification broadcasted successfully");
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
