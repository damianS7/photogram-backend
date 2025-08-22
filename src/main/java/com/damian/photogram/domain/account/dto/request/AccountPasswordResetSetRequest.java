package com.damian.photogram.domain.account.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * TODO
 */
public record AccountPasswordResetSetRequest(
        @NotBlank(message = "Email must not be blank")
        @Email(message = "Email must be a well-formed email address.")
        String email,

        @NotBlank(message = "Password must not be blank")
        String password
) {
}
