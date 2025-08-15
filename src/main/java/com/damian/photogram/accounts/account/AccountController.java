package com.damian.photogram.accounts.account;

import com.damian.photogram.accounts.activation.AccountActivationService;
import com.damian.photogram.accounts.activation.http.ResendActivationRequest;
import com.damian.photogram.common.utils.ApiResponse;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class AccountController {

    private final AccountActivationService accountActivationService;

    public AccountController(
            AccountActivationService accountActivationService
    ) {
        this.accountActivationService = accountActivationService;
    }

    // endpoint for accounts activation
    @GetMapping("/auth/accounts/activate/{token:.+}")
    public ResponseEntity<?> activate(
            @PathVariable @NotBlank
            String token
    ) {
        accountActivationService.activate(token);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Account has been activated."));
    }

    // endpoint for accounts activation
    @PostMapping("/auth/accounts/resend-activation")
    public ResponseEntity<?> resendActivation(
            @Validated @RequestBody
            ResendActivationRequest request
    ) {
        accountActivationService.sendAccountActivationToken(request.email());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Activation email sent successfully. Please check your inbox."));
    }

}