package com.damian.photogram.domain.user.exception;

public class AccountTokenNotFoundException extends AccountTokenException {
    public AccountTokenNotFoundException(String message, String token, Long accountId, Long customerId) {
        super(message, token, accountId, customerId);
    }
}
