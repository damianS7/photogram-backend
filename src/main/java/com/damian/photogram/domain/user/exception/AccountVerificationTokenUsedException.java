package com.damian.photogram.domain.user.exception;

public class AccountVerificationTokenUsedException extends AccountTokenException {
    public AccountVerificationTokenUsedException(String message, String token, Long accountId, Long customerId) {
        super(message, token, accountId, customerId);
    }
}
