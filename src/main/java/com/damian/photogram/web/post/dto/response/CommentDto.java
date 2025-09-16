package com.damian.photogram.web.post.dto.response;

public record CommentDto(
        Long id,
        Long postId,
        String username,
        String message,
        String createdAt
) {
}
