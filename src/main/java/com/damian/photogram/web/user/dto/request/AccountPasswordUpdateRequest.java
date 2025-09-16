package com.damian.photogram.web.user.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request used to set a new password.
 *
 * @param currentPassword this is the current password
 * @param newPassword     this is the new password
 */
public record AccountPasswordUpdateRequest(
        @NotBlank(message = "Current password must not be blank")
        String currentPassword,

        @NotBlank(message = "New password must not be blank")
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
                message = "Password must be at least 8 characters long, contain at least one uppercase letter, " +
                          "one number, and one special character."
        )
        String newPassword
) {
}
