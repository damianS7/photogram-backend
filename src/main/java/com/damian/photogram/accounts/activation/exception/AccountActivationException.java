package com.damian.photogram.accounts.activation.exception;

import com.damian.photogram.accounts.auth.exception.AuthenticationException;

public class AccountActivationException extends AuthenticationException {
    public AccountActivationException(String message) {
        super(message);
    }
}
