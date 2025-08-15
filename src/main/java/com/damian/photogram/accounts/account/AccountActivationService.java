package com.damian.photogram.accounts.account;

import com.damian.photogram.accounts.account.exception.AccountActivationException;
import com.damian.photogram.accounts.account.exception.AccountNotFoundException;
import com.damian.photogram.accounts.token.AccountToken;
import com.damian.photogram.accounts.token.AccountTokenRepository;
import com.damian.photogram.accounts.token.AccountTokenType;
import com.damian.photogram.common.exception.Exceptions;
import com.damian.photogram.customers.CustomerRepository;
import com.damian.photogram.mail.EmailSenderService;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class AccountActivationService {
    private final Environment env;
    private final CustomerRepository customerRepository;
    private final AccountTokenRepository accountTokenRepository;
    private final AccountRepository accountRepository;
    private final EmailSenderService emailSenderService;
    private final AccountActivationTokenVerificationService accountActivationTokenVerificationService;

    public AccountActivationService(
            Environment env,
            CustomerRepository customerRepository,
            AccountTokenRepository accountTokenRepository,
            AccountRepository accountRepository,
            EmailSenderService emailSenderService,
            AccountActivationTokenVerificationService accountActivationTokenVerificationService
    ) {
        this.env = env;
        this.customerRepository = customerRepository;
        this.accountTokenRepository = accountTokenRepository;
        this.accountRepository = accountRepository;
        this.emailSenderService = emailSenderService;
        this.accountActivationTokenVerificationService = accountActivationTokenVerificationService;
    }

    // Activate an account using the token
    public void activate(String token) {
        // check the token is valid and not expired.
        AccountToken accountToken = accountActivationTokenVerificationService.verify(token);

        // find the customer associated with the token
        Account accountCustomer = accountRepository
                .findByCustomer_Id(accountToken.getCustomer().getId())
                .orElseThrow(
                        () -> new AccountNotFoundException(Exceptions.ACCOUNT.NOT_FOUND)
                );

        // checks if the account is pending for activation.
        if (!accountCustomer.getAccountStatus().equals(AccountStatus.PENDING_VERIFICATION)) {
            throw new AccountActivationException(Exceptions.ACCOUNT_ACTIVATION.NOT_ELEGIBLE_FOR_ACTIVATION);
        }

        // mark the token as used
        accountToken.setUsed(true);
        accountTokenRepository.save(accountToken);

        // update accounts status to active
        accountCustomer.setAccountStatus(
                AccountStatus.ACTIVE
        );

        accountRepository.save(accountCustomer);

        // notify activation or welcome
        emailSenderService.send(
                accountCustomer.getCustomer().getEmail(),
                "Welcome to photogram!",
                "Your accounts has been activated successfully."
        );
    }

    // Send an activation email to the customer email address
    public void sendAccountActivationToken(String email) {
        // retrieve the customer by email
        Account account = accountRepository.findByCustomer_Email(email).orElseThrow(
                () -> new AccountNotFoundException(Exceptions.ACCOUNT.NOT_FOUND_BY_EMAIL)
        );

        // only accounts pending for verification can request the email
        if (!account.getAccountStatus().equals(AccountStatus.PENDING_VERIFICATION)) {
            throw new AccountActivationException(Exceptions.ACCOUNT_ACTIVATION.NOT_ELEGIBLE_FOR_ACTIVATION);
        }

        // check if AccountToken exists orElse create a new one
        AccountToken accountToken = accountTokenRepository.findByCustomer_Id(account.getCustomer().getId()).orElseGet(
                AccountToken::new
        );

        // we set the accountToken data
        accountToken.setCustomer(account.getCustomer());
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

        String url = env.getProperty("app.frontend.url");
        String activationLink = url + "/auth/activate-account/" + token;
        // Send email to confirm registration
        emailSenderService.send(
                email,
                "Photogram accounts activation link.",
                "Please click on the link below to confirm your registration: \n\n" + activationLink
        );
    }
}
