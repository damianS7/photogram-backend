package com.damian.photogram.domain.post.dto.response;

public record PostLikeDataDto(
        Long postId,
        boolean hasBeenLiked,
        Long totalLikes
) {
}
