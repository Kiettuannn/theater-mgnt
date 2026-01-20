package com.theatermgnt.theatermgnt.notification.mapper;

import org.mapstruct.*;

import com.theatermgnt.theatermgnt.notification.dto.request.NotificationPreferenceRequest;
import com.theatermgnt.theatermgnt.notification.dto.response.NotificationPreferenceResponse;
import com.theatermgnt.theatermgnt.notification.entity.NotificationChannel;
import com.theatermgnt.theatermgnt.notification.entity.NotificationPreference;

@Mapper(componentModel = "spring")
public interface NotificationPreferenceMapper {

    NotificationPreference toEntity(NotificationPreferenceRequest request, @Context NotificationChannel channel);

    @Mapping(source = "channel.id", target = "channelId")
    @Mapping(source = "channel.name", target = "channelName")
    NotificationPreferenceResponse toResponse(NotificationPreference entity);
}
