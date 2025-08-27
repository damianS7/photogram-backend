package com.damian.photogram.domain.account.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class AccountVerificationTokenMismatchException extends ApplicationException {
    public AccountVerificationTokenMismatchException(String message) {
        super(message);
    }
}
