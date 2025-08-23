package com.damian.photogram.app.feed.dto.response;

public record FeedDto(
        Long customerId,
        String username,
        Long totalPosts,
        Long following,
        Long followers,
        String profileImageFilename,
        String aboutMe
) {
}
