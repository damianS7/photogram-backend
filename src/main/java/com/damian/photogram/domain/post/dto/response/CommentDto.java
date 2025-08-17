package com.damian.photogram.domain.post.dto.response;

public record CommentDto(
        Long id,
        Long postId,
        String username,
        String content,
        String createdAt
) {
}
