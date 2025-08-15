package com.damian.photogram.accounts.activation;

import com.damian.photogram.accounts.AccountRepository;
import com.damian.photogram.accounts.token.AccountToken;
import com.damian.photogram.accounts.token.AccountTokenRepository;
import com.damian.photogram.accounts.token.exception.TokenExpiredException;
import com.damian.photogram.accounts.token.exception.TokenNotFoundException;
import com.damian.photogram.common.exception.Exceptions;
import com.damian.photogram.mail.EmailSenderService;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AccountVerificationService {
    private final AccountRepository accountRepository;
    private final AccountTokenRepository accountTokenRepository;
    private final EmailSenderService emailSenderService;

    public AccountVerificationService(
            AccountRepository accountRepository,
            AccountTokenRepository accountTokenRepository,
            EmailSenderService emailSenderService
    ) {
        this.accountRepository = accountRepository;
        this.accountTokenRepository = accountTokenRepository;
        this.emailSenderService = emailSenderService;
    }

    public AccountToken verify(String token) {
        // check the token if it matches with the one in the database
        AccountToken accountToken = accountTokenRepository
                .findByToken(token)
                .orElseThrow(
                        () -> new TokenNotFoundException(Exceptions.ACCOUNT_ACTIVATION.INVALID_TOKEN)
                );

        // check if the token has expired
        if (!accountToken.getExpiresAt().isAfter(Instant.now())) {
            throw new TokenExpiredException(Exceptions.ACCOUNT_ACTIVATION.EXPIRED_TOKEN);
        }

        return accountToken;
    }
}
