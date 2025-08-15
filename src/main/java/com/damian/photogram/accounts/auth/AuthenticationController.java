package com.damian.photogram.accounts.auth;

import com.damian.photogram.accounts.auth.http.AuthenticationRequest;
import com.damian.photogram.accounts.auth.http.AuthenticationResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(
            AuthenticationService authenticationService
    ) {
        this.authenticationService = authenticationService;
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
}