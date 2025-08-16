package com.damian.photogram.domain.customer.dto.response;

public record FollowDto(
        Long followedCustomerId,
        String followedCustomerUsername,
        String followedCustomerProfileImageFilename,
        Long followerCustomerId,
        String followerCustomerUsername,
        String followerCustomerProfileImageFilename
) {
}
