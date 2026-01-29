package com.theatermgnt.theatermgnt.notification.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.corundumstudio.socketio.SocketIOServer;

import lombok.extern.slf4j.Slf4j;

/**
 * SocketIOConfig - Configuration for Socket.IO server
 * Enables real-time communication for in-app notifications and chat
 */
@Configuration
@Slf4j
public class SocketIOConfig {

    @Value("${socketio.port:9092}")
    private Integer port;

    @Bean
    public SocketIOServer socketIOServer() {
        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();

        // Bind to all network interfaces (0.0.0.0) to be accessible from outside
        config.setHostname("0.0.0.0");
        config.setPort(port);

        // CORS configuration - allow all origins
        config.setOrigin("*");

        // Enable WebSocket and Polling transports
        config.setTransports(
                com.corundumstudio.socketio.Transport.WEBSOCKET, com.corundumstudio.socketio.Transport.POLLING);

        // Connection settings
        config.setUpgradeTimeout(10000); // 10 seconds
        config.setPingTimeout(60000); // 60 seconds
        config.setPingInterval(25000); // 25 seconds
        config.setMaxFramePayloadLength(1048576); // 1MB
        config.setMaxHttpContentLength(1048576); // 1MB

        // Allow credentials for CORS
        config.setAllowCustomRequests(true);

        log.info("Socket.IO server configured on hostname: 0.0.0.0, port: {}", port);
        log.info("Enabled transports: WEBSOCKET, POLLING");

        return new SocketIOServer(config);
    }
}
