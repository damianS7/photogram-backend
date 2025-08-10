package com.damian.photogram.customers;

import com.damian.photogram.common.exception.Exceptions;
import com.damian.photogram.common.exception.PasswordMismatchException;
import com.damian.photogram.common.utils.AuthHelper;
import com.damian.photogram.customers.exception.CustomerEmailTakenException;
import com.damian.photogram.customers.exception.CustomerException;
import com.damian.photogram.customers.exception.CustomerNotFoundException;
import com.damian.photogram.customers.http.request.CustomerEmailUpdateRequest;
import com.damian.photogram.customers.http.request.CustomerRegistrationRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public CustomerService(
            CustomerRepository customerRepository,
            BCryptPasswordEncoder bCryptPasswordEncoder
    ) {
        this.customerRepository = customerRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    // get all the non friends for the logged customers filtered by name.
    public Set<Customer> searchCustomers(String name) {
        return customerRepository.findTop10ByName(name);
    }

    /**
     * Creates a new customers
     *
     * @param request contains the fields needed for the customers creation
     * @return the customers created
     * @throws CustomerException if another user has the email
     */
    public Customer createCustomer(CustomerRegistrationRequest request) {

        // check if the email is already taken
        if (emailExist(request.email())) {
            throw new CustomerEmailTakenException(
                    Exceptions.CUSTOMER.EMAIL_TAKEN
            );
        }

        // we create the customers and assign the data
        Customer customer = new Customer();
        customer.setEmail(request.email());
        customer.setPassword(bCryptPasswordEncoder.encode(request.password()));
        customer.getProfile().setFirstName(request.firstName());
        customer.getProfile().setLastName(request.lastName());
        customer.getProfile().setPhone(request.phone());
        customer.getProfile().setGender(request.gender());
        customer.getProfile().setBirthdate(request.birthdate());

        return customerRepository.save(customer);
    }

    /**
     * Deletes a customers
     *
     * @param customerId the id of the customers to be deleted
     * @return true if the customers was deleted
     * @throws CustomerException if the customers does not exist or if the logged user is not ADMIN
     */
    public boolean deleteCustomer(Long customerId) {
        // if the customers does not exist we throw an exception
        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotFoundException(
                    Exceptions.CUSTOMER.NOT_FOUND
            );
        }

        // we delete the customers
        customerRepository.deleteById(customerId);

        // if no exception is thrown we return true
        return true;
    }

    /**
     * Returns all the customers
     *
     * @return a list of CustomerDTO
     * @throws CustomerException if the logged user is not ADMIN
     */
    public Page<Customer> getCustomers(Pageable pageable) {
        // we return all the customers
        return customerRepository.findAll(pageable);
    }

    /**
     * Returns a customers
     *
     * @param customerId the id of the customers to be returned
     * @return the customers
     * @throws CustomerException if the customers does not exist or if the logged user is not ADMIN
     */
    public Customer getCustomer(Long customerId) {
        // if the customers does not exist we throw an exception
        return customerRepository.findById(customerId).orElseThrow(
                () -> new CustomerNotFoundException(
                        Exceptions.CUSTOMER.NOT_FOUND
                )
        );
    }

    // returns the logged customers
    public Customer getCustomer() {
        Customer loggedCustomer = AuthHelper.getLoggedCustomer();
        return this.getCustomer(loggedCustomer.getId());
    }

    /**
     * It checks if an email exist in the database
     *
     * @param email the email to be checked
     * @return true if the email exists, false otherwise
     */
    private boolean emailExist(String email) {
        // we search the email in the database
        return customerRepository.findByEmail(email).isPresent();
    }

    /**
     * It updates the email of a customers
     *
     * @param customerId the id of the customers
     * @param email      the new email to set
     * @return the customers updated
     * @throws CustomerException if the password does not match, or if the customers does not exist
     */
    public Customer updateEmail(Long customerId, String email) {
        // we get the Customer entity so we can save at the end
        Customer customer = customerRepository.findById(customerId).orElseThrow(
                () -> new CustomerNotFoundException(
                        Exceptions.CUSTOMER.NOT_FOUND
                )
        );

        // set the new email
        customer.setEmail(email);

        // we change the updateAt timestamp field
        customer.setUpdatedAt(Instant.now());

        // save the changes
        return customerRepository.save(customer);
    }

    /**
     * It updates the email from the logged customers
     *
     * @param request that contains the current password and the new email.
     * @return the customers updated
     * @throws PasswordMismatchException if the password does not match
     */
    public Customer updateEmail(CustomerEmailUpdateRequest request) {
        // we extract the email from the Customer stored in the SecurityContext
        final Customer loggedCustomer = AuthHelper.getLoggedCustomer();

        // Before making any changes we check that the password sent by the customers matches the one in the entity
        AuthHelper.validatePassword(loggedCustomer, request.currentPassword());

        return this.updateEmail(loggedCustomer.getId(), request.newEmail());
    }
}
