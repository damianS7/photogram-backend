package com.damian.photogram.web.notification.dto;

import com.damian.photogram.domain.notification.NotificationType;

import java.util.Map;

public record NotificationEvent(
        Long recipientId,
        NotificationType type,
        Map<String, Object> metadata,
        String message,
        String createdAt
) {
}