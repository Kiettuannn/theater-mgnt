package com.theatermgnt.theatermgnt.notification.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.theatermgnt.theatermgnt.common.dto.response.ApiResponse;
import com.theatermgnt.theatermgnt.notification.dto.request.UpdatePreferenceRequest;
import com.theatermgnt.theatermgnt.notification.dto.response.NotificationPreferenceResponse;
import com.theatermgnt.theatermgnt.notification.enums.RecipientType;
import com.theatermgnt.theatermgnt.notification.service.NotificationPreferenceService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 * NotificationPreferenceController - User APIs for managing notification preferences
 * Allows users to customize their notification settings
 */
@RestController
@RequestMapping("/notification-preferences")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class NotificationPreferenceController {
    NotificationPreferenceService preferenceService;

    /**
     * Get current user's notification preferences
     * GET /notification-preferences
     */
    @GetMapping
    public ApiResponse<List<NotificationPreferenceResponse>> getMyPreferences(Authentication authentication) {
        log.info("Getting notification preferences for user: {}", authentication.getName());

        String userId = authentication.getName();

        return ApiResponse.<List<NotificationPreferenceResponse>>builder()
                .result(preferenceService.getUserPreferences(userId))
                .build();
    }

    /**
     * Update notification preference
     * PUT /notification-preferences
     */
    @PutMapping
    public ApiResponse<NotificationPreferenceResponse> updatePreference(
            @RequestBody @Valid UpdatePreferenceRequest request, Authentication authentication) {
        log.info(
                "Updating notification preference for user: {}, channel: {}, category: {}",
                authentication.getName(),
                request.getChannelName(),
                request.getCategory());

        return ApiResponse.<NotificationPreferenceResponse>builder()
                .result(preferenceService.updatePreference(request))
                .build();
    }

    /**
     * Reset all preferences to default
     * POST /notification-preferences/reset
     */
    @PostMapping("/reset")
    public ApiResponse<Void> resetToDefaults(Authentication authentication) {
        log.info("Resetting notification preferences to default for user: {}", authentication.getName());

        String userId = authentication.getName();
        // Service method is createDefaultPreferences which needs recipientType
        // Assuming user is always CUSTOMER for this endpoint, or you need to determine type
        preferenceService.createDefaultPreferences(userId, RecipientType.CUSTOMER);

        return ApiResponse.<Void>builder()
                .message("Preferences reset to defaults")
                .build();
    }
}
