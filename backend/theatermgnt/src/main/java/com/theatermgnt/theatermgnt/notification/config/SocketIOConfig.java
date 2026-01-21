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
        config.setPort(port);
        
        // CORS configuration - allow all origins
        config.setOrigin("*");

        log.info("Socket.IO server configured on port: {}", port);

        return new SocketIOServer(config);
    }
}
