package com.theatermgnt.theatermgnt.notification.dto.response;

import com.theatermgnt.theatermgnt.notification.enums.NotificationCategory;
import com.theatermgnt.theatermgnt.notification.enums.RecipientType;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationPreferenceResponse {
    String id;
    String recipientId;
    RecipientType recipientType;
    String channelId;
    String channelName;
    NotificationCategory category;
    Boolean isEnabled;
}
