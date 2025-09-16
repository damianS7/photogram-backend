package com.damian.photogram.domain.user.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class AccountException extends ApplicationException {
    private final Long customerId;
    private final Long accountId;

    public AccountException(String message, Long accountId, Long customerId) {
        super(message);
        this.customerId = customerId;
        this.accountId = accountId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public Long getAccountId() {
        return accountId;
    }
}
