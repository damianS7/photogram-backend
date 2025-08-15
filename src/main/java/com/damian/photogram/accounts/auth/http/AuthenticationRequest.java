package com.damian.photogram.accounts.auth.http;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request with required fields for login
 */
public record AuthenticationRequest(
        @NotBlank(message = "Email must not be blank")
        @Email(message = "Email must be a well-formed email address.")
        String email,

        @NotBlank(message = "Password must not be blank")
        String password
) {
}
