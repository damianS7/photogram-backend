package com.damian.photogram.domain.post.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PostCreateRequest(
        @NotBlank(message = "Image filename cannot be empty")
        String photoFilename,

        // description can be empty
        @NotNull(message = "Description cannot be null")
        String description
) {
}
