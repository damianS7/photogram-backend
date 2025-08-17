package com.damian.photogram.domain.post.dto.response;

public record PostCreateRequest(
        String photoFilename,
        String description
) {
}
