package com.theatermgnt.theatermgnt.notification.provider;

import com.theatermgnt.theatermgnt.notification.dto.request.NotificationSendRequest;
import com.theatermgnt.theatermgnt.notification.dto.response.NotificationSendResult;

/**
 * Interface for all notification channel providers
 * Each provider implements a specific channel (EMAIL, SMS, PUSH, IN_APP)
 */
public interface NotificationChannelProvider {
    /**
     * Get the channel name this provider handles
     * @return Channel name (e.g., "EMAIL", "SMS", "PUSH", "IN_APP")
     */
    String getChannelName();

    /**
     * Send notification through this channel
     * @param request The notification send request
     * @return Result of the send operation
     */
    NotificationSendResult send(NotificationSendRequest request);

    /**
     * Check if this provider supports the given channel name
     * @param channelName The channel name to check
     * @return true if this provider supports the channel
     */
    default boolean supports(String channelName) {
        return getChannelName().equalsIgnoreCase(channelName);
    }

    /**
     * Check if this provider is currently available
     * @return true if the provider is ready to send notifications
     */
    default boolean isAvailable() {
        return true;
    }
}
