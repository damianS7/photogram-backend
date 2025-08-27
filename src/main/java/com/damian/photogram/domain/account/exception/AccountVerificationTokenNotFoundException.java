package com.damian.photogram.domain.account.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class AccountVerificationTokenNotFoundException extends ApplicationException {
    public AccountVerificationTokenNotFoundException(String message) {
        super(message);
    }
}
