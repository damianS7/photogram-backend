package com.damian.photogram.web.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request again the email with the token activation.
 *
 * @param email
 */
public record AccountActivationResendRequest(
        @NotBlank(message = "Email must not be blank")
        @Email(message = "Email must be a well-formed email address.")
        String email
) {
}
