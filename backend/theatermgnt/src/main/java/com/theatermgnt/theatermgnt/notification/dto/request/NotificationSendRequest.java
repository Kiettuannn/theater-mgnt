package com.theatermgnt.theatermgnt.notification.dto.request;

import java.util.Map;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Request DTO for sending notifications through any channel
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationSendRequest {
    String recipientId;
    String recipientEmail;
    String recipientName;

    String templateCode;
    String title;
    String content;

    Map<String, Object> metadata;
    Map<String, Object> channelConfig;
}
