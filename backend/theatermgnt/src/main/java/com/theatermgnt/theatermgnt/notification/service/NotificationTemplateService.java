package com.theatermgnt.theatermgnt.notification.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.theatermgnt.theatermgnt.common.exception.AppException;
import com.theatermgnt.theatermgnt.common.exception.ErrorCode;
import com.theatermgnt.theatermgnt.notification.dto.request.NotificationTemplateRequest;
import com.theatermgnt.theatermgnt.notification.dto.response.NotificationTemplateResponse;
import com.theatermgnt.theatermgnt.notification.entity.NotificationTemplate;
import com.theatermgnt.theatermgnt.notification.mapper.NotificationTemplateMapper;
import com.theatermgnt.theatermgnt.notification.repository.NotificationTemplateRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class NotificationTemplateService {
    NotificationTemplateRepository templateRepository;
    NotificationTemplateMapper templateMapper;
    EmailTemplateFactory emailTemplateFactory;

    /**
     * Sanitize HTML content to prevent XSS attacks
     * Allows common HTML tags used in email templates while removing dangerous elements
     */
    private String sanitizeHtml(String html) {
        if (html == null || html.trim().isEmpty()) {
            return html;
        }

        log.debug("Sanitizing HTML content, length: {}", html.length());

        // Create a relaxed safelist with additional tags for email templates
        Safelist safelist = Safelist.relaxed()
                // Allow style tags and attributes for formatting
                .addTags("style", "span", "div", "hr", "header", "footer", "section")
                // Allow common attributes
                .addAttributes(":all", "style", "class", "id")
                // Allow link attributes
                .addAttributes("a", "href", "target", "rel")
                // Allow image attributes
                .addAttributes("img", "src", "alt", "width", "height")
                // Allow table attributes
                .addAttributes("table", "border", "cellpadding", "cellspacing", "width")
                .addAttributes("td", "colspan", "rowspan", "align", "valign")
                .addAttributes("th", "colspan", "rowspan", "align", "valign")
                // Allow only safe protocols
                .addProtocols("a", "href", "http", "https", "mailto")
                .addProtocols("img", "src", "http", "https", "data");

        String sanitized = Jsoup.clean(html, safelist);
        log.debug("HTML sanitized, output length: {}", sanitized.length());

        return sanitized;
    }

    /**
     * Validate HTML content size
     */
    private void validateContentSize(String content) {
        if (content != null && content.length() > 100000) { // 100KB limit
            log.warn("Template content exceeds size limit: {} bytes", content.length());
            throw new AppException(ErrorCode.INVALID_KEY);
        }
    }

    @Transactional
    public NotificationTemplateResponse createTemplate(NotificationTemplateRequest request) {
        log.info("Creating notification template with code: {}", request.getTemplateCode());

        // Check if template code already exists
        if (templateRepository.findByTemplateCode(request.getTemplateCode()).isPresent()) {
            throw new AppException(ErrorCode.TEMPLATE_ALREADY_EXISTS);
        }

        // Validate content size
        validateContentSize(request.getContentTemplate());

        NotificationTemplate template = templateMapper.toEntity(request);

        // Sanitize HTML content before saving
        String sanitizedContent = sanitizeHtml(request.getContentTemplate());
        template.setContentTemplate(sanitizedContent);

        NotificationTemplate saved = templateRepository.save(template);

        log.info("Template created successfully: {}", saved.getId());
        return templateMapper.toResponse(saved);
    }

    @Transactional
    public NotificationTemplateResponse updateTemplate(String id, NotificationTemplateRequest request) {
        log.info("Updating notification template: {}", id);

        NotificationTemplate template =
                templateRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.TEMPLATE_NOT_FOUND));

        // Check if new template code conflicts with another template
        templateRepository.findByTemplateCode(request.getTemplateCode()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new AppException(ErrorCode.TEMPLATE_ALREADY_EXISTS);
            }
        });

        // Validate content size
        validateContentSize(request.getContentTemplate());

        templateMapper.updateEntity(template, request);

        // Sanitize HTML content before saving
        String sanitizedContent = sanitizeHtml(request.getContentTemplate());
        template.setContentTemplate(sanitizedContent);

        NotificationTemplate updated = templateRepository.save(template);

        log.info("Template updated successfully: {}", id);
        return templateMapper.toResponse(updated);
    }

    @Transactional(readOnly = true)
    public NotificationTemplateResponse getTemplateById(String id) {
        NotificationTemplate template =
                templateRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.TEMPLATE_NOT_FOUND));
        return templateMapper.toResponse(template);
    }

    @Transactional(readOnly = true)
    public NotificationTemplate getTemplateByCode(String templateCode) {
        return templateRepository
                .findByTemplateCode(templateCode)
                .orElseThrow(() -> new AppException(ErrorCode.TEMPLATE_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<NotificationTemplateResponse> getAllTemplates() {
        return templateRepository.findAll().stream()
                .map(templateMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteTemplate(String id) {
        log.info("Deleting notification template: {}", id);

        NotificationTemplate template =
                templateRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.TEMPLATE_NOT_FOUND));

        templateRepository.delete(template);
        log.info("Template deleted successfully: {}", id);
    }

    /**
     * Render template with variables
     * Uses existing EmailTemplateFactory for HTML rendering
     */
    public String renderTemplate(String templateCode, Map<String, Object> variables) {
        log.debug("Rendering template: {} with variables", templateCode);

        NotificationTemplate template = getTemplateByCode(templateCode);

        // Use EmailTemplateFactory to render content
        // Note: EmailTemplateFactory uses EmailType enum, so we need to map templateCode to EmailType
        // For now, we'll use the contentTemplate directly with simple variable replacement
        String content = template.getContentTemplate();

        // Simple variable replacement for now
        // TODO: Enhance with more sophisticated template engine if needed
        if (variables != null) {
            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                String placeholder = "{{" + entry.getKey() + "}}";
                content = content.replace(placeholder, String.valueOf(entry.getValue()));
            }
        }

        return content;
    }

    /**
     * Render title template with variables
     */
    public String renderTitle(String templateCode, Map<String, Object> variables) {
        NotificationTemplate template = getTemplateByCode(templateCode);
        String title = template.getTitleTemplate();

        if (variables != null) {
            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                String placeholder = "{{" + entry.getKey() + "}}";
                title = title.replace(placeholder, String.valueOf(entry.getValue()));
            }
        }

        return title;
    }
}
