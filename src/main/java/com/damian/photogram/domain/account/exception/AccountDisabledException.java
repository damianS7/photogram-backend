package com.damian.photogram.domain.account.exception;

import com.damian.photogram.app.auth.exception.AuthenticationException;

public class AccountDisabledException extends AuthenticationException {
    public AccountDisabledException(String message) {
        super(message);
    }
}
