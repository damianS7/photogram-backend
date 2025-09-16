package com.damian.photogram.domain.user.exception;

public class AccountTokenException extends AccountException {
    private final String token;

    public AccountTokenException(String message, String token, Long accountId) {
        this(message, token, accountId, null);
    }

    public AccountTokenException(String message, String token, Long accountId, Long customerId) {
        super(message, accountId, customerId);
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
