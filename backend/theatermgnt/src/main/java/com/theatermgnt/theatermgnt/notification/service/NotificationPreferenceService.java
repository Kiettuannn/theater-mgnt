package com.theatermgnt.theatermgnt.notification.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.theatermgnt.theatermgnt.common.exception.AppException;
import com.theatermgnt.theatermgnt.common.exception.ErrorCode;
import com.theatermgnt.theatermgnt.notification.dto.request.NotificationPreferenceRequest;
import com.theatermgnt.theatermgnt.notification.dto.request.UpdatePreferenceRequest;
import com.theatermgnt.theatermgnt.notification.dto.response.NotificationPreferenceResponse;
import com.theatermgnt.theatermgnt.notification.entity.NotificationChannel;
import com.theatermgnt.theatermgnt.notification.entity.NotificationPreference;
import com.theatermgnt.theatermgnt.notification.enums.NotificationCategory;
import com.theatermgnt.theatermgnt.notification.enums.RecipientType;
import com.theatermgnt.theatermgnt.notification.mapper.NotificationPreferenceMapper;
import com.theatermgnt.theatermgnt.notification.repository.NotificationChannelRepository;
import com.theatermgnt.theatermgnt.notification.repository.NotificationPreferenceRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class NotificationPreferenceService {
    NotificationPreferenceRepository preferenceRepository;
    NotificationChannelRepository channelRepository;
    NotificationPreferenceMapper preferenceMapper;

    @Transactional(readOnly = true)
    public List<NotificationPreferenceResponse> getUserPreferences(String recipientId) {
        log.debug("Getting preferences for recipient: {}", recipientId);

        List<NotificationPreference> preferences = preferenceRepository.findByRecipientId(recipientId);

        return preferences.stream().map(preferenceMapper::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public NotificationPreferenceResponse createPreference(NotificationPreferenceRequest request) {
        log.info(
                "Creating preference for recipient: {}, channel: {}", request.getRecipientId(), request.getChannelId());

        NotificationChannel channel = channelRepository
                .findById(request.getChannelId())
                .orElseThrow(() -> new AppException(ErrorCode.CHANNEL_NOT_FOUND));

        // Check if preference already exists
        preferenceRepository
                .findByRecipientIdAndChannelAndCategory(request.getRecipientId(), channel, request.getCategory())
                .ifPresent(existing -> {
                    throw new AppException(ErrorCode.PREFERENCE_ALREADY_EXISTS);
                });

        NotificationPreference preference = preferenceMapper.toEntity(request, channel);
        NotificationPreference saved = preferenceRepository.save(preference);

        log.info("Preference created successfully");
        return preferenceMapper.toResponse(saved);
    }

    @Transactional
    public NotificationPreferenceResponse updatePreference(UpdatePreferenceRequest request) {
        log.info(
                "Updating preference for recipient: {}, channel: {}, category: {}",
                request.getRecipientId(),
                request.getChannelName(),
                request.getCategory());

        NotificationChannel channel = channelRepository
                .findByName(request.getChannelName())
                .orElseThrow(() -> new AppException(ErrorCode.CHANNEL_NOT_FOUND));

        NotificationPreference preference = preferenceRepository
                .findByRecipientIdAndChannelAndCategory(request.getRecipientId(), channel, request.getCategory())
                .orElseThrow(() -> new AppException(ErrorCode.PREFERENCE_NOT_FOUND));

        preference.setIsEnabled(request.getIsEnabled());
        NotificationPreference updated = preferenceRepository.save(preference);

        log.info("Preference updated successfully");
        return preferenceMapper.toResponse(updated);
    }

    /**
     * Check if a channel is enabled for a user and category
     * CRITICAL method used by NotificationDispatcher
     */
    @Transactional(readOnly = true)
    public boolean isChannelEnabledForUser(String recipientId, String channelName, NotificationCategory category) {
        log.debug(
                "Checking if channel {} is enabled for recipient: {}, category: {}",
                channelName,
                recipientId,
                category);

        NotificationChannel channel = channelRepository.findByName(channelName).orElse(null);

        if (channel == null || !channel.getIsActive()) {
            log.debug("Channel {} not found or inactive", channelName);
            return false;
        }

        return preferenceRepository
                .findByRecipientIdAndChannelAndCategory(recipientId, channel, category)
                .map(NotificationPreference::getIsEnabled)
                .orElse(true); // Default to enabled if no preference set
    }

    /**
     * Create default preferences for a new user
     * Called when a new user/account is created
     */
    @Transactional
    public void createDefaultPreferences(String recipientId, RecipientType recipientType) {
        log.info("Creating default preferences for recipient: {}, type: {}", recipientId, recipientType);

        // Get all active channels
        List<NotificationChannel> activeChannels = channelRepository.findByIsActive(true);

        if (activeChannels.isEmpty()) {
            log.warn("No active channels found. Skipping default preference creation.");
            return;
        }

        // Create default preferences
        List<NotificationPreference> defaultPreferences =
                createDefaultPreferences(recipientId, recipientType, activeChannels);

        preferenceRepository.saveAll(defaultPreferences);

        log.info("Created {} default preferences for recipient: {}", defaultPreferences.size(), recipientId);
    }

    @Transactional
    public void deletePreference(String id) {
        log.info("Deleting preference: {}", id);

        NotificationPreference preference =
                preferenceRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.PREFERENCE_NOT_FOUND));

        preferenceRepository.delete(preference);
        log.info("Preference deleted successfully");
    }

    /**
     * Create default preferences for a new user - Private helper method
     */
    private List<NotificationPreference> createDefaultPreferences(
            String recipientId, RecipientType recipientType, List<NotificationChannel> channels) {

        List<NotificationPreference> preferences = new ArrayList<>();

        for (NotificationChannel channel : channels) {
            for (NotificationCategory category : NotificationCategory.values()) {
                NotificationPreference preference = NotificationPreference.builder()
                        .recipientId(recipientId)
                        .recipientType(recipientType)
                        .channel(channel)
                        .category(category)
                        .isEnabled(true)
                        .build();
                preferences.add(preference);
            }
        }

        return preferences;
    }
}
