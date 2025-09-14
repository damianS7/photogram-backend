package com.damian.photogram.domain.user.account.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class AccountVerificationTokenExpiredException extends ApplicationException {
    public AccountVerificationTokenExpiredException(String message) {
        super(message);
    }
}
