package com.damian.photogram.accounts.account;

import com.damian.photogram.common.exception.Exceptions;
import com.damian.photogram.common.exception.PasswordMismatchException;
import com.damian.photogram.common.utils.AuthHelper;
import com.damian.photogram.customers.Customer;
import com.damian.photogram.customers.exception.CustomerNotFoundException;
import com.damian.photogram.customers.http.request.CustomerPasswordUpdateRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AccountPasswordService {
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AccountRepository accountRepository;

    public AccountPasswordService(
            BCryptPasswordEncoder bCryptPasswordEncoder,
            AccountRepository accountRepository
    ) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.accountRepository = accountRepository;
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
