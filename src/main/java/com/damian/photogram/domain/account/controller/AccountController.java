package com.damian.photogram.domain.account.controller;

import com.damian.photogram.core.utils.ApiResponse;
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

    // endpoint for account verification
    @GetMapping("/accounts/verification/{token:.+}")
    public ResponseEntity<?> verification(
            @PathVariable @NotBlank
            String token
    ) {

        // verification the account using the provided token
        Account account = accountVerificationService.verifyAccount(token);

        // send email to customer after account has been verificated
        accountVerificationService.sendAccountVerifiedEmail(account.getOwner());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Your account has been verified. You can now log in with your credentials."));
    }

    // endpoint for account to request for account verification email
    @PostMapping("/accounts/resend-verification")
    public ResponseEntity<?> resendVerification(
            @Validated @RequestBody
            AccountActivationResendRequest request
    ) {
        // generate a new verification token
        AccountToken accountToken = accountVerificationService.generateVerificationToken(request.email());

        // send the account verification link
        accountVerificationService.sendAccountVerificationLinkEmail(request.email(), accountToken.getToken());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("A verification link has been sent to your email."));
    }

    // endpoint to request for a reset password
    @PostMapping("/accounts/reset-password")
    public ResponseEntity<?> resetPasswordRequest(
            @Validated @RequestBody
            AccountPasswordResetRequest request
    ) {
        // generate a new password reset token
        AccountToken accountToken = accountPasswordService.generatePasswordResetToken(request);

        // send the email with the link to reset the password
        accountPasswordService.sendResetPasswordEmail(
                accountToken.getCustomer().getEmail(),
                accountToken.getToken()
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("A password reset link has been sent to your email address."));
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
        accountPasswordService.passwordResetWithToken(token, request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Password reset successfully."));
    }
}