package com.damian.photogram.domain.user.exception;

public class AccountInvalidPasswordConfirmationException extends AccountException {
    public AccountInvalidPasswordConfirmationException(String message, Long accountId, Long customerId) {
        super(message, accountId, customerId);
    }
}
