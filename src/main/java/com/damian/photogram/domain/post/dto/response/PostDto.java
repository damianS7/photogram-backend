package com.damian.photogram.domain.post.dto.response;

public record PostDto(
        Long id,
        Long customerId,
        String description,
        String photoFilename,
        String createdAt
) {
}
