package com.damian.photogram.web.user.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AccountPasswordResetSetRequest(
        @NotBlank(message = "Password must not be blank")
        String password
) {
}
