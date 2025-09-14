package com.damian.photogram.domain.user.account.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class AccountVerificationTokenUsedException extends ApplicationException {
    public AccountVerificationTokenUsedException(String message) {
        super(message);
    }
}
