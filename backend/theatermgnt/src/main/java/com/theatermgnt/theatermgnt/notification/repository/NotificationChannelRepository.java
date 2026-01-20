package com.theatermgnt.theatermgnt.notification.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.theatermgnt.theatermgnt.notification.entity.NotificationChannel;

public interface NotificationChannelRepository extends JpaRepository<NotificationChannel, String> {
    Optional<NotificationChannel> findByName(String name);

    List<NotificationChannel> findByIsActive(Boolean isActive);
}
