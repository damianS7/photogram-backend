package com.damian.photogram.web.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * This is the request the user send to establish a new password along with the token.
 *
 * @param password
 */
public record AccountPasswordResetSetRequest(
        @NotBlank(message = "Password must not be blank")
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
                message = "Password must be at least 8 characters long, contain at least one uppercase letter, " +
                          "one number, and one special character."
        )
        String password
) {
}
