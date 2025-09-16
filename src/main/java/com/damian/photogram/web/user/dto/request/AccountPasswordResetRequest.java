package com.damian.photogram.web.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * This is the request used to reset password through email.
 * Email must match with the one in db otherwise nothing will be sent.
 *
 * @param email the email where the token reset will be sent.
 */
public record AccountPasswordResetRequest(
        @NotBlank(message = "Email must not be blank")
        @Email(message = "Email must be a well-formed email address.")
        String email
) {
}
