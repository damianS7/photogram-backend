package com.damian.photogram.app.notification.dto;

import com.damian.photogram.app.notification.NotificationType;

import java.util.Map;

public record NotificationDto(
        Long id,
        NotificationType type,
        String message,
        Map<String, Object> metadata,
        String createdAt
) {
}