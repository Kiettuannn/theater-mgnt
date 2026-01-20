package com.theatermgnt.theatermgnt.notification.entity;

import java.util.Map;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

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
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "notification_channels")
@SQLDelete(sql = "UPDATE notification_channels SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationChannel extends BaseEntity {
    String name;
    Boolean isActive;

    @JdbcTypeCode(SqlTypes.JSON)
    Map<String, Object> configJson;
}
