package com.damian.photogram.customer.dto;

public record CustomerNonFriendDTO(
        Long customerId,
        String firstName,
        String lastName,
        String avatarFilename
) {
}