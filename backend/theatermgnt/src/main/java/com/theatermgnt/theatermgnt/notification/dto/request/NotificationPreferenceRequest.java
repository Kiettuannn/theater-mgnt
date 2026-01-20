package com.theatermgnt.theatermgnt.notification.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.theatermgnt.theatermgnt.notification.enums.NotificationCategory;
import com.theatermgnt.theatermgnt.notification.enums.RecipientType;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationPreferenceRequest {
    @NotBlank(message = "Recipient ID is required")
    String recipientId;

    @NotNull(message = "Recipient type is required")
    RecipientType recipientType;

    @NotBlank(message = "Channel ID is required")
    String channelId;

    @NotNull(message = "Category is required")
    NotificationCategory category;

    @NotNull(message = "Enabled status is required")
    Boolean isEnabled;
}
