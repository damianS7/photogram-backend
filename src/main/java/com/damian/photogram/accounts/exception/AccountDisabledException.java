package com.damian.photogram.accounts.exception;

import com.damian.photogram.accounts.auth.exception.AuthenticationException;

public class AccountDisabledException extends AuthenticationException {
    public AccountDisabledException(String message) {
        super(message);
    }
}
