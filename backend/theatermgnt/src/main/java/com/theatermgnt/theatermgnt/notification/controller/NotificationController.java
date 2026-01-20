package com.theatermgnt.theatermgnt.notification.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.theatermgnt.theatermgnt.common.dto.response.ApiResponse;
import com.theatermgnt.theatermgnt.notification.dto.response.NotificationDetailResponse;
import com.theatermgnt.theatermgnt.notification.service.NotificationService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 * NotificationController - User-facing notification APIs
 * Handles notification retrieval, marking as read, and deletion
 */
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class NotificationController {
    NotificationService notificationService;

    /**
     * Get current user's notifications with pagination
     * GET /notifications?page=0&size=20
     */
    @GetMapping
    public ApiResponse<Page<NotificationDetailResponse>> getMyNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        log.info("Getting notifications for user: {}", authentication.getName());

        Pageable pageable = PageRequest.of(page, size);
        String userId = authentication.getName();

        return ApiResponse.<Page<NotificationDetailResponse>>builder()
                .result(notificationService.getUserNotifications(userId, pageable))
                .build();
    }

    /**
     * Get unread notification count for current user
     * GET /notifications/unread-count
     */
    @GetMapping("/unread-count")
    public ApiResponse<Long> getUnreadCount(Authentication authentication) {
        log.info("Getting unread count for user: {}", authentication.getName());

        String userId = authentication.getName();

        return ApiResponse.<Long>builder()
                .result(notificationService.getUnreadCount(userId))
                .build();
    }

    /**
     * Get notification detail with logs
     * GET /notifications/{id}
     */
    @GetMapping("/{id}")
    public ApiResponse<NotificationDetailResponse> getNotificationDetail(
            @PathVariable String id, Authentication authentication) {
        log.info("Getting notification detail: {} for user: {}", id, authentication.getName());

        return ApiResponse.<NotificationDetailResponse>builder()
                .result(notificationService.getNotificationDetail(id))
                .build();
    }

    /**
     * Mark a notification as read
     * PUT /notifications/{id}/read
     */
    @PutMapping("/{id}/read")
    public ApiResponse<NotificationDetailResponse> markAsRead(@PathVariable String id, Authentication authentication) {
        log.info("Marking notification as read: {} for user: {}", id, authentication.getName());

        return ApiResponse.<NotificationDetailResponse>builder()
                .result(notificationService.markAsRead(id))
                .build();
    }

    /**
     * Mark all notifications as read for current user
     * PUT /notifications/read-all
     */
    @PutMapping("/read-all")
    public ApiResponse<Void> markAllAsRead(Authentication authentication) {
        log.info("Marking all notifications as read for user: {}", authentication.getName());

        String userId = authentication.getName();
        notificationService.markAllAsRead(userId);

        return ApiResponse.<Void>builder()
                .message("All notifications marked as read")
                .build();
    }

    /**
     * Delete a notification (soft delete)
     * DELETE /notifications/{id}
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteNotification(@PathVariable String id, Authentication authentication) {
        log.info("Deleting notification: {} for user: {}", id, authentication.getName());

        notificationService.deleteNotification(id);

        return ApiResponse.<Void>builder()
                .message("Notification deleted successfully")
                .build();
    }
}
