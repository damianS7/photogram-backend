package com.damian.photogram.feed.dto;

public record FeedDTO(
        Long customerId,
        String username,
        Long totalPosts,
        Long following,
        Long followers,
        String profileImageFilename,
        String aboutMe
) {
}
