package com.damian.photogram.app.notification.dto;

public record LikeNotificationEvent(
        Long postId,
        String username,
        String content,
        String createdAt
) {
}
