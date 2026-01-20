package com.theatermgnt.theatermgnt.notification.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.theatermgnt.theatermgnt.notification.enums.NotificationCategory;
import com.theatermgnt.theatermgnt.notification.enums.NotificationStatus;
import com.theatermgnt.theatermgnt.notification.enums.Priority;
import com.theatermgnt.theatermgnt.notification.enums.RecipientType;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationDetailResponse {
    String id;
    String templateCode;
    String recipientId;
    RecipientType recipientType;
    NotificationCategory category;
    Priority priority;
    NotificationStatus status;
    String title;
    String content;
    Map<String, Object> metadata;
    Boolean isRead;
    LocalDateTime readAt;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    List<NotificationLogResponse> logs;
}
