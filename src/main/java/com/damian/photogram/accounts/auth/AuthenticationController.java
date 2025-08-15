package com.damian.photogram.accounts.auth;

import com.damian.photogram.accounts.AccountRegistrationService;
import com.damian.photogram.accounts.auth.http.AuthenticationRequest;
import com.damian.photogram.accounts.auth.http.AuthenticationResponse;
import com.damian.photogram.customers.Customer;
import com.damian.photogram.customers.dto.CustomerDTOMapper;
import com.damian.photogram.customers.dto.CustomerWithProfileDTO;
import com.damian.photogram.customers.http.request.CustomerPasswordUpdateRequest;
import com.damian.photogram.customers.http.request.CustomerRegistrationRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1")
public class AuthenticationController {

    private final AccountRegistrationService accountRegistrationService;
    private final AuthenticationService authenticationService;

    public AuthenticationController(
            AccountRegistrationService accountRegistrationService,
            AuthenticationService authenticationService
    ) {
        this.accountRegistrationService = accountRegistrationService;
        this.authenticationService = authenticationService;
    }

    // endpoint for registration
    @PostMapping("/auth/register")
    public ResponseEntity<?> register(
            @Validated @RequestBody
            CustomerRegistrationRequest request
    ) {
        Customer registeredCustomer = accountRegistrationService.register(request);
        CustomerWithProfileDTO dto = CustomerDTOMapper.toCustomerWithProfileDTO(registeredCustomer);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(dto);
    }

    // endpoint for login
    @PostMapping("/auth/login")
    public ResponseEntity<?> login(
            @Validated @RequestBody
            AuthenticationRequest request
    ) {
        AuthenticationResponse authResponse = authenticationService.login(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .header(HttpHeaders.AUTHORIZATION, authResponse.token())
                .body(authResponse);
    }

    // endpoint for token validation
    @GetMapping("/auth/token/validate")
    public ResponseEntity<?> tokenValidation(
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }

    // endpoint to modify customers password
    @PatchMapping("/auth/customers/me/password")
    public ResponseEntity<?> updateLoggedCustomerPassword(
            @Validated @RequestBody
            CustomerPasswordUpdateRequest request
    ) {
        authenticationService.updatePassword(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body("Password updated");
    }
}