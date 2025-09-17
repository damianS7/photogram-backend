package com.damian.photogram.web.rest.post.dto.response;

public record PostLikeDataDto(
        Long postId,
        boolean hasBeenLiked,
        Long totalLikes
) {
}
