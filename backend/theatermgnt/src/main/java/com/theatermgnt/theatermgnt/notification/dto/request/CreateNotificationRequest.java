package com.theatermgnt.theatermgnt.notification.dto.request;

import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import com.theatermgnt.theatermgnt.notification.enums.NotificationCategory;
import com.theatermgnt.theatermgnt.notification.enums.Priority;
import com.theatermgnt.theatermgnt.notification.enums.RecipientType;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateNotificationRequest {
    @NotBlank(message = "Template code is required")
    String templateCode;

    @NotBlank(message = "Recipient ID is required")
    String recipientId;

    @NotNull(message = "Recipient type is required")
    RecipientType recipientType;

    @NotNull(message = "Category is required")
    NotificationCategory category;

    @NotEmpty(message = "At least one channel is required")
    List<String> channels; // ["EMAIL", "IN_APP"]

    Map<String, Object> metadata; // Variables for template rendering

    Priority priority; // Optional, will use template's priority if not provided
}
