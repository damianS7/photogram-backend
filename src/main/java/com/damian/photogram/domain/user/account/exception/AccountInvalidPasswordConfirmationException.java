package com.damian.photogram.domain.user.account.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class AccountInvalidPasswordConfirmationException extends ApplicationException {
    public AccountInvalidPasswordConfirmationException(String message) {
        super(message);
    }
}
