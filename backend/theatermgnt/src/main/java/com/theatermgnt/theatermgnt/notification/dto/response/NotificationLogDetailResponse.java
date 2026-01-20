package com.theatermgnt.theatermgnt.notification.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationLogDetailResponse {
    String id;
    String notificationId;
    String notificationTitle;
    String channelName;
    String status;
    Map<String, Object> providerResponse;
    LocalDateTime sentAt;
}
