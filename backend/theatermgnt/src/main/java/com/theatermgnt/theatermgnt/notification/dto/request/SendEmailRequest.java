package com.theatermgnt.theatermgnt.notification.dto.request;

import java.util.List;

import lombok.*;
import lombok.experimental.FieldDefaults;
import sibModel.SendSmtpEmailAttachment;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SendEmailRequest {
    Recipient to;
    String subject;
    String htmlContent;
    List<SendSmtpEmailAttachment> attachments;
}
