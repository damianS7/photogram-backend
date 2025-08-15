package com.damian.photogram.accounts.account;

import com.damian.photogram.customers.Customer;
import com.damian.photogram.customers.CustomerService;
import com.damian.photogram.customers.http.request.CustomerRegistrationRequest;
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
     * Register a new customers.
     *
     * @param request Contains the fields needed for the customers creation
     * @return The customers created
     */
    public Customer register(CustomerRegistrationRequest request) {
        // It uses the customers service to create a new customers
        Customer registeredCustomer = customerService.createCustomer(request);

        // Create a new authentication token for the customer
        accountActivationService.sendAccountActivationToken(registeredCustomer.getEmail());

        return registeredCustomer;
    }
}
