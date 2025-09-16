package com.damian.photogram.web.auth;

import com.damian.photogram.web.auth.dto.AuthenticationRequest;
import com.damian.photogram.web.auth.dto.AuthenticationResponse;
import com.damian.photogram.service.auth.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1")
public class AuthenticationController {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationController.class);
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
        log.debug("Received login request for: {}", request.email());
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
        log.debug("Received token validation request.");
        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }
}