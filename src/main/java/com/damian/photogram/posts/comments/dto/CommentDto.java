package com.damian.photogram.posts.comments.dto;

public record CommentDto(
        Long id,
        Long postId,
        String username,
        String content,
        String createdAt
) {
}
