package com.damian.photogram.auth;

import com.damian.photogram.auth.http.AuthenticationRequest;
import com.damian.photogram.auth.http.AuthenticationResponse;
import com.damian.photogram.customer.Customer;
import com.damian.photogram.customer.dto.CustomerDTOMapper;
import com.damian.photogram.customer.dto.CustomerWithProfileDTO;
import com.damian.photogram.customer.http.request.CustomerPasswordUpdateRequest;
import com.damian.photogram.customer.http.request.CustomerRegistrationRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    // endpoint for registration
    @PostMapping("/auth/register")
    public ResponseEntity<?> register(
            @Validated @RequestBody
            CustomerRegistrationRequest request
    ) {
        Customer registeredCustomer = authenticationService.register(request);
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

    // endpoint to modify customer password
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