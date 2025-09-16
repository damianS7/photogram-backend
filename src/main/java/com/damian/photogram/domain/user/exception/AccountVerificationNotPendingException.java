package com.damian.photogram.domain.user.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class AccountVerificationNotPendingException extends ApplicationException {
    public AccountVerificationNotPendingException(String message) {
        super(message);
    }
}
