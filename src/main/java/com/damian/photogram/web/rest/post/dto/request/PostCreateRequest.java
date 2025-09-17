package com.damian.photogram.web.rest.post.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PostCreateRequest(
        @NotBlank(message = "Image filename cannot be empty")
        String imageFilename,

        @NotBlank(message = "Description cannot be empty")
        String description
) {
}
