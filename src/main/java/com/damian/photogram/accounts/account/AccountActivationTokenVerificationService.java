package com.damian.photogram.accounts.account;

import com.damian.photogram.accounts.account.exception.AccountActivationTokenExpiredException;
import com.damian.photogram.accounts.account.exception.AccountActivationTokenNotFoundException;
import com.damian.photogram.accounts.token.AccountToken;
import com.damian.photogram.accounts.token.AccountTokenRepository;
import com.damian.photogram.common.exception.Exceptions;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AccountActivationTokenVerificationService {
    private final AccountTokenRepository accountTokenRepository;

    public AccountActivationTokenVerificationService(
            AccountTokenRepository accountTokenRepository
    ) {
        this.accountTokenRepository = accountTokenRepository;
    }

    // Verifies if the token matches with the one in the database and is not
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

        return accountToken;
    }
}
