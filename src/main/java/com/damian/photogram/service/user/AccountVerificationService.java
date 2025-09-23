package com.damian.photogram.service.user;

import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.domain.user.enums.AccountStatus;
import com.damian.photogram.domain.user.enums.AccountTokenType;
import com.damian.photogram.domain.user.exception.*;
import com.damian.photogram.domain.user.model.Account;
import com.damian.photogram.domain.user.model.AccountToken;
import com.damian.photogram.domain.user.model.Customer;
import com.damian.photogram.domain.user.repository.AccountRepository;
import com.damian.photogram.domain.user.repository.AccountTokenRepository;
import com.damian.photogram.infrastructure.mail.EmailSenderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class AccountVerificationService {
    private static final Logger log = LoggerFactory.getLogger(AccountVerificationService.class);
    private final Environment env;
    private final AccountTokenRepository accountTokenRepository;
    private final AccountRepository accountRepository;
    private final EmailSenderService emailSenderService;

    public AccountVerificationService(
            Environment env,
            AccountTokenRepository accountTokenRepository,
            AccountRepository accountRepository,
            EmailSenderService emailSenderService
    ) {
        this.env = env;
        this.accountTokenRepository = accountTokenRepository;
        this.accountRepository = accountRepository;
        this.emailSenderService = emailSenderService;
    }

    /**
     * Activate an account using the token
     *
     * @param token the token to activate the account.
     * @throws AccountNotFoundException               when the account cannot be found.
     * @throws AccountVerificationNotPendingException when the account is not pending for activation.
     */
    public Account verifyAccount(String token) {
        // check the token is valid and not expired.
        AccountToken accountToken = this.validateToken(token);

        log.debug("Verifying account from customer: {}", accountToken.getCustomer().getId());

        // find the customer associated with the token
        Account accountCustomer = accountRepository
                .findByCustomer_Id(accountToken.getCustomer().getId())
                .orElseThrow(
                        () -> {
                            log.warn("Failed to verify account. Account not found.");
                            return new AccountNotFoundException(Exceptions.ACCOUNT.NOT_FOUND);
                        }
                );

        // checks if the account is pending for activation.
        if (!accountCustomer.getAccountStatus().equals(AccountStatus.PENDING_VERIFICATION)) {
            log.warn("Failed to verify account. Account is not awaiting verification.");
            throw new AccountVerificationNotPendingException(
                    Exceptions.ACCOUNT.VERIFICATION.NOT_ELIGIBLE,
                    accountCustomer.getId(),
                    accountCustomer.getCustomerId()
            );
        }

        // mark the token as used
        accountToken.setUsed(true);
        accountTokenRepository.save(accountToken);

        // update account status to active
        accountCustomer.setAccountStatus(
                AccountStatus.VERIFIED
        );

        // set the time at what the account was updated
        accountCustomer.setUpdatedAt(Instant.now());

        log.debug("Account successfully verified.");
        return accountRepository.save(accountCustomer);
    }

    /**
     * Validate if the token matches with the one in the database
     * also run checks for expiration and usage of the token.
     *
     * @param token the token to verify
     * @return AccountToken the token entity
     */
    public AccountToken validateToken(String token) {
        log.debug("Validating token");
        // check the token if it matches with the one in database
        AccountToken accountToken = accountTokenRepository
                .findByToken(token)
                .orElseThrow(
                        () -> {
                            log.error("Failed to validate token. Token not found.");
                            return new AccountTokenNotFoundException(
                                    Exceptions.ACCOUNT.VERIFICATION.TOKEN.NOT_FOUND,
                                    token,
                                    null,
                                    null
                            );
                        }
                );

        // check expiration
        if (!accountToken.getExpiresAt().isAfter(Instant.now())) {
            log.error("Failed to validate token. Token expired.");
            throw new AccountVerificationTokenExpiredException(
                    Exceptions.ACCOUNT.VERIFICATION.TOKEN.EXPIRED,
                    token,
                    accountToken.getCustomer().getAccount().getId(),
                    accountToken.getCustomerId()
            );
        }

        // check if token is already used
        if (accountToken.isUsed()) {
            log.error("Failed to validate token. Token used.");
            throw new AccountVerificationTokenUsedException(
                    Exceptions.ACCOUNT.VERIFICATION.TOKEN.USED,
                    token,
                    accountToken.getCustomer().getAccount().getId(),
                    accountToken.getCustomer().getId()
            );
        }

        return accountToken;
    }

    /**
     * It sends a welcome message to the customer email address after verification.
     *
     * @param customer The customer to send a welcome message to.
     */
    public void sendAccountVerifiedEmail(Customer customer) {
        emailSenderService.send(
                customer.getEmail(),
                "Welcome to Photogram!",
                "Your account has been verified successfully."
        );
    }

    /**
     * Create a new verification token associated to the customer.
     *
     * @param email The email address of the customer.
     * @return An AccountToken object containing the verification token.
     * @throws AccountNotFoundException               If the customer is not found.
     * @throws AccountVerificationNotPendingException If the account is not pending for verification.
     */
    public AccountToken generateVerificationToken(String email) {
        log.debug("Generating verification token for: {}", email);
        // retrieve the customer by email
        Account account = accountRepository.findByCustomer_Email(email).orElseThrow(
                () -> {
                    log.error("Failed to generate verification token. Account for: {} not found.", email);
                    return new AccountNotFoundException(Exceptions.ACCOUNT.NOT_FOUND);
                }
        );

        // only account pending for verification can request the email
        if (!account.getAccountStatus().equals(AccountStatus.PENDING_VERIFICATION)) {
            log.error("Failed to generate verification token. Account for: {} is not awaiting verification.", email);
            throw new AccountVerificationNotPendingException(
                    Exceptions.ACCOUNT.VERIFICATION.NOT_ELIGIBLE,
                    account.getId(),
                    account.getCustomerId()
            );
        }

        // check if AccountToken exists orElse create a new one
        AccountToken accountToken = accountTokenRepository
                .findByCustomer_Id(account.getOwner().getId())
                .orElseGet(
                        AccountToken::new
                );

        // we set the accountToken data
        accountToken.setCustomer(account.getOwner())
                    .setType(AccountTokenType.ACCOUNT_VERIFICATION)
                    .setToken(UUID.randomUUID().toString())
                    .setCreatedAt(Instant.now())
                    .setExpiresAt(Instant.now().plus(1, ChronoUnit.DAYS));

        log.debug("Verification token: {} for: {} generated.", accountToken.getToken(), email);
        // save the token to the database
        return accountTokenRepository.save(
                accountToken
        );
    }

    /**
     * It sends an email with a verification link
     *
     * @param email The email address to send the verification link to.
     * @param token The token that will be used to verify the user's account.
     */
    public void sendAccountVerificationLinkEmail(String email, String token) {
        String host = env.getProperty("app.frontend.host");
        String port = env.getProperty("app.frontend.port");
        String url = String.format("http://%s:%s", host, port);
        String activationLink = url + "/accounts/verification/" + token;

        // Send email to confirm registration
        emailSenderService.send(
                email,
                "Photogram account verification link.",
                "Please click on the link below to confirm your registration: \n\n" + activationLink
        );
    }
}
