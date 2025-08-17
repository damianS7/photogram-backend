package com.damian.photogram.domain.post.dto.response;

public record LikeDto(
        Long id,
        Long postId,
        Long customerId
) {
}
