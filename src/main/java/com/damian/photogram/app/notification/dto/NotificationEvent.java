package com.damian.photogram.app.notification.dto;

import com.damian.photogram.app.notification.NotificationType;

import java.util.Map;

public record NotificationEvent(
        Long recipientId,
        NotificationType type,
        Map<String, Object> metadata,
        String message,
        String createdAt
) {
}