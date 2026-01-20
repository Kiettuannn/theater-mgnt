package com.theatermgnt.theatermgnt.notification.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.theatermgnt.theatermgnt.notification.entity.NotificationChannel;
import com.theatermgnt.theatermgnt.notification.entity.NotificationPreference;
import com.theatermgnt.theatermgnt.notification.enums.NotificationCategory;

public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, String> {
    List<NotificationPreference> findByRecipientId(String recipientId);

    Optional<NotificationPreference> findByRecipientIdAndChannelAndCategory(
            String recipientId, NotificationChannel channel, NotificationCategory category);
}
