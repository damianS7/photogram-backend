package com.damian.photogram.domain.user.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class AccountInvalidPasswordConfirmationException extends ApplicationException {
    private final Long customerId;

    public AccountInvalidPasswordConfirmationException(String message, Long customerId) {
        super(message);
        this.customerId = customerId;
    }

    public AccountInvalidPasswordConfirmationException(String message) {
        super(message);
        this.customerId = null;
    }

    public Long getCustomerId() {
        return customerId;
    }
}
