package com.damian.photogram.app.notification.dto;

public record NotificationEvent(
        Long postId,
        Long senderId,
        String senderUsername,
        Long recipientId,
        String content,
        String createdAt
) {
}