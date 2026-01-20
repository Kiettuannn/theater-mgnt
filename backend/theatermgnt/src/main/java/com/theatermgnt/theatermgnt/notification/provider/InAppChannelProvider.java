package com.theatermgnt.theatermgnt.notification.provider;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.theatermgnt.theatermgnt.notification.dto.request.NotificationSendRequest;
import com.theatermgnt.theatermgnt.notification.dto.response.NotificationSendResult;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 * In-App notification provider
 * This provider doesn't actually "send" anywhere - notifications are saved to DB
 * and will be retrieved by the frontend when user accesses the app
 */
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class InAppChannelProvider implements NotificationChannelProvider {

    private static final String CHANNEL_NAME = "IN_APP";

    @Override
    public String getChannelName() {
        return CHANNEL_NAME;
    }

    @Override
    public NotificationSendResult send(NotificationSendRequest request) {
        try {
            log.info("Processing in-app notification for user: {}", request.getRecipientId());

            // Validate recipient
            if (request.getRecipientId() == null || request.getRecipientId().isEmpty()) {
                return NotificationSendResult.builder()
                        .success(false)
                        .status("FAILED")
                        .channelName(CHANNEL_NAME)
                        .errorMessage("Recipient ID is required")
                        .sentAt(java.time.LocalDateTime.now())
                        .build();
            }

            // For in-app notifications, we just need to ensure the notification is saved to DB
            // The actual saving happens in NotificationService, not here
            // This provider just marks it as "delivered" to in-app channel

            Map<String, Object> providerResponse = new HashMap<>();
            providerResponse.put("recipientId", request.getRecipientId());
            providerResponse.put("deliveryMethod", "DATABASE");
            providerResponse.put("message", "Notification saved to database for in-app display");

            log.info("In-app notification processed successfully for user: {}", request.getRecipientId());

            return NotificationSendResult.builder()
                    .success(true)
                    .status("SENT")
                    .channelName(CHANNEL_NAME)
                    .providerResponse(providerResponse)
                    .sentAt(java.time.LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Failed to process in-app notification for user: {}", request.getRecipientId(), e);
            return NotificationSendResult.builder()
                    .success(false)
                    .status("FAILED")
                    .channelName(CHANNEL_NAME)
                    .errorMessage(e.getMessage())
                    .sentAt(java.time.LocalDateTime.now())
                    .build();
        }
    }
}
