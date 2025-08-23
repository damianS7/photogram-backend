package com.damian.photogram.app.auth.dto;

// Used for returning the token after successful authentication.
public record AuthenticationResponse(
        String token
) {
}

