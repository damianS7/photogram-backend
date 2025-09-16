package com.damian.photogram.domain.user.exception;

public class AccountVerificationTokenExpiredException extends AccountTokenException {
    public AccountVerificationTokenExpiredException(String message, String token, Long accountId, Long customerId) {
        super(message, token, accountId, customerId);
    }
}
