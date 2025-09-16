package com.damian.photogram.domain.user.exception;

public class AccountVerificationTokenNotFoundException extends AccountTokenException {
    public AccountVerificationTokenNotFoundException(String message, String token, Long accountId, Long customerId) {
        super(message, token, accountId, customerId);
    }
}
