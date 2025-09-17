package com.damian.photogram.web.rest.post.dto.response;

public record LikeDto(
        Long id,
        Long postId,
        Long customerId
) {
}
