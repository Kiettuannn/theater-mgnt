package com.theatermgnt.theatermgnt.notification.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.theatermgnt.theatermgnt.common.dto.response.ApiResponse;
import com.theatermgnt.theatermgnt.notification.dto.request.NotificationTemplateRequest;
import com.theatermgnt.theatermgnt.notification.dto.response.NotificationTemplateResponse;
import com.theatermgnt.theatermgnt.notification.service.NotificationTemplateService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 * NotificationTemplateController - Admin APIs for managing notification templates
 * Requires ADMIN role for all operations
 */
@RestController
@RequestMapping("/admin/notification-templates")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class NotificationTemplateController {
    NotificationTemplateService templateService;

    /**
     * Create a new notification template
     * POST /admin/notification-templates
     */
    @PostMapping
    public ApiResponse<NotificationTemplateResponse> createTemplate(
            @RequestBody @Valid NotificationTemplateRequest request) {
        log.info("Creating notification template: {}", request.getTemplateCode());

        return ApiResponse.<NotificationTemplateResponse>builder()
                .result(templateService.createTemplate(request))
                .build();
    }

    /**
     * Get all notification templates
     * GET /admin/notification-templates
     */
    @GetMapping
    public ApiResponse<List<NotificationTemplateResponse>> getAllTemplates() {
        log.info("Getting all notification templates");

        return ApiResponse.<List<NotificationTemplateResponse>>builder()
                .result(templateService.getAllTemplates())
                .build();
    }

    /**
     * Get notification template by ID
     * GET /admin/notification-templates/{id}
     */
    @GetMapping("/{id}")
    public ApiResponse<NotificationTemplateResponse> getTemplateById(@PathVariable String id) {
        log.info("Getting notification template by ID: {}", id);

        return ApiResponse.<NotificationTemplateResponse>builder()
                .result(templateService.getTemplateById(id))
                .build();
    }

    /**
     * Get notification template by code
     * GET /admin/notification-templates/code/{code}
     */
    @GetMapping("/code/{code}")
    public ApiResponse<NotificationTemplateResponse> getTemplateByCode(@PathVariable String code) {
        log.info("Getting notification template by code: {}", code);

        // getTemplateByCode returns entity, need to convert to response
        return ApiResponse.<NotificationTemplateResponse>builder()
                .result(templateService.getTemplateById(
                        templateService.getTemplateByCode(code).getId()))
                .build();
    }

    /**
     * Update notification template
     * PUT /admin/notification-templates/{id}
     */
    @PutMapping("/{id}")
    public ApiResponse<NotificationTemplateResponse> updateTemplate(
            @PathVariable String id, @RequestBody @Valid NotificationTemplateRequest request) {
        log.info("Updating notification template: {}", id);

        return ApiResponse.<NotificationTemplateResponse>builder()
                .result(templateService.updateTemplate(id, request))
                .build();
    }

    /**
     * Delete notification template
     * DELETE /admin/notification-templates/{id}
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteTemplate(@PathVariable String id) {
        log.info("Deleting notification template: {}", id);

        templateService.deleteTemplate(id);

        return ApiResponse.<Void>builder()
                .message("Template deleted successfully")
                .build();
    }
}
