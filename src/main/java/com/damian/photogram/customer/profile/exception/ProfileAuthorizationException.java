package com.damian.photogram.customer.profile.exception;

import com.damian.photogram.auth.exception.AuthorizationException;

public class ProfileAuthorizationException extends AuthorizationException {
    public ProfileAuthorizationException(String message) {
        super(message);
    }
}
