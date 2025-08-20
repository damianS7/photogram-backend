package com.damian.photogram.domain.account.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class AccountActivationNotPendingException extends ApplicationException {
    public AccountActivationNotPendingException(String message) {
        super(message);
    }
}
