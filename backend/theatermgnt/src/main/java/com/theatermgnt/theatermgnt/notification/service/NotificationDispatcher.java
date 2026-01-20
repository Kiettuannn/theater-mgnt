package com.theatermgnt.theatermgnt.notification.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.theatermgnt.theatermgnt.account.entity.Account;
import com.theatermgnt.theatermgnt.account.repository.AccountRepository;
import com.theatermgnt.theatermgnt.notification.dto.request.NotificationSendRequest;
import com.theatermgnt.theatermgnt.notification.dto.response.NotificationSendResult;
import com.theatermgnt.theatermgnt.notification.entity.Notification;
import com.theatermgnt.theatermgnt.notification.entity.NotificationLog;
import com.theatermgnt.theatermgnt.notification.enums.NotificationCategory;
import com.theatermgnt.theatermgnt.notification.enums.NotificationStatus;
import com.theatermgnt.theatermgnt.notification.provider.NotificationChannelProvider;
import com.theatermgnt.theatermgnt.notification.repository.NotificationLogRepository;
import com.theatermgnt.theatermgnt.notification.repository.NotificationRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 * NotificationDispatcher - Routes and sends notifications through appropriate channels
 * This is the orchestration layer between NotificationService and channel providers
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class NotificationDispatcher {
    List<NotificationChannelProvider> channelProviders;
    NotificationPreferenceService preferenceService;
    NotificationTemplateService templateService;
    NotificationRepository notificationRepository;
    NotificationLogRepository logRepository;
    AccountRepository accountRepository;

    /**
     * Dispatch notification to specified channels asynchronously
     * This is the main orchestration method
     */
    @Async
    @Transactional
    public void dispatch(String notificationId, List<String> channels, Map<String, Object> templateVariables) {
        log.info("Dispatching notification {} to channels: {}", notificationId, channels);

        // Fetch notification in this thread's transaction context
        Notification notification = notificationRepository
                .findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));

        NotificationCategory category = templateVariables != null && templateVariables.containsKey("category")
                ? NotificationCategory.valueOf((String) templateVariables.get("category"))
                : NotificationCategory.SYSTEM;

        boolean anySuccess = false;

        for (String channelName : channels) {
            try {
                // 1. Check if channel is enabled for user
                boolean isEnabled =
                        preferenceService.isChannelEnabledForUser(notification.getRecipientId(), channelName, category);

                if (!isEnabled) {
                    log.info("Channel {} is disabled for recipient: {}", channelName, notification.getRecipientId());
                    saveLog(
                            notification,
                            channelName,
                            "SKIPPED",
                            Map.of("reason", "Channel disabled by user preference"),
                            null);
                    continue;
                }

                // 2. Find appropriate provider
                NotificationChannelProvider provider = findProvider(channelName);
                if (provider == null) {
                    log.warn("No provider found for channel: {}", channelName);
                    saveLog(notification, channelName, "FAILED", Map.of("reason", "Provider not found"), null);
                    continue;
                }

                if (!provider.isAvailable()) {
                    log.warn("Provider {} is not available", channelName);
                    saveLog(notification, channelName, "FAILED", Map.of("reason", "Provider not available"), null);
                    continue;
                }

                // 3. Build send request
                NotificationSendRequest sendRequest = buildSendRequest(notification, channelName, templateVariables);

                // 4. Send through provider
                log.info("Sending notification via provider: {}", channelName);
                NotificationSendResult result = provider.send(sendRequest);

                // 5. Save log
                saveLog(
                        notification,
                        channelName,
                        result.getStatus(),
                        result.getProviderResponse(),
                        result.getSentAt());

                if (result.isSuccess()) {
                    anySuccess = true;
                    log.info("Notification sent successfully via {}", channelName);
                } else {
                    log.error("Failed to send notification via {}: {}", channelName, result.getErrorMessage());
                }

            } catch (Exception e) {
                log.error("Error dispatching notification via {}: {}", channelName, e.getMessage(), e);
                saveLog(notification, channelName, "FAILED", Map.of("error", e.getMessage()), LocalDateTime.now());
            }
        }

        // 6. Update notification status
        notification.setStatus(anySuccess ? NotificationStatus.SENT : NotificationStatus.FAILED);
        notificationRepository.save(notification);

        log.info("Notification {} dispatch completed. Status: {}", notification.getId(), notification.getStatus());
    }

    /**
     * Find provider for a specific channel
     */
    private NotificationChannelProvider findProvider(String channelName) {
        return channelProviders.stream()
                .filter(provider -> provider.supports(channelName))
                .findFirst()
                .orElse(null);
    }

    /**
     * Build NotificationSendRequest from Notification entity and template variables
     */
    private NotificationSendRequest buildSendRequest(
            Notification notification, String channelName, Map<String, Object> templateVariables) {

        // Get recipient details from Account
        Account recipient =
                accountRepository.findById(notification.getRecipientId()).orElse(null);

        // Render title and content from template
        String title = notification.getNotificationTemplate() != null
                ? templateService.renderTitle(
                        notification.getNotificationTemplate().getTemplateCode(), templateVariables)
                : (String) templateVariables.getOrDefault("title", "Notification");

        String content = notification.getNotificationTemplate() != null
                ? templateService.renderTemplate(
                        notification.getNotificationTemplate().getTemplateCode(), templateVariables)
                : (String) templateVariables.getOrDefault("content", "");

        // Build metadata for channel-specific requirements
        Map<String, Object> metadata = new HashMap<>(notification.getMetadata());

        // Add attachments if present in template variables
        if (templateVariables.containsKey("attachments")) {
            metadata.put("attachments", templateVariables.get("attachments"));
        }

        return NotificationSendRequest.builder()
                .recipientId(notification.getRecipientId())
                .recipientEmail(recipient != null ? recipient.getEmail() : null)
                .recipientName(recipient != null ? recipient.getUsername() : null)
                .templateCode(
                        notification.getNotificationTemplate() != null
                                ? notification.getNotificationTemplate().getTemplateCode()
                                : null)
                .title(title)
                .content(content)
                .metadata(metadata)
                .build();
    }

    /**
     * Save notification log to database
     */
    private void saveLog(
            Notification notification,
            String channelName,
            String status,
            Map<String, Object> providerResponse,
            LocalDateTime sentAt) {
        NotificationLog notificationLog = NotificationLog.builder()
                .notification(notification)
                .channelName(channelName)
                .status(status)
                .providerResponse(providerResponse)
                .sentAt(sentAt != null ? sentAt : LocalDateTime.now())
                .build();

        logRepository.save(notificationLog);
        log.info("Notification log saved for channel: {}, status: {}", channelName, status);
    }
}
