package com.damian.photogram.app.auth;

import com.damian.photogram.app.auth.dto.AuthenticationRequest;
import com.damian.photogram.app.auth.dto.AuthenticationResponse;
import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.core.utils.JwtUtil;
import com.damian.photogram.domain.account.enums.AccountStatus;
import com.damian.photogram.domain.account.exception.AccountNotVerifiedException;
import com.damian.photogram.domain.account.exception.AccountSuspendedException;
import com.damian.photogram.domain.customer.model.Customer;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class AuthenticationService {
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(
            JwtUtil jwtUtil,
            AuthenticationManager authenticationManager
    ) {
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Controls the login flow.
     *
     * @param request Contains the fields needed to login into the service
     * @return Contains the data (Customer, Profile) and the token
     * @throws BadCredentialsException     if credentials are invalid
     * @throws AccountNotVerifiedException if the account is not verified
     */
    public AuthenticationResponse login(AuthenticationRequest request) {
        final String email = request.email();
        final String password = request.password();
        final Authentication auth;

        try {
            // Authenticate the user
            auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            email, password)
            );
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException(
                    Exceptions.ACCOUNT.BAD_CREDENTIALS
            );
        }

        // Get the authenticated user
        final Customer customer = (Customer) auth.getPrincipal();

        HashMap<String, Object> claims = new HashMap<>();
        claims.put("email", customer.getEmail());
        claims.put("role", customer.getRole());

        // Generate a token for the authenticated user
        final String token = jwtUtil.generateToken(
                claims,
                email
        );

        // check if the account is disabled
        if (customer.getAccount().getAccountStatus().equals(AccountStatus.SUSPENDED)) {
            throw new AccountSuspendedException(
                    Exceptions.ACCOUNT.SUSPENDED
            );
        }

        // check if the account is verified
        if (customer.getAccount().getAccountStatus().equals(AccountStatus.PENDING_VERIFICATION)) {
            throw new AccountNotVerifiedException(
                    Exceptions.ACCOUNT.EMAIL_NOT_VERIFIED
            );
        }

        // Return the customer data and the token
        return new AuthenticationResponse(
                token
        );
    }
}
