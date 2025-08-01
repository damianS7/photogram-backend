package com.damian.photogram.auth.http;

import jakarta.validation.constraints.NotNull;

public record PasswordConfirmationRequest(
        @NotNull(
                message = "Password must not be null"
        )
        String password
) {
}
