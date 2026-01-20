package com.theatermgnt.theatermgnt.notification.entity;

import java.time.LocalDateTime;
import java.util.Map;

import jakarta.persistence.*;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.hibernate.type.SqlTypes;

import com.theatermgnt.theatermgnt.common.entity.BaseEntity;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "notification_logs")
@SQLDelete(sql = "UPDATE notification_logs SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationLog extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "notification_id")
    Notification notification;

    String channelName;
    String status;

    @JdbcTypeCode(SqlTypes.JSON)
    Map<String, Object> providerResponse;

    LocalDateTime sentAt;
}
