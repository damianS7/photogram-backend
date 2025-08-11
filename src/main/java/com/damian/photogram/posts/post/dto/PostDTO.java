package com.damian.photogram.posts.post.dto;

public record PostDTO(
        Long id,
        Long customerId,
        String description,
        String photoFilename,
        String createdAt
) {
}
