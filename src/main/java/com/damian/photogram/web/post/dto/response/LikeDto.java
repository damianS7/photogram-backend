package com.damian.photogram.web.post.dto.response;

public record LikeDto(
        Long id,
        Long postId,
        Long customerId
) {
}
