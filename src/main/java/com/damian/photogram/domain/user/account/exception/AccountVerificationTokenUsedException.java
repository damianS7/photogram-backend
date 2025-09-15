package com.damian.photogram.domain.user.account.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class AccountVerificationTokenUsedException extends ApplicationException {
    private final Long customerId;

    public AccountVerificationTokenUsedException(String message, Long customerId) {
        super(message);
        this.customerId = customerId;
    }

    public AccountVerificationTokenUsedException(String message) {
        super(message);
        this.customerId = null;
    }

    public Long getCustomerId() {
        return customerId;
    }
}
