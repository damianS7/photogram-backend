package com.damian.photogram.domain.account.service;

import com.damian.photogram.domain.account.dto.request.AccountRegistrationRequest;
import com.damian.photogram.domain.account.model.AccountToken;
import com.damian.photogram.domain.customer.model.Customer;
import com.damian.photogram.domain.customer.service.CustomerService;
import org.springframework.stereotype.Service;

@Service
public class AccountRegistrationService {
    private final AccountActivationService accountActivationService;
    private final CustomerService customerService;

    public AccountRegistrationService(
            AccountActivationService accountActivationService,
            CustomerService customerService
    ) {
        this.accountActivationService = accountActivationService;
        this.customerService = customerService;
    }

    /**
     * Register a new customer.
     *
     * @param request Contains the fields needed for the customer creation
     * @return Customer The customer created
     */
    public Customer register(AccountRegistrationRequest request) {
        // It uses the customer service to create a new customer
        Customer registeredCustomer = customerService.createCustomer(request);

        // Create a token for the account activation
        AccountToken accountToken = accountActivationService.createAccountActivationToken(request.email());

        // send the account activation link
        accountActivationService.sendAccountActivationEmail(request.email(), accountToken.getToken());

        return registeredCustomer;
    }
}
