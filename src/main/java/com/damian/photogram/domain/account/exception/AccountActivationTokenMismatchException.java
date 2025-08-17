package com.damian.photogram.domain.account.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class AccountActivationTokenMismatchException extends ApplicationException {
    public AccountActivationTokenMismatchException(String message) {
        super(message);
    }
}
