package com.theatermgnt.theatermgnt.notification.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.theatermgnt.theatermgnt.common.dto.response.ApiResponse;
import com.theatermgnt.theatermgnt.notification.dto.request.CreateNotificationRequest;
import com.theatermgnt.theatermgnt.notification.dto.response.NotificationDetailResponse;
import com.theatermgnt.theatermgnt.notification.dto.response.NotificationLogDetailResponse;
import com.theatermgnt.theatermgnt.notification.service.NotificationService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 * AdminNotificationController - Admin APIs for notification management
 * Allows admins to view all notifications and manually send notifications
 */
@RestController
@RequestMapping("/admin/notifications")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminNotificationController {
    NotificationService notificationService;

    /**
     * Get all notifications (Admin view)
     * GET /admin/notifications?page=0&size=20
     */
    @GetMapping
    public ApiResponse<Page<NotificationDetailResponse>> getAllNotifications(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "50") int size) {
        log.info("Admin getting all notifications - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size);

        return ApiResponse.<Page<NotificationDetailResponse>>builder()
                .result(notificationService.getAllNotifications(pageable))
                .build();
    }

    /**
     * Manually send a notification
     * POST /admin/notifications/send
     */
    @PostMapping("/send")
    public ApiResponse<NotificationDetailResponse> sendNotification(
            @RequestBody @Valid CreateNotificationRequest request) {
        log.info("Admin manually sending notification to recipient: {}", request.getRecipientId());

        return ApiResponse.<NotificationDetailResponse>builder()
                .result(notificationService.createAndSend(request))
                .build();
    }

    /**
     * Get all notification logs (Admin only)
     * GET /admin/notifications/logs
     * NOTE: This must be defined BEFORE /{id} to avoid path conflicts
     */
    @GetMapping("/logs")
    public ApiResponse<List<NotificationLogDetailResponse>> getAllNotificationLogs() {
        log.info("Admin getting all notification logs");

        return ApiResponse.<List<NotificationLogDetailResponse>>builder()
                .result(notificationService.getAllNotificationLogs())
                .build();
    }

    /**
     * Get notification log detail by ID (Admin only)
     * GET /admin/notifications/logs/{id}
     */
    @GetMapping("/logs/{id}")
    public ApiResponse<NotificationLogDetailResponse> getNotificationLogDetail(@PathVariable String id) {
        log.info("Admin getting notification log detail: {}", id);

        return ApiResponse.<NotificationLogDetailResponse>builder()
                .result(notificationService.getNotificationLogDetail(id))
                .build();
    }

    /**
     * Delete notification log (Admin only)
     * DELETE /admin/notifications/logs/{id}
     */
    @DeleteMapping("/logs/{id}")
    public ApiResponse<Void> deleteNotificationLog(@PathVariable String id) {
        log.info("Admin deleting notification log: {}", id);

        notificationService.deleteNotificationLog(id);

        return ApiResponse.<Void>builder()
                .message("Notification log deleted successfully")
                .build();
    }

    /**
     * Get notification detail by ID (Admin access)
     * GET /admin/notifications/{id}
     * NOTE: This is defined AFTER /logs endpoints to avoid path conflicts
     */
    @GetMapping("/{id}")
    public ApiResponse<NotificationDetailResponse> getNotificationDetail(@PathVariable String id) {
        log.info("Admin getting notification detail: {}", id);

        return ApiResponse.<NotificationDetailResponse>builder()
                .result(notificationService.getNotificationDetail(id))
                .build();
    }

    /**
     * Delete any notification (Admin only)
     * DELETE /admin/notifications/{id}
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteNotification(@PathVariable String id) {
        log.info("Admin deleting notification: {}", id);

        notificationService.deleteNotification(id);

        return ApiResponse.<Void>builder()
                .message("Notification deleted successfully")
                .build();
    }

    /**
     * Get in-app notifications for current user
     * GET /admin/notifications/in-app
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/in-app")
    public ApiResponse<List<NotificationDetailResponse>> getInAppNotifications(Authentication authentication) {
        log.info("Getting in-app notifications for admin: {}", authentication.getName());

        String userId = authentication.getName();
        
        return ApiResponse.<List<NotificationDetailResponse>>builder()
                .result(notificationService.getInAppNotifications(userId))
                .build();
    }

    /**
     * Mark notification as read
     * PATCH /admin/notifications/{id}/read
     */
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{id}/read")
    public ApiResponse<Void> markNotificationAsRead(@PathVariable String id, Authentication authentication) {
        log.info("Admin marking notification as read: {} for user: {}", id, authentication.getName());

        notificationService.markAsRead(id);

        return ApiResponse.<Void>builder()
                .message("Notification marked as read")
                .build();
    }

    /**
     * Mark all notifications as read for current user
     * PATCH /admin/notifications/read-all
     */
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/read-all")
    public ApiResponse<Void> markAllNotificationsAsRead(Authentication authentication) {
        log.info("Admin marking all notifications as read for user: {}", authentication.getName());

        String userId = authentication.getName();
        notificationService.markAllAsRead(userId);

        return ApiResponse.<Void>builder()
                .message("All notifications marked as read")
                .build();
    }
}
