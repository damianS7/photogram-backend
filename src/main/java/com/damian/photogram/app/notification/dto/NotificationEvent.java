package com.damian.photogram.app.notification.dto;

import com.damian.photogram.app.notification.NotificationType;

public record NotificationEvent(
        NotificationType type,
        Long postId,
        Long senderId,
        String senderUsername,
        Long recipientId,
        String content,
        String createdAt
) {
}