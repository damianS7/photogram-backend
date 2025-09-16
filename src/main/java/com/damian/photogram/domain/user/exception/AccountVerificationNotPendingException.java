package com.damian.photogram.domain.user.exception;

public class AccountVerificationNotPendingException extends AccountException {
    public AccountVerificationNotPendingException(String message, Long accountId, Long customerId) {
        super(message, accountId, customerId);
    }
}
