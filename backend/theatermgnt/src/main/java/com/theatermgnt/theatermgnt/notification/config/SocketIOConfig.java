package com.theatermgnt.theatermgnt.notification.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.protocol.JacksonJsonSupport;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

/**
 * SocketIOConfig - Configuration for Socket.IO server
 * Enables real-time communication for in-app notifications
 */
@Configuration
@Slf4j
public class SocketIOConfig {

    @Value("${socketio.host:localhost}")
    private String host;

    @Value("${socketio.port:9092}")
    private Integer port;

    @Bean
    public SocketIOServer socketIOServer() {
        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
        config.setHostname(host);
        config.setPort(port);

        // CORS configuration - adjust for production
        config.setOrigin("*");

        // Connection settings
        config.setMaxFramePayloadLength(1024 * 1024);
        config.setMaxHttpContentLength(1024 * 1024);

        // Ping settings
        config.setPingInterval(25000);
        config.setPingTimeout(60000);

        // Configure custom JSON support with Java 8 date/time
        config.setJsonSupport(new CustomJsonSupport());

        log.info("Socket.IO server configured on {}:{}", host, port);

        return new SocketIOServer(config);
    }

    /**
     * Start and stop Socket.IO server with Spring Boot lifecycle
     */
    @Component
    @Slf4j
    public static class SocketIOServerRunner implements CommandLineRunner {
        private final SocketIOServer server;

        public SocketIOServerRunner(SocketIOServer server) {
            this.server = server;
        }

        @Override
        public void run(String... args) {
            server.start();
            log.info("Socket.IO server started successfully");

            // Add connection listeners
            server.addConnectListener(client -> {
                log.info("Client connected: {}", client.getSessionId());
                
                // Get userId from handshake parameters
                String userId = client.getHandshakeData().getSingleUrlParam("userId");
                if (userId != null && !userId.isEmpty()) {
                    // Join room with userId
                    client.joinRoom("user:" + userId);
                    log.info("Client {} joined room: user:{}", client.getSessionId(), userId);
                } else {
                    log.warn("Client {} connected without userId", client.getSessionId());
                }
            });

            server.addDisconnectListener(client -> {
                log.info("Client disconnected: {}", client.getSessionId());
            });
        }

        @PreDestroy
        public void stop() {
            if (server != null) {
                server.stop();
                log.info("Socket.IO server stopped");
            }
        }
    }
}
