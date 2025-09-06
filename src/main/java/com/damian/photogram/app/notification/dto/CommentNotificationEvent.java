package com.damian.photogram.app.notification.dto;

public record CommentNotificationEvent(
        Long postId,
        String username,
        String content,
        String createdAt
) {
}
