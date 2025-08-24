package com.damian.photogram.domain.account.controller;

import com.damian.photogram.core.utils.ApiResponse;
import com.damian.photogram.domain.account.dto.request.*;
import com.damian.photogram.domain.account.service.AccountActivationService;
import com.damian.photogram.domain.account.service.AccountPasswordService;
import com.damian.photogram.domain.account.service.AccountRegistrationService;
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
    private final AccountActivationService accountActivationService;

    public AccountController(
            AccountRegistrationService accountRegistrationService,
            AccountPasswordService accountPasswordService,
            AccountActivationService accountActivationService
    ) {
        this.accountRegistrationService = accountRegistrationService;
        this.accountPasswordService = accountPasswordService;
        this.accountActivationService = accountActivationService;
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

    // endpoint to modify customer password
    @PatchMapping("/accounts/password")
    public ResponseEntity<?> updateLoggedCustomerPassword(
            @Validated @RequestBody
            AccountPasswordUpdateRequest request
    ) {
        accountPasswordService.updatePassword(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body("Password updated");
    }

    // endpoint to activate an account
    @GetMapping("/accounts/activate/{token:.+}")
    public ResponseEntity<?> activate(
            @PathVariable @NotBlank
            String token
    ) {
        accountActivationService.activate(token);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Account has been activated."));
    }

    // endpoint for account to request resending activation
    @PostMapping("/accounts/resend-activation")
    public ResponseEntity<?> resendActivation(
            @Validated @RequestBody
            AccountActivationResendRequest request
    ) {
        accountActivationService.sendAccountActivationToken(request.email());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Activation email sent successfully. Please check your inbox."));
    }

    // endpoint to request for a reset password
    @PostMapping("/accounts/reset-password")
    public ResponseEntity<?> resetPasswordRequest(
            @Validated @RequestBody
            AccountPasswordResetRequest request
    ) {
        accountPasswordService.resetPassword(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Reset password email sent successfully. Please check your inbox."));
    }

    // endpoint to set a new password using token
    @PostMapping("/accounts/reset-password/{token:.+}")
    public ResponseEntity<?> resetPassword(
            @PathVariable @NotBlank
            String token,
            @Validated @RequestBody
            AccountPasswordResetSetRequest request
    ) {
        accountPasswordService.updatePassword(token, request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Password reset successfully."));
    }
}