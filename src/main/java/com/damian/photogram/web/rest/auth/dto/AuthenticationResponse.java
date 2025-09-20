package com.damian.photogram.web.rest.auth.dto;

/**
 * Used for returning the token after successful authentication.
 *
 * @param token jwt token
 */
public record AuthenticationResponse(
        String token
) {
}

