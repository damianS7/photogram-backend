package com.damian.photogram.domain.account.service;

import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.core.exception.PasswordMismatchException;
import com.damian.photogram.core.service.EmailSenderService;
import com.damian.photogram.core.utils.AuthHelper;
import com.damian.photogram.domain.account.dto.request.AccountPasswordResetRequest;
import com.damian.photogram.domain.account.dto.request.AccountPasswordResetSetRequest;
import com.damian.photogram.domain.account.dto.request.AccountPasswordUpdateRequest;
import com.damian.photogram.domain.account.enums.AccountTokenType;
import com.damian.photogram.domain.account.exception.AccountNotFoundException;
import com.damian.photogram.domain.account.model.Account;
import com.damian.photogram.domain.account.model.AccountToken;
import com.damian.photogram.domain.account.repository.AccountRepository;
import com.damian.photogram.domain.account.repository.AccountTokenRepository;
import com.damian.photogram.domain.customer.exception.CustomerNotFoundException;
import com.damian.photogram.domain.customer.model.Customer;
import com.damian.photogram.domain.customer.repository.CustomerRepository;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AccountPasswordService {
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AccountRepository accountRepository;
    private final EmailSenderService emailSenderService;
    private final CustomerRepository customerRepository;
    private final AccountTokenRepository accountTokenRepository;
    private final Environment env;
    private final AccountVerificationService accountVerificationService;

    public AccountPasswordService(
            BCryptPasswordEncoder bCryptPasswordEncoder,
            AccountRepository accountRepository,
            EmailSenderService emailSenderService,
            CustomerRepository customerRepository,
            AccountTokenRepository accountTokenRepository,
            Environment env,
            AccountVerificationService accountVerificationService
    ) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.accountRepository = accountRepository;
        this.emailSenderService = emailSenderService;
        this.customerRepository = customerRepository;
        this.accountTokenRepository = accountTokenRepository;
        this.env = env;
        this.accountVerificationService = accountVerificationService;
    }

    /**
     * It updates the password of given customer.
     *
     * @param customerId the id of the customer to be updated
     * @param password   the new password to be set
     * @throws CustomerNotFoundException if the customer does not exist
     * @throws PasswordMismatchException if the password does not match
     */
    public void updatePassword(Long customerId, String password) {

        // we get the CustomerAuth entity so we can save.
        Account customerAccount = accountRepository.findByCustomer_Id(customerId).orElseThrow(
                () -> new CustomerNotFoundException(
                        Exceptions.CUSTOMER.NOT_FOUND
                )
        );

        // set the new password
        customerAccount.setPassword(
                bCryptPasswordEncoder.encode(password)
        );

        // we change the updateAt timestamp field
        customerAccount.setUpdatedAt(Instant.now());

        // save the changes
        accountRepository.save(customerAccount);
    }

    /**
     * It updates the password of the current customer
     *
     * @param request the request body that contains the current password and the new password
     * @throws CustomerNotFoundException if the customer does not exist
     * @throws PasswordMismatchException if the password does not match
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
        // verify the token
        final AccountToken accountToken = accountVerificationService.validateToken(token);

        // update the password
        this.updatePassword(accountToken.getCustomer().getId(), request.password());

        // set the token as used
        accountToken.setUsed(true);
        accountTokenRepository.save(accountToken);

        // send the email notifying the customer that his password is successfully changed
        this.sendResetPasswordSuccessEmail(accountToken.getCustomer().getEmail());
    }

    /**
     * Generate a token for password reset
     *
     * @param request the request containing the email of the customer and password
     * @return AccountToken with the token
     */
    public AccountToken generatePasswordResetToken(AccountPasswordResetRequest request) {
        Account account = accountRepository
                .findByCustomer_Email(request.email())
                .orElseThrow(
                        () -> new AccountNotFoundException(Exceptions.ACCOUNT.NOT_FOUND)
                );

        // generate the token for password reset
        AccountToken token = new AccountToken(account.getOwner());
        token.setToken(token.generateToken());
        token.setType(AccountTokenType.RESET_PASSWORD);
        return accountTokenRepository.save(token);
    }

    /**
     * Send email to the customer with a link to reset password.
     *
     * @param toEmail the customer's email address to send the email
     * @param token   the token to be included in the email
     */
    public void sendResetPasswordEmail(String toEmail, String token) {
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
        emailSenderService.send(
                toEmail,
                "Photogram password reset.",
                "Your password has been reset successfully."
        );
    }
}
