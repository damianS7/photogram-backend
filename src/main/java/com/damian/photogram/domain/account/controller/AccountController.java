package com.damian.photogram.domain.account.controller;

import com.damian.photogram.domain.account.dto.request.*;
import com.damian.photogram.domain.account.model.Account;
import com.damian.photogram.domain.account.model.AccountToken;
import com.damian.photogram.domain.account.service.AccountPasswordService;
import com.damian.photogram.domain.account.service.AccountRegistrationService;
import com.damian.photogram.domain.account.service.AccountVerificationService;
import com.damian.photogram.domain.customer.dto.response.CustomerWithProfileDto;
import com.damian.photogram.domain.customer.mapper.CustomerDtoMapper;
import com.damian.photogram.domain.customer.model.Customer;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class AccountController {

    private final AccountRegistrationService accountRegistrationService;
    private final AccountPasswordService accountPasswordService;
    private final AccountVerificationService accountVerificationService;

    public AccountController(
            AccountRegistrationService accountRegistrationService,
            AccountPasswordService accountPasswordService,
            AccountVerificationService accountVerificationService
    ) {
        this.accountRegistrationService = accountRegistrationService;
        this.accountPasswordService = accountPasswordService;
        this.accountVerificationService = accountVerificationService;
    }

    // endpoint for account registration
    @PostMapping("/accounts/register")
    public ResponseEntity<?> register(
            @Validated @RequestBody
            AccountRegistrationRequest request
    ) {
        Customer registeredCustomer = accountRegistrationService.register(request);

        CustomerWithProfileDto dto = CustomerDtoMapper.toCustomerWithProfileDto(registeredCustomer);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(dto);
    }

    // endpoint to modify current customer password
    @PatchMapping("/accounts/password")
    public ResponseEntity<?> updatePassword(
            @Validated @RequestBody
            AccountPasswordUpdateRequest request
    ) {
        accountPasswordService.updatePassword(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }

    // endpoint to activate an account
    @GetMapping("/accounts/activate/{token:.+}")
    public ResponseEntity<?> activate(
            @PathVariable @NotBlank
            String token
    ) {

        // activate the account using the provided token
        Account account = accountVerificationService.verifyAccount(token);

        // send email to customer after account has been activated
        accountVerificationService.sendAccountVerifiedEmail(account.getOwner());

        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }

    // endpoint for account to request for account activation email
    @PostMapping("/accounts/resend-activation")
    public ResponseEntity<?> resendActivation(
            @Validated @RequestBody
            AccountActivationResendRequest request
    ) {
        // generate a new activation token
        AccountToken accountToken = accountVerificationService.generateVerificationToken(request.email());

        // send the account activation link
        accountVerificationService.sendAccountVerificationLinkEmail(request.email(), accountToken.getToken());

        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }

    // endpoint to request for a reset password
    @PostMapping("/accounts/reset-password")
    public ResponseEntity<?> resetPasswordRequest(
            @Validated @RequestBody
            AccountPasswordResetRequest request
    ) {
        // generate a new password reset token
        AccountToken accountToken = accountPasswordService.createPasswordResetToken(request);

        // send the email with the link to reset the password
        accountPasswordService.sendResetPasswordEmail(
                accountToken.getCustomer().getEmail(),
                accountToken.getToken()
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }

    // endpoint to set a new password using token
    @PostMapping("/accounts/reset-password/{token:.+}")
    public ResponseEntity<?> resetPassword(
            @PathVariable @NotBlank
            String token,
            @Validated @RequestBody
            AccountPasswordResetSetRequest request
    ) {
        // update the password using the token
        accountPasswordService.updatePassword(token, request);

        // send the email notifying password is successfully changed
        accountPasswordService.sendResetPasswordSuccessEmail(request.email());

        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }
}