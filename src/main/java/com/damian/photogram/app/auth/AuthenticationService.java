package com.damian.photogram.app.auth;

import com.damian.photogram.app.auth.dto.AuthenticationRequest;
import com.damian.photogram.app.auth.dto.AuthenticationResponse;
import com.damian.photogram.core.common.JwtUtil;
import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.core.security.user.User;
import com.damian.photogram.domain.account.enums.AccountStatus;
import com.damian.photogram.domain.account.exception.AccountNotVerifiedException;
import com.damian.photogram.domain.account.exception.AccountSuspendedException;
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

        // Authenticate the user
        auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email, password)
        );

        // Get the authenticated user
        final User currentUser = ((User) auth.getPrincipal());
        final HashMap<String, Object> claims = new HashMap<>();
        claims.put("email", currentUser.getEmail());
        claims.put("role", currentUser.getRole());

        // Generate a token for the authenticated user
        final String token = jwtUtil.generateToken(
                claims,
                email
        );

        // check if the account is disabled
        if (currentUser.getAccount().getAccountStatus().equals(AccountStatus.SUSPENDED)) {
            throw new AccountSuspendedException(
                    Exceptions.ACCOUNT.SUSPENDED
            );
        }

        // check if the account is verified
        if (currentUser.getAccount().getAccountStatus().equals(AccountStatus.PENDING_VERIFICATION)) {
            throw new AccountNotVerifiedException(
                    Exceptions.ACCOUNT.NOT_VERIFIED
            );
        }

        // Return the customer data and the token
        return new AuthenticationResponse(
                token
        );
    }
}
