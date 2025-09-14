package com.damian.photogram.domain.follow.dto;

public record FollowDto(
        Long followedCustomerId,
        String followedCustomerUsername,
        String followedCustomerProfileImageFilename,
        Long followerCustomerId,
        String followerCustomerUsername,
        String followerCustomerProfileImageFilename
) {
}
