package com.damian.photogram.app.auth.dto;

/**
 * Used for returning the token after successful authentication.
 *
 * @param token jwt token
 */
public record AuthenticationResponse(
        String token
) {
}

