package com.theatermgnt.theatermgnt.notification.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.theatermgnt.theatermgnt.notification.entity.Notification;
import com.theatermgnt.theatermgnt.notification.entity.NotificationLog;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, String> {
    List<NotificationLog> findByNotificationOrderBySentAtDesc(Notification notification);

    // Find all logs ordered by sent date descending
    Page<NotificationLog> findAllByOrderBySentAtDesc(Pageable pageable);

    // Find all logs with notification info using JOIN
    @Query("SELECT nl FROM NotificationLog nl JOIN FETCH nl.notification ORDER BY nl.sentAt DESC")
    List<NotificationLog> findAllWithNotificationOrderBySentAtDesc();
}
