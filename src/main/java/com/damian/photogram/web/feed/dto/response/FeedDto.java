package com.damian.photogram.web.feed.dto.response;

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
