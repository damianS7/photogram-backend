package com.damian.photogram.web.rest.post.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CommentCreateRequest(
        @NotBlank(message = "Comment cannot be empty")
        String comment
) {
}
