package com.damian.photogram.auth.exception;

public class AccountDisabledException extends AuthenticationException {
    public AccountDisabledException(String message) {
        super(message);
    }
}
