package com.theatermgnt.theatermgnt.notification.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.theatermgnt.theatermgnt.notification.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, String> {
    Page<Notification> findByRecipientIdOrderByCreatedAtDesc(String recipientId, Pageable pageable);

    List<Notification> findByRecipientIdOrderByCreatedAtDesc(String recipientId);

    Long countByRecipientIdAndReadAtIsNull(String recipientId);

    List<Notification> findByRecipientIdAndReadAtIsNull(String recipientId);
}
