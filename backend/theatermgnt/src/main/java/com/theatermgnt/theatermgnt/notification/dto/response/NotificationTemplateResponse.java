package com.theatermgnt.theatermgnt.notification.dto.response;

import java.time.LocalDateTime;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationTemplateResponse {
    String id;
    String templateCode;
    String titleTemplate;
    String contentTemplate;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
