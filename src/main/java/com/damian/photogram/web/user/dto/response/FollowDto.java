package com.damian.photogram.web.user.dto.response;

/**
 * Response with follow info between two users
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
