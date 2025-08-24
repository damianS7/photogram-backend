package com.damian.photogram.domain.post.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CommentCreateRequest(
        @NotBlank(message = "Comment cannot be empty")
        String comment
) {
}
