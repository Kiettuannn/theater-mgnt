package com.theatermgnt.theatermgnt.notification.entity;

import java.time.LocalDateTime;
import java.util.Map;

import jakarta.persistence.*;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.hibernate.type.SqlTypes;

import com.theatermgnt.theatermgnt.common.entity.BaseEntity;
import com.theatermgnt.theatermgnt.notification.enums.NotificationStatus;
import com.theatermgnt.theatermgnt.notification.enums.Priority;
import com.theatermgnt.theatermgnt.notification.enums.RecipientType;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "notifications")
@SQLDelete(sql = "UPDATE notifications SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Notification extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "template_id")
    NotificationTemplate notificationTemplate;

    String recipientId;

    @Enumerated(EnumType.STRING)
    RecipientType recipientType;

    @JdbcTypeCode(SqlTypes.JSON)
    Map<String, Object> metadata;

    @Enumerated(EnumType.STRING)
    Priority priority;

    @Enumerated(EnumType.STRING)
    NotificationStatus status;

    LocalDateTime readAt;
}
