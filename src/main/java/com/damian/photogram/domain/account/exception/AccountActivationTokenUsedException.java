package com.damian.photogram.domain.account.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class AccountActivationTokenUsedException extends ApplicationException {
    public AccountActivationTokenUsedException(String message) {
        super(message);
    }
}
