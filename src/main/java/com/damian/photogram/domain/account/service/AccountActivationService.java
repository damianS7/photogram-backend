package com.damian.photogram.domain.account.service;

import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.core.service.EmailSenderService;
import com.damian.photogram.domain.account.enums.AccountStatus;
import com.damian.photogram.domain.account.enums.AccountTokenType;
import com.damian.photogram.domain.account.exception.AccountActivationNotPendingException;
import com.damian.photogram.domain.account.exception.AccountNotFoundException;
import com.damian.photogram.domain.account.model.Account;
import com.damian.photogram.domain.account.model.AccountToken;
import com.damian.photogram.domain.account.repository.AccountRepository;
import com.damian.photogram.domain.account.repository.AccountTokenRepository;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class AccountActivationService {
    private final Environment env;
    private final AccountTokenRepository accountTokenRepository;
    private final AccountRepository accountRepository;
    private final EmailSenderService emailSenderService;
    private final AccountTokenVerificationService accountTokenVerificationService;

    public AccountActivationService(
            Environment env,
            AccountTokenRepository accountTokenRepository,
            AccountRepository accountRepository,
            EmailSenderService emailSenderService,
            AccountTokenVerificationService accountTokenVerificationService
    ) {
        this.env = env;
        this.accountTokenRepository = accountTokenRepository;
        this.accountRepository = accountRepository;
        this.emailSenderService = emailSenderService;
        this.accountTokenVerificationService = accountTokenVerificationService;
    }

    // Activate an account using the token
    public void activate(String token) {
        // check the token is valid and not expired.
        AccountToken accountToken = accountTokenVerificationService.verify(token);

        // find the customer associated with the token
        Account accountCustomer = accountRepository
                .findByCustomer_Id(accountToken.getCustomer().getId())
                .orElseThrow(
                        () -> new AccountNotFoundException(Exceptions.ACCOUNT.NOT_FOUND)
                );

        // checks if the account is pending for activation.
        if (!accountCustomer.getAccountStatus().equals(AccountStatus.PENDING_VERIFICATION)) {
            throw new AccountActivationNotPendingException(Exceptions.ACCOUNT_ACTIVATION.NOT_ELEGIBLE_FOR_ACTIVATION);
        }

        // mark the token as used
        accountToken.setUsed(true);
        accountTokenRepository.save(accountToken);

        // update account status to active
        accountCustomer.setAccountStatus(
                AccountStatus.ACTIVE
        );

        accountRepository.save(accountCustomer);

        // notify activation or welcome
        emailSenderService.send(
                accountCustomer.getOwner().getEmail(),
                "Welcome to photogram!",
                "Your account has been activated successfully."
        );
    }

    // Send an activation email to the email address
    public void sendAccountActivationToken(String email) {
        // retrieve the customer by email
        Account account = accountRepository.findByCustomer_Email(email).orElseThrow(
                () -> new AccountNotFoundException(Exceptions.ACCOUNT.NOT_FOUND_BY_EMAIL)
        );

        // only account pending for verification can request the email
        if (!account.getAccountStatus().equals(AccountStatus.PENDING_VERIFICATION)) {
            throw new AccountActivationNotPendingException(Exceptions.ACCOUNT_ACTIVATION.NOT_ELEGIBLE_FOR_ACTIVATION);
        }

        // check if AccountToken exists orElse create a new one
        AccountToken accountToken = accountTokenRepository.findByCustomer_Id(account.getOwner().getId()).orElseGet(
                AccountToken::new
        );

        // we set the accountToken data
        accountToken.setCustomer(account.getOwner());
        accountToken.setType(AccountTokenType.ACCOUNT_VERIFICATION);
        accountToken.setToken(UUID.randomUUID().toString());
        accountToken.setCreatedAt(Instant.now());
        accountToken.setExpiresAt(Instant.now().plus(1, ChronoUnit.DAYS));

        accountTokenRepository.save(
                accountToken
        );

        sendAccountActivationTokenEmail(email, accountToken.getToken());
    }

    public void sendAccountActivationTokenEmail(String email, String token) {

        String host = env.getProperty("app.frontend.host");
        String port = env.getProperty("app.frontend.port");
        String url = String.format("http://%s:%s", host, port);
        String activationLink = url + "/auth/accounts/activate/" + token;
        // Send email to confirm registration
        emailSenderService.send(
                email,
                "Photogram account activation link.",
                "Please click on the link below to confirm your registration: \n\n" + activationLink
        );
    }
}
