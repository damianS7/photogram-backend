package com.damian.photogram.web.rest.user.dto.response;

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
