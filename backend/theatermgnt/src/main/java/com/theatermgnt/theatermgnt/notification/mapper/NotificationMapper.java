package com.theatermgnt.theatermgnt.notification.mapper;

import org.mapstruct.*;

import com.theatermgnt.theatermgnt.notification.dto.response.NotificationDetailResponse;
import com.theatermgnt.theatermgnt.notification.dto.response.NotificationLogResponse;
import com.theatermgnt.theatermgnt.notification.dto.response.NotificationResponse;
import com.theatermgnt.theatermgnt.notification.entity.Notification;
import com.theatermgnt.theatermgnt.notification.entity.NotificationLog;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(source = "notificationTemplate.templateCode", target = "templateCode")
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "title", ignore = true)
    @Mapping(target = "content", ignore = true)
    @Mapping(target = "isRead", ignore = true)
    NotificationResponse toResponse(Notification entity);

    @Mapping(source = "notificationTemplate.templateCode", target = "templateCode")
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "title", ignore = true)
    @Mapping(target = "content", ignore = true)
    @Mapping(target = "isRead", ignore = true)
    @Mapping(target = "logs", ignore = true)
    NotificationDetailResponse toDetailResponse(Notification entity);

    @Mapping(source = "notification.id", target = "notificationId")
    NotificationLogResponse toLogResponse(NotificationLog entity);
}
