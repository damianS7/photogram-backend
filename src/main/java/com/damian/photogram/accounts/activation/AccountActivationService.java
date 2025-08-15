package com.damian.photogram.accounts.activation;

import com.damian.photogram.accounts.Account;
import com.damian.photogram.accounts.AccountRepository;
import com.damian.photogram.accounts.AccountStatus;
import com.damian.photogram.accounts.activation.exception.AccountActivationException;
import com.damian.photogram.accounts.exception.AccountNotFoundException;
import com.damian.photogram.accounts.token.AccountToken;
import com.damian.photogram.accounts.token.AccountTokenRepository;
import com.damian.photogram.accounts.token.AccountTokenType;
import com.damian.photogram.common.exception.Exceptions;
import com.damian.photogram.customers.Customer;
import com.damian.photogram.customers.CustomerRepository;
import com.damian.photogram.customers.exception.CustomerNotFoundException;
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
    private final AccountVerificationService accountVerificationService;

    public AccountActivationService(
            Environment env, CustomerRepository customerRepository,
            AccountTokenRepository accountTokenRepository,
            AccountRepository accountRepository,
            EmailSenderService emailSenderService,
            AccountVerificationService accountVerificationService
    ) {
        this.env = env;
        this.customerRepository = customerRepository;
        this.accountTokenRepository = accountTokenRepository;
        this.accountRepository = accountRepository;
        this.emailSenderService = emailSenderService;
        this.accountVerificationService = accountVerificationService;
    }

    // Activate an accounts using the token
    public void activate(String token) {
        // check the token is valid and not expired.
        AccountToken accountToken = accountVerificationService.verify(token);

        Account accountCustomer = accountRepository
                .findByCustomer_Id(accountToken.getCustomer().getId())
                .orElseThrow(
                        () -> new AccountNotFoundException(Exceptions.ACCOUNT.NOT_FOUND)
                );

        // only accounts pending for verification can request the email
        if (!accountCustomer.getAccountStatus().equals(AccountStatus.PENDING_VERIFICATION)) {
            throw new AccountActivationException(Exceptions.ACCOUNT_ACTIVATION.NOT_VERIFICATION_PENDING);
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

    // Send an activation email to the customer's email address
    public void sendAccountActivationToken(String email) {
        // retrieve the customer by email
        Customer customer = customerRepository.findByEmail(email).orElseThrow(
                () -> new CustomerNotFoundException(Exceptions.CUSTOMER.NOT_FOUND)
        );

        // only accounts pending for verification can request the email
        if (!customer.getAccount().getAccountStatus().equals(AccountStatus.PENDING_VERIFICATION)) {
            throw new AccountActivationException(Exceptions.ACCOUNT_ACTIVATION.NOT_VERIFICATION_PENDING);
        }

        // check if AccountToken exists orElse create a new one
        AccountToken accountToken = accountTokenRepository.findByCustomer_Id(customer.getId()).orElseGet(
                AccountToken::new
        );

        // we set the accountToken data
        accountToken.setCustomer(customer);
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
        String activationLink = url + "/auth/activate-accounts/" + token;
        // Send email to confirm registration
        emailSenderService.send(
                email,
                "Photogram accounts activation link.",
                "Please click on the link below to confirm your registration: \n\n" + activationLink
        );
    }
}
