package com.damian.photogram.domain.user.exception;

public class AccountNotFoundException extends AccountException {
    public AccountNotFoundException(String message, Long accountId, Long customerId) {
        super(message, accountId, customerId);
    }

    public AccountNotFoundException(String message) {
        this(message, null, null);
    }
}
