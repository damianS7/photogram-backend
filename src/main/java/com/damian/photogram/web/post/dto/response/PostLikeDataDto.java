package com.damian.photogram.web.post.dto.response;

public record PostLikeDataDto(
        Long postId,
        boolean hasBeenLiked,
        Long totalLikes
) {
}
