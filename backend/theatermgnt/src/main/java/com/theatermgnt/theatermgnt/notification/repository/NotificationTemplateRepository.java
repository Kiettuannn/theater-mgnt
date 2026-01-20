package com.theatermgnt.theatermgnt.notification.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.theatermgnt.theatermgnt.notification.entity.NotificationTemplate;

public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, String> {
    Optional<NotificationTemplate> findByTemplateCode(String templateCode);
}
