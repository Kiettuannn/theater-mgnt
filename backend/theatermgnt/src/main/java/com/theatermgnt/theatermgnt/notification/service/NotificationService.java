package com.theatermgnt.theatermgnt.notification.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.theatermgnt.theatermgnt.notification.enums.Priority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.theatermgnt.theatermgnt.common.exception.AppException;
import com.theatermgnt.theatermgnt.common.exception.ErrorCode;
import com.theatermgnt.theatermgnt.notification.dto.request.CreateNotificationRequest;
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
     * Create notification and dispatch to channels
     * This is the main method called by event listeners
     */
    public NotificationDetailResponse createAndSend(CreateNotificationRequest request) {
        log.info(
                "Creating and sending notification for recipient: {}, template: {}, channels: {}",
                request.getRecipientId(),
                request.getTemplateCode(),
                request.getChannels());

        // 1. Get template
        NotificationTemplate template = templateService.getTemplateByCode(request.getTemplateCode());

        // 2. Build metadata with template variables and category
        Map<String, Object> metadata = new HashMap<>();
        if (request.getMetadata() != null) {
            metadata.putAll(request.getMetadata());
        }
        metadata.put("category", request.getCategory().name());

        // Render and store title/content in metadata for quick access
        String title = templateService.renderTitle(request.getTemplateCode(), request.getMetadata());
        String content = templateService.renderTemplate(request.getTemplateCode(), request.getMetadata());
        metadata.put("title", title);
        metadata.put("content", content);

        // 3. Create notification entity using builder
        Notification notification = Notification.builder()
                .notificationTemplate(template)
                .recipientId(request.getRecipientId())
                .recipientType(request.getRecipientType())
                .priority(request.getPriority() != null ? request.getPriority() : Priority.NORMAL)
                .status(NotificationStatus.PENDING)
                .metadata(metadata)
                .build();

        // 4. Save notification to DB in a separate transaction (commits immediately)
        Notification saved = saveNotification(notification);
        log.info("Notification created with ID: {}", saved.getId());

        // 5. Dispatch to channels asynchronously (transaction already committed)
        log.info("About to dispatch notification {} to channels: {}", saved.getId(), request.getChannels());
        dispatcher.dispatch(saved.getId(), request.getChannels(), metadata);
        log.info("Dispatcher.dispatch() called successfully for notification: {}", saved.getId());

        // 6. Emit to Socket.IO for IN_APP channel
        if (request.getChannels().contains("IN_APP")) {
            log.info("Emitting notification {} to Socket.IO for IN_APP channel", saved.getId());
            
            // Create response with title/content from metadata
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
            response.setIsRead(false); // New notification is always unread
            
            // Only emit to targeted user OR broadcast, not both
            log.info("Socket.IO emit check - recipientId: {}", request.getRecipientId());
            if (request.getRecipientId() != null && !request.getRecipientId().isEmpty()) {
                log.info("Emitting to specific user: {}", request.getRecipientId());
                socketIOService.emitNotificationToUser(request.getRecipientId(), response);
            } else {
                log.info("Broadcasting to all users");
                socketIOService.broadcastNotification(response);
            }
        }

        return notificationMapper.toDetailResponse(saved);
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
