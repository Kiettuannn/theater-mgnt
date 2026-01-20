package com.theatermgnt.theatermgnt.notification.provider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.theatermgnt.theatermgnt.notification.dto.request.NotificationSendRequest;
import com.theatermgnt.theatermgnt.notification.dto.request.Recipient;
import com.theatermgnt.theatermgnt.notification.dto.request.SendEmailRequest;
import com.theatermgnt.theatermgnt.notification.dto.response.EmailResponse;
import com.theatermgnt.theatermgnt.notification.dto.response.NotificationSendResult;
import com.theatermgnt.theatermgnt.notification.service.EmailService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import sibModel.SendSmtpEmailAttachment;

/**
 * Email channel provider that sends notifications via email
 * Wraps the existing EmailService
 */
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class EmailChannelProvider implements NotificationChannelProvider {
    EmailService emailService;

    private static final String CHANNEL_NAME = "EMAIL";

    @Override
    public String getChannelName() {
        return CHANNEL_NAME;
    }

    @Override
    public NotificationSendResult send(NotificationSendRequest request) {
        try {
            log.info("Sending email notification to: {}", request.getRecipientEmail());

            // Validate email recipient
            if (request.getRecipientEmail() == null
                    || request.getRecipientEmail().isEmpty()) {
                return NotificationSendResult.builder()
                        .success(false)
                        .status("FAILED")
                        .channelName(CHANNEL_NAME)
                        .errorMessage("Recipient email is required")
                        .sentAt(java.time.LocalDateTime.now())
                        .build();
            }

            // Build recipient
            Recipient recipient = Recipient.builder()
                    .email(request.getRecipientEmail())
                    .name(request.getRecipientName())
                    .build();

            // Extract attachments from metadata if any
            List<SendSmtpEmailAttachment> attachments = null;
            if (request.getMetadata() != null && request.getMetadata().containsKey("attachments")) {
                attachments =
                        (List<SendSmtpEmailAttachment>) request.getMetadata().get("attachments");
            }

            // Build email request
            SendEmailRequest emailRequest = SendEmailRequest.builder()
                    .to(recipient)
                    .subject(request.getTitle())
                    .htmlContent(request.getContent())
                    .attachments(attachments)
                    .build();

            // Send email using existing EmailService
            EmailResponse emailResponse = emailService.sendEmail(emailRequest);

            // Build provider response
            Map<String, Object> providerResponse = new HashMap<>();
            providerResponse.put("messageId", emailResponse.getMessageId());

            log.info(
                    "Email sent successfully to: {}, messageId: {}",
                    request.getRecipientEmail(),
                    emailResponse.getMessageId());

            return NotificationSendResult.builder()
                    .success(true)
                    .status("SENT")
                    .channelName(CHANNEL_NAME)
                    .providerResponse(providerResponse)
                    .sentAt(java.time.LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Failed to send email notification to: {}", request.getRecipientEmail(), e);
            return NotificationSendResult.builder()
                    .success(false)
                    .status("FAILED")
                    .channelName(CHANNEL_NAME)
                    .errorMessage(e.getMessage())
                    .sentAt(java.time.LocalDateTime.now())
                    .build();
        }
    }
}
