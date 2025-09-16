package com.damian.photogram.web.notification.dto.response;

import com.damian.photogram.domain.notification.NotificationType;

import java.util.Map;

public record NotificationDto(
        Long id,
        NotificationType type,
        String message,
        Map<String, Object> metadata,
        String createdAt
) {
}