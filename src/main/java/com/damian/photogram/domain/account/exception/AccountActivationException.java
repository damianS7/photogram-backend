package com.damian.photogram.domain.account.exception;

import com.damian.photogram.app.auth.exception.AuthenticationException;

public class AccountActivationException extends AuthenticationException {
    public AccountActivationException(String message) {
        super(message);
    }
}
