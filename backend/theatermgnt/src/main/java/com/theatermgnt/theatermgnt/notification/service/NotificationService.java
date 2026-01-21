package com.theatermgnt.theatermgnt.notification.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.theatermgnt.theatermgnt.notification.dto.request.CreateNotificationRequest;
import com.theatermgnt.theatermgnt.notification.enums.Priority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.theatermgnt.theatermgnt.common.exception.AppException;
import com.theatermgnt.theatermgnt.common.exception.ErrorCode;
import com.theatermgnt.theatermgnt.notification.dto.response.NotificationDetailResponse;
import com.theatermgnt.theatermgnt.notification.dto.response.NotificationLogDetailResponse;
import com.theatermgnt.theatermgnt.notification.dto.response.NotificationLogResponse;
import com.theatermgnt.theatermgnt.notification.entity.Notification;
import com.theatermgnt.theatermgnt.notification.entity.NotificationLog;
import com.theatermgnt.theatermgnt.notification.entity.NotificationTemplate;
import com.theatermgnt.theatermgnt.notification.enums.NotificationCategory;
import com.theatermgnt.theatermgnt.notification.enums.NotificationStatus;
import com.theatermgnt.theatermgnt.notification.mapper.NotificationMapper;
import com.theatermgnt.theatermgnt.notification.repository.NotificationLogRepository;
import com.theatermgnt.theatermgnt.notification.repository.NotificationRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 * NotificationService - Core service for managing notifications
 * This is the main entry point for creating and managing notifications
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class NotificationService {
    NotificationRepository notificationRepository;
    NotificationLogRepository logRepository;
    NotificationTemplateService templateService;
    NotificationDispatcher dispatcher;
    NotificationMapper notificationMapper;
    SocketIOService socketIOService;

    /**
     * Create and send notifications to one or multiple recipients
     * Handles both single and batch sends with the same logic
     */
    public List<NotificationDetailResponse> createAndSend(CreateNotificationRequest request) {
        log.info(
                "Creating and sending notification to {} recipient(s), template: {}, channels: {}",
                request.getRecipientIds().size(),
                request.getTemplateCode(),
                request.getChannels());

        // 1. Get template
        NotificationTemplate template = templateService.getTemplateByCode(request.getTemplateCode());


        // 3. Build base metadata
        Map<String, Object> baseMetadata = new HashMap<>();
        if (request.getMetadata() != null) {
            baseMetadata.putAll(request.getMetadata());
        }
        baseMetadata.put("category", request.getCategory().name());

        // 4. Render template once (same content for all recipients)
        String title = templateService.renderTitle(request.getTemplateCode(), request.getMetadata());
        String content = templateService.renderTemplate(request.getTemplateCode(), request.getMetadata());
        baseMetadata.put("title", title);
        baseMetadata.put("content", content);

        // 5. Create notifications for all recipients
        List<Notification> notifications = new ArrayList<>();
        for (String recipientId : request.getRecipientIds()) {
            Notification notification = Notification.builder()
                    .notificationTemplate(template)
                    .recipientId(recipientId)
                    .recipientType(request.getRecipientType())
                    .priority(request.getPriority())
                    .status(NotificationStatus.PENDING)
                    .metadata(new HashMap<>(baseMetadata)) // Clone metadata for each recipient
                    .build();
            notifications.add(notification);
        }

        // 6. Batch save all notifications
        List<Notification> savedNotifications = notificationRepository.saveAll(notifications);
        log.info("Created {} notification(s)", savedNotifications.size());

        // 7. Dispatch to channels and emit Socket.IO for each notification
        for (Notification saved : savedNotifications) {
            // Dispatch asynchronously
            dispatcher.dispatch(saved.getId(), request.getChannels(), saved.getMetadata());
            
            // Emit to Socket.IO for IN_APP channel
            if (request.getChannels().contains("IN_APP")) {
                NotificationDetailResponse response = notificationMapper.toDetailResponse(saved);
                if (saved.getMetadata() != null) {
                    if (saved.getMetadata().containsKey("category")) {
                        response.setCategory(NotificationCategory.valueOf(
                                (String) saved.getMetadata().get("category")));
                    }
                    if (saved.getMetadata().containsKey("title")) {
                        response.setTitle((String) saved.getMetadata().get("title"));
                    }
                    if (saved.getMetadata().containsKey("content")) {
                        response.setContent((String) saved.getMetadata().get("content"));
                    }
                }
                response.setIsRead(false);
                socketIOService.emitNotificationToUser(saved.getRecipientId(), response);
            }
        }

        // 8. Return all created notifications
        return savedNotifications.stream()
                .map(notificationMapper::toDetailResponse)
                .collect(Collectors.toList());
    }

    /**
     * Save notification in a separate transaction to ensure it's committed before async dispatch
     */
    @Transactional
    public Notification saveNotification(Notification notification) {
        return notificationRepository.save(notification);
    }

    /**
     * Get user's notifications with pagination
     */
    @Transactional(readOnly = true)
    public Page<NotificationDetailResponse> getUserNotifications(String userId, Pageable pageable) {
        log.debug("Getting notifications for user: {}", userId);

        Page<Notification> notifications =
                notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId, pageable);

        return notifications.map(notification -> toNotificationDetailResponse(notification, new ArrayList<>()));
    }

    /**
     * Get unread notification count for a user
     */
    @Transactional(readOnly = true)
    public Long getUnreadCount(String userId) {
        return notificationRepository.countByRecipientIdAndReadAtIsNull(userId);
    }

    /**
     * Mark a notification as read
     */
    @Transactional
    public NotificationDetailResponse markAsRead(String notificationId) {
        log.info("Marking notification as read: {}", notificationId);

        Notification notification = notificationRepository
                .findById(notificationId)
                .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_FOUND));

        if (notification.getReadAt() == null) {
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
            log.info("Notification marked as read: {}", notificationId);
        }

        List<NotificationLog> logs = logRepository.findByNotificationOrderBySentAtDesc(notification);
        return toNotificationDetailResponse(notification, logs);
    }

    /**
     * Mark all notifications as read for a user
     */
    @Transactional
    public void markAllAsRead(String userId) {
        log.info("Marking all notifications as read for user: {}", userId);

        List<Notification> unreadNotifications = notificationRepository.findByRecipientIdAndReadAtIsNull(userId);

        LocalDateTime now = LocalDateTime.now();
        unreadNotifications.forEach(notification -> notification.setReadAt(now));

        notificationRepository.saveAll(unreadNotifications);
        log.info("Marked {} notifications as read for user: {}", unreadNotifications.size(), userId);
    }

    /**
     * Get in-app notifications for a user (notifications with IN_APP channel)
     */
    @Transactional(readOnly = true)
    public List<NotificationDetailResponse> getInAppNotifications(String userId) {
        log.debug("Getting in-app notifications for user: {}", userId);

        // Get all notifications for user, then filter by IN_APP channel in logs
        List<Notification> allNotifications =
                notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId);

        return allNotifications.stream()
                .filter(notification -> {
                    // Check if any log has IN_APP channel
                    List<NotificationLog> logs = logRepository.findByNotificationOrderBySentAtDesc(notification);
                    return logs.stream()
                            .anyMatch(log ->
                                    log.getChannelName() != null && "IN_APP".equals(log.getChannelName()));
                })
                .map(notification -> {
                    List<NotificationLog> logs = logRepository.findByNotificationOrderBySentAtDesc(notification);
                    return toNotificationDetailResponse(notification, logs);
                })
                .collect(Collectors.toList());
    }

    /**
     * Get notification detail with logs
     */
    @Transactional(readOnly = true)
    public NotificationDetailResponse getNotificationDetail(String notificationId) {
        log.debug("Getting notification detail: {}", notificationId);

        Notification notification = notificationRepository
                .findById(notificationId)
                .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_FOUND));

        List<NotificationLog> logs = logRepository.findByNotificationOrderBySentAtDesc(notification);

        return toNotificationDetailResponse(notification, logs);
    }

    /**
     * Get all notifications (Admin API)
     */
    @Transactional(readOnly = true)
    public Page<NotificationDetailResponse> getAllNotifications(Pageable pageable) {
        Page<Notification> notifications = notificationRepository.findAll(pageable);
        return notifications.map(notification -> toNotificationDetailResponse(notification, new ArrayList<>()));
    }

    /**
     * Delete notification (soft delete)
     */
    @Transactional
    public void deleteNotification(String notificationId) {
        log.info("Deleting notification: {}", notificationId);

        Notification notification = notificationRepository
                .findById(notificationId)
                .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_FOUND));

        notificationRepository.delete(notification);
        log.info("Notification deleted: {}", notificationId);
    }

    /**
     * Get all notification logs (Admin API)
     * Returns all logs with notification details
     */
    @Transactional(readOnly = true)
    public List<NotificationLogDetailResponse> getAllNotificationLogs() {
        log.info("Getting all notification logs");

        List<NotificationLog> logs = logRepository.findAllWithNotificationOrderBySentAtDesc();

        return logs.stream().map(this::toLogDetailResponse).collect(Collectors.toList());
    }

    /**
     * Get notification log detail by ID (Admin API)
     */
    @Transactional(readOnly = true)
    public NotificationLogDetailResponse getNotificationLogDetail(String logId) {
        log.info("Getting notification log detail: {}", logId);

        NotificationLog notificationLog =
                logRepository.findById(logId).orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_FOUND));

        return toLogDetailResponse(notificationLog);
    }

    /**
     * Delete notification log (Admin API)
     */
    @Transactional
    public void deleteNotificationLog(String logId) {
        log.info("Deleting notification log: {}", logId);

        NotificationLog notificationLog =
                logRepository.findById(logId).orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_FOUND));

        logRepository.delete(notificationLog);
        log.info("Notification log deleted: {}", logId);
    }

    /**
     * Private helper method to convert entity to response DTO
     */
    private NotificationDetailResponse toNotificationDetailResponse(
            Notification notification, List<NotificationLog> logs) {
        NotificationDetailResponse response = notificationMapper.toDetailResponse(notification);

        // Extract category, title, content from metadata
        if (notification.getMetadata() != null) {
            if (notification.getMetadata().containsKey("category")) {
                response.setCategory(NotificationCategory.valueOf(
                        (String) notification.getMetadata().get("category")));
            }
            if (notification.getMetadata().containsKey("title")) {
                response.setTitle((String) notification.getMetadata().get("title"));
            }
            if (notification.getMetadata().containsKey("content")) {
                response.setContent((String) notification.getMetadata().get("content"));
            }
        }

        // Set isRead based on readAt
        response.setIsRead(notification.getReadAt() != null);

        // Map logs
        List<NotificationLogResponse> logResponses =
                logs.stream().map(notificationMapper::toLogResponse).collect(Collectors.toList());
        response.setLogs(logResponses);

        return response;
    }

    /**
     * Convert NotificationLog to NotificationLogDetailResponse with notification title
     */
    private NotificationLogDetailResponse toLogDetailResponse(NotificationLog log) {
        String notificationTitle = "";
        if (log.getNotification() != null && log.getNotification().getMetadata() != null) {
            notificationTitle = (String) log.getNotification().getMetadata().getOrDefault("title", "");
        }

        return NotificationLogDetailResponse.builder()
                .id(log.getId())
                .notificationId(
                        log.getNotification() != null ? log.getNotification().getId() : null)
                .notificationTitle(notificationTitle)
                .channelName(log.getChannelName())
                .status(log.getStatus())
                .providerResponse(log.getProviderResponse())
                .sentAt(log.getSentAt())
                .build();
    }
}
