package com.damian.photogram.app.notification.dto;

public record FollowNotificationEvent(
        Long followerId,
        Long followedId,
        String username,
        String content,
        String createdAt
) {
}
