package com.damian.photogram.domain.follow.dto;

/**
 * Response with follow info
 */
public record FollowDto(
        Long followerCustomerId,
        String followerCustomerUsername,
        String followerCustomerProfileImageFilename,
        Long followedCustomerId,
        String followedCustomerUsername,
        String followedCustomerProfileImageFilename
) {
}
