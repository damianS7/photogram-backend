package com.damian.photogram.service.user;

import com.damian.photogram.domain.user.model.AccountToken;
import com.damian.photogram.domain.user.model.Customer;
import com.damian.photogram.web.rest.user.dto.request.AccountRegistrationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AccountRegistrationService {
    private static final Logger log = LoggerFactory.getLogger(AccountRegistrationService.class);
    private final AccountVerificationService accountVerificationService;
    private final CustomerService customerService;

    public AccountRegistrationService(
            AccountVerificationService accountVerificationService,
            CustomerService customerService
    ) {
        this.accountVerificationService = accountVerificationService;
        this.customerService = customerService;
    }

    /**
     * Register a new customer.
     *
     * @param request Contains the fields needed for the customer creation
     * @return Customer The customer created
     */
    public Customer register(AccountRegistrationRequest request) {
        log.debug("Registering a new customer");
        // It uses the customer service to create a new customer
        Customer registeredCustomer = customerService.createCustomer(request);

        // Create a token for the account activation
        AccountToken accountToken = accountVerificationService.generateVerificationToken(request.email());

        // send the account activation link
        accountVerificationService.sendAccountVerificationLinkEmail(request.email(), accountToken.getToken());

        log.debug("Customer: {} with email:{} registered", registeredCustomer.getId(), registeredCustomer.getEmail());
        return registeredCustomer;
    }
}
