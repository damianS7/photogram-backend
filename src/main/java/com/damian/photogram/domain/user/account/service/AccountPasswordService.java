package com.damian.photogram.domain.user.account.service;

import com.damian.photogram.core.common.AuthHelper;
import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.core.mail.service.EmailSenderService;
import com.damian.photogram.domain.user.account.dto.request.AccountPasswordResetRequest;
import com.damian.photogram.domain.user.account.dto.request.AccountPasswordResetSetRequest;
import com.damian.photogram.domain.user.account.dto.request.AccountPasswordUpdateRequest;
import com.damian.photogram.domain.user.account.enums.AccountTokenType;
import com.damian.photogram.domain.user.account.exception.AccountInvalidPasswordConfirmationException;
import com.damian.photogram.domain.user.account.exception.AccountNotFoundException;
import com.damian.photogram.domain.user.account.model.Account;
import com.damian.photogram.domain.user.account.model.AccountToken;
import com.damian.photogram.domain.user.account.repository.AccountRepository;
import com.damian.photogram.domain.user.account.repository.AccountTokenRepository;
import com.damian.photogram.domain.user.customer.exception.CustomerNotFoundException;
import com.damian.photogram.domain.user.customer.model.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AccountPasswordService {
    private static final Logger log = LoggerFactory.getLogger(AccountPasswordService.class);
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AccountRepository accountRepository;
    private final EmailSenderService emailSenderService;
    private final AccountTokenRepository accountTokenRepository;
    private final Environment env;
    private final AccountVerificationService accountVerificationService;

    public AccountPasswordService(
            BCryptPasswordEncoder bCryptPasswordEncoder,
            AccountRepository accountRepository,
            EmailSenderService emailSenderService,
            AccountTokenRepository accountTokenRepository,
            Environment env,
            AccountVerificationService accountVerificationService
    ) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.accountRepository = accountRepository;
        this.emailSenderService = emailSenderService;
        this.accountTokenRepository = accountTokenRepository;
        this.env = env;
        this.accountVerificationService = accountVerificationService;
    }

    /**
     * It updates the password of given customer.
     *
     * @param customerId the id of the customer to be updated
     * @param password   the new password to be set
     * @throws CustomerNotFoundException                   if the customer does not exist
     * @throws AccountInvalidPasswordConfirmationException if the password does not match
     */
    public void updatePassword(Long customerId, String password) {
        log.debug("Updating password for customerId: {}", customerId);
        // we get the CustomerAuth entity so we can save.
        Account customerAccount = accountRepository.findByCustomer_Id(customerId).orElseThrow(
                () -> {
                    log.error("Failed to update password. No customer found with id: {}", customerId);
                    return new CustomerNotFoundException(
                            Exceptions.CUSTOMER.NOT_FOUND, customerId
                    );
                }
        );

        // set the new password
        customerAccount.setPassword(
                bCryptPasswordEncoder.encode(password)
        );

        // we change the updateAt timestamp field
        customerAccount.setUpdatedAt(Instant.now());

        // save the changes
        accountRepository.save(customerAccount);
        log.debug("Successfully updated password for customerId: {}", customerId);
    }

    /**
     * It updates the password of the current customer
     *
     * @param request the request body that contains the current password and the new password
     * @throws CustomerNotFoundException                   if the customer does not exist
     * @throws AccountInvalidPasswordConfirmationException if the password does not match
     */
    public void updatePassword(AccountPasswordUpdateRequest request) {
        // we extract the email from the Customer stored in the SecurityContext
        final Customer currentCustomer = AuthHelper.getLoggedCustomer();

        // Before making any changes we check that the password sent by the customer matches the one in the entity
        AuthHelper.validatePassword(currentCustomer, request.currentPassword());

        // update the password
        this.updatePassword(currentCustomer.getId(), request.newPassword());
    }

    /**
     * It resets the customer password using a token.
     *
     * @param token   the token used to reset the password
     * @param request the request with the password to set
     */
    public void passwordResetWithToken(String token, AccountPasswordResetSetRequest request) {
        log.debug("Resetting password using token: {}", token);
        // verify the token
        final AccountToken accountToken = accountVerificationService.validateToken(token);

        // update the password
        this.updatePassword(accountToken.getCustomer().getId(), request.password());

        // set the token as used
        accountToken.setUsed(true);
        accountTokenRepository.save(accountToken);

        // send the email notifying the customer that his password is successfully changed
        this.sendResetPasswordSuccessEmail(accountToken.getCustomer().getEmail());
        log.debug("Resetting password successfully done.");
    }

    /**
     * Generate a token for password reset
     *
     * @param request the request containing the email of the customer and password
     * @return AccountToken with the token
     */
    public AccountToken generatePasswordResetToken(AccountPasswordResetRequest request) {
        log.debug("Generating password reset token for email: {}", request.email());
        Account account = accountRepository
                .findByCustomer_Email(request.email())
                .orElseThrow(
                        () -> {
                            log.error(
                                    "Failed to generate password reset token. No account found for: {}",
                                    request.email()
                            );
                            return new AccountNotFoundException(Exceptions.ACCOUNT.NOT_FOUND);
                        }
                );

        // generate the token for password reset
        AccountToken token = new AccountToken(account.getOwner());
        token.setToken(token.generateToken());
        token.setType(AccountTokenType.RESET_PASSWORD);

        log.debug("Password reset token generated successfully for email: {}", request.email());
        return accountTokenRepository.save(token);
    }

    /**
     * Send email to the customer with a link to reset password.
     *
     * @param toEmail the customer's email address to send the email
     * @param token   the token to be included in the email
     */
    public void sendResetPasswordEmail(String toEmail, String token) {
        log.debug("Sending reset password email to: {} with token: {}", toEmail, token);
        String host = env.getProperty("app.frontend.host");
        String port = env.getProperty("app.frontend.port");
        String url = String.format("http://%s:%s", host, port);
        String link = url + "/accounts/reset-password/" + token;
        emailSenderService.send(
                toEmail,
                "Photogram password reset.",
                "Reset your password following this url: " + link
        );
    }

    /**
     * Send an email after successfully reset of the password
     *
     * @param toEmail the customer's email address to send the email
     */
    public void sendResetPasswordSuccessEmail(String toEmail) {
        log.debug("Sending email to: {}", toEmail);
        emailSenderService.send(
                toEmail,
                "Photogram password reset.",
                "Your password has been reset successfully."
        );
    }
}
