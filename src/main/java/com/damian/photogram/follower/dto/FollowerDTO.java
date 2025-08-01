package com.damian.photogram.follower.dto;

public record FollowerDTO(
        Long id,
        Long customerId,
        String name,
        String avatarFilename
) {
}
