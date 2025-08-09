package com.damian.photogram.follow.dto;

public record FollowDto(
        Long id,
        Long followedCustomerId,
        String followedCustomerUsername,
        Long followerCustomerId,
        String followerCustomerUsername,
        String followerCustomerProfileImageFilename
) {
}
