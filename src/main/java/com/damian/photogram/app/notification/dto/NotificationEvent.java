package com.damian.photogram.app.notification.dto;

import com.damian.photogram.app.notification.NotificationEventType;

public record NotificationEvent(
        NotificationEventType type,
        Long postId,
        Long senderId,
        String senderUsername,
        Long recipientId,
        String content,
        String createdAt
) {
}