package com.damian.photogram.customers.dto;

public record CustomerNonFriendDTO(
        Long customerId,
        String firstName,
        String lastName,
        String avatarFilename
) {
}