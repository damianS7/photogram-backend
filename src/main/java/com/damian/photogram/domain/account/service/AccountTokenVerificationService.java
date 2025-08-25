package com.damian.photogram.domain.account.service;

import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.domain.account.exception.AccountActivationTokenExpiredException;
import com.damian.photogram.domain.account.exception.AccountActivationTokenNotFoundException;
import com.damian.photogram.domain.account.exception.AccountActivationTokenUsedException;
import com.damian.photogram.domain.account.model.AccountToken;
import com.damian.photogram.domain.account.repository.AccountTokenRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AccountTokenVerificationService {
    private final AccountTokenRepository accountTokenRepository;

    public AccountTokenVerificationService(
            AccountTokenRepository accountTokenRepository
    ) {
        this.accountTokenRepository = accountTokenRepository;
    }

    /**
     * Verifies if the token matches with the one in the database
     * also run checks for expiration and usage of the token.
     *
     * @param token the token to verify
     * @return AccountToken the token entity
     */
    public AccountToken verify(String token) {
        // check the token if it matches with the one in database
        AccountToken accountToken = accountTokenRepository
                .findByToken(token)
                .orElseThrow(
                        () -> new AccountActivationTokenNotFoundException(Exceptions.ACCOUNT_ACTIVATION.INVALID_TOKEN)
                );

        // check expiration
        if (!accountToken.getExpiresAt().isAfter(Instant.now())) {
            throw new AccountActivationTokenExpiredException(Exceptions.ACCOUNT_ACTIVATION.EXPIRED_TOKEN);
        }

        // check if token is already used
        if (accountToken.isUsed()) {
            throw new AccountActivationTokenUsedException(Exceptions.ACCOUNT_ACTIVATION.TOKEN_USED);
        }

        return accountToken;
    }
}
