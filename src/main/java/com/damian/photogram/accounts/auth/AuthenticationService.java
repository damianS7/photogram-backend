package com.damian.photogram.accounts.auth;

import com.damian.photogram.accounts.account.Account;
import com.damian.photogram.accounts.account.AccountRepository;
import com.damian.photogram.accounts.account.AccountStatus;
import com.damian.photogram.accounts.account.exception.AccountDisabledException;
import com.damian.photogram.accounts.auth.exception.AuthenticationBadCredentialsException;
import com.damian.photogram.accounts.auth.http.AuthenticationRequest;
import com.damian.photogram.accounts.auth.http.AuthenticationResponse;
import com.damian.photogram.common.exception.Exceptions;
import com.damian.photogram.common.exception.PasswordMismatchException;
import com.damian.photogram.common.utils.AuthHelper;
import com.damian.photogram.common.utils.JWTUtil;
import com.damian.photogram.customers.Customer;
import com.damian.photogram.customers.exception.CustomerNotFoundException;
import com.damian.photogram.customers.http.request.CustomerPasswordUpdateRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;

@Service
public class AuthenticationService {
    private final JWTUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AccountRepository accountRepository;

    public AuthenticationService(
            JWTUtil jwtUtil,
            AuthenticationManager authenticationManager,
            BCryptPasswordEncoder bCryptPasswordEncoder,
            AccountRepository accountRepository
    ) {
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.accountRepository = accountRepository;
    }

    /**
     * Controls the login flow.
     *
     * @param request Contains the fields needed to login into the service
     * @return Contains the data (Customer, Profile) and the token
     * @throws AuthenticationBadCredentialsException if credentials are invalid
     * @throws AccountDisabledException              if the accounts is not enabled
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
            throw new AuthenticationBadCredentialsException(
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

        // check if the accounts is disabled
        if (customer.getAccount().getAccountStatus().equals(AccountStatus.SUSPENDED)) {
            throw new AccountDisabledException(
                    Exceptions.ACCOUNT.SUSPENDED
            );
        }

        // check if the accounts is verified
        if (!customer.getAccount().isEmailVerified()) {
            throw new AccountDisabledException(
                    Exceptions.ACCOUNT.EMAIL_NOT_VERIFIED
            );
        }

        // Return the customers data and the token
        return new AuthenticationResponse(
                token
        );
    }

    /**
     * It updates the password of given followedCustomerId.
     *
     * @param customerId the id of the customers to be updated
     * @param password   the new password to be set
     * @throws CustomerNotFoundException if the customers does not exist
     * @throws PasswordMismatchException if the password does not match
     */
    public void updatePassword(Long customerId, String password) {

        // we get the CustomerAuth entity so we can save.
        Account customerAccount = accountRepository.findByCustomer_Id(customerId).orElseThrow(
                () -> new CustomerNotFoundException(
                        Exceptions.CUSTOMER.NOT_FOUND
                )
        );

        // set the new password
        customerAccount.setPassword(
                bCryptPasswordEncoder.encode(password)
        );

        // we change the updateAt timestamp field
        customerAccount.setUpdatedAt(Instant.now());

        // save the changes
        accountRepository.save(customerAccount);
    }

    /**
     * It updates the password of the logged customers
     *
     * @param request the request body that contains the current password and the new password
     * @throws CustomerNotFoundException if the customers does not exist
     * @throws PasswordMismatchException if the password does not match
     */
    public void updatePassword(CustomerPasswordUpdateRequest request) {
        // we extract the email from the Customer stored in the SecurityContext
        final Customer loggedCustomer = AuthHelper.getLoggedCustomer();

        // Before making any changes we check that the password sent by the customers matches the one in the entity
        AuthHelper.validatePassword(loggedCustomer, request.currentPassword());

        // update the password
        this.updatePassword(loggedCustomer.getId(), request.newPassword());
    }
}
