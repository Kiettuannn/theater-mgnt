package com.theatermgnt.theatermgnt.notification.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Result DTO after sending notification through a channel
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationSendResult {
    boolean success;
    String status; // "SENT", "FAILED", "PENDING"
    String message;
    String channelName;

    Map<String, Object> providerResponse;
    LocalDateTime sentAt;
    String errorCode;
    String errorMessage;
}
