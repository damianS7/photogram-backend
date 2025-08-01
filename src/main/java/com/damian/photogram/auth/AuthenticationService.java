package com.damian.photogram.auth;

import com.damian.photogram.auth.exception.AccountDisabledException;
import com.damian.photogram.auth.exception.AuthenticationBadCredentialsException;
import com.damian.photogram.auth.http.AuthenticationRequest;
import com.damian.photogram.auth.http.AuthenticationResponse;
import com.damian.photogram.common.exception.Exceptions;
import com.damian.photogram.common.exception.PasswordMismatchException;
import com.damian.photogram.common.utils.AuthHelper;
import com.damian.photogram.common.utils.JWTUtil;
import com.damian.photogram.customer.Customer;
import com.damian.photogram.customer.CustomerService;
import com.damian.photogram.customer.exception.CustomerNotFoundException;
import com.damian.photogram.customer.http.request.CustomerPasswordUpdateRequest;
import com.damian.photogram.customer.http.request.CustomerRegistrationRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuthenticationService {
    private final JWTUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final CustomerService customerService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AuthenticationRepository authenticationRepository;

    public AuthenticationService(
            JWTUtil jwtUtil,
            AuthenticationManager authenticationManager,
            CustomerService customerService,
            BCryptPasswordEncoder bCryptPasswordEncoder,
            AuthenticationRepository authenticationRepository
    ) {
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.customerService = customerService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.authenticationRepository = authenticationRepository;
    }

    /**
     * Register a new customer.
     *
     * @param request Contains the fields needed for the customer creation
     * @return The customer created
     */
    public Customer register(CustomerRegistrationRequest request) {
        // It uses the customer service to create a new customer
        // Customer registeredCustomer = customerService.createCustomer(request);

        // send welcome email
        // Generate token for email validation
        // send email to confirm registration

        return customerService.createCustomer(request);
    }

    /**
     * Controls the login flow.
     *
     * @param request Contains the fields needed to login into the service
     * @return Contains the data (Customer, Profile) and the token
     * @throws AuthenticationBadCredentialsException if credentials are invalid
     * @throws AccountDisabledException              if the account is not enabled
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
                    Exceptions.AUTH.BAD_CREDENTIALS
            );
        }

        // Generate a token for the authenticated user
        final String token = jwtUtil.generateToken(email);

        // Get the authenticated user
        final Customer customer = (Customer) auth.getPrincipal();

        // check if the account is disabled
        if (customer.getAuth().getAuthAccountStatus().equals(AuthAccountStatus.DISABLED)) {
            throw new AccountDisabledException(
                    Exceptions.CUSTOMER.DISABLED
            );
        }

        // Return the customer data and the token
        return new AuthenticationResponse(
                token
        );
    }

    /**
     * It updates the password of given customerId.
     *
     * @param customerId the id of the customer to be updated
     * @param password   the new password to be set
     * @throws CustomerNotFoundException if the customer does not exist
     * @throws PasswordMismatchException if the password does not match
     */
    public void updatePassword(Long customerId, String password) {

        // we get the CustomerAuth entity so we can save.
        Auth customerAuth = authenticationRepository.findByCustomer_Id(customerId).orElseThrow(
                () -> new CustomerNotFoundException(
                        Exceptions.CUSTOMER.NOT_FOUND
                )
        );

        // set the new password
        customerAuth.setPassword(
                bCryptPasswordEncoder.encode(password)
        );

        // we change the updateAt timestamp field
        customerAuth.setUpdatedAt(Instant.now());

        // save the changes
        authenticationRepository.save(customerAuth);
    }

    /**
     * It updates the password of the logged customer
     *
     * @param request the request body that contains the current password and the new password
     * @throws CustomerNotFoundException if the customer does not exist
     * @throws PasswordMismatchException if the password does not match
     */
    public void updatePassword(CustomerPasswordUpdateRequest request) {
        // we extract the email from the Customer stored in the SecurityContext
        final Customer loggedCustomer = AuthHelper.getLoggedCustomer();

        // Before making any changes we check that the password sent by the customer matches the one in the entity
        AuthHelper.validatePassword(loggedCustomer, request.currentPassword());

        // update the password
        this.updatePassword(loggedCustomer.getId(), request.newPassword());
    }
}
