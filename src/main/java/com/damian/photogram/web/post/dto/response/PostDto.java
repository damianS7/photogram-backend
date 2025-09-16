package com.damian.photogram.web.post.dto.response;

public record PostDto(
        Long id,
        Long authorId,
        String description,
        String photoFilename,
        String createdAt
) {
}
