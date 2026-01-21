package com.theatermgnt.theatermgnt.notification.dto.request;

import jakarta.validation.constraints.NotBlank;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationTemplateRequest {
    @NotBlank(message = "Template code is required")
    String templateCode;

    @NotBlank(message = "Title template is required")
    String titleTemplate;

    @NotBlank(message = "Content template is required")
    String contentTemplate;
}
