package com.damian.photogram.service.user.customer;

import com.damian.photogram.core.AbstractServiceTest;
import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.domain.user.enums.CustomerGender;
import com.damian.photogram.domain.user.exception.AccountInvalidPasswordConfirmationException;
import com.damian.photogram.domain.user.exception.CustomerEmailTakenException;
import com.damian.photogram.domain.user.exception.CustomerNotFoundException;
import com.damian.photogram.domain.user.model.Customer;
import com.damian.photogram.domain.user.repository.CustomerRepository;
import com.damian.photogram.service.user.CustomerService;
import com.damian.photogram.web.rest.user.dto.request.AccountRegistrationRequest;
import com.damian.photogram.web.rest.user.dto.request.CustomerEmailUpdateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CustomerServiceTest extends AbstractServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @InjectMocks
    private CustomerService customerService;

    @Test
    @DisplayName("Should get all customer")
    void shouldGetAllCustomers() {
        // given
        List<Customer> customerList = List.of(
                new Customer(1L, "customer1@test.com", "password1"),
                new Customer(2L, "customer2@test.com", "password2")
        );
        Pageable pageable = PageRequest.of(0, 10);
        Page<Customer> customerPage = new PageImpl<>(customerList, pageable, customerList.size());

        // when
        when(customerRepository.findAll(pageable)).thenReturn(customerPage);
        Page<Customer> result = customerService.getCustomers(pageable);

        // then
        assertNotNull(result);
        assertEquals(customerList.size(), result.getTotalElements());
        verify(customerRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Should get customer")
    void shouldGetCustomer() {
        // given
        Customer customer = Customer.create()
                                    .setId(1L)
                                    .setEmail("customer@test.com")
                                    .setPassword("1234");

        // when
        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        Customer storedCustomer = customerService.getCustomer(customer.getId());

        // then
        assertThat(storedCustomer)
                .isNotNull()
                .extracting(
                        Customer::getId,
                        Customer::getEmail
                ).containsExactly(
                        storedCustomer.getId(),
                        storedCustomer.getEmail()
                );
        verify(customerRepository, times(1)).findById(customer.getId());
    }

    @Test
    @DisplayName("Should not get customer when not exist")
    void shouldNotGetCustomerWhenNotExist() {
        // given
        Long id = -1L;

        // when
        CustomerNotFoundException exception = assertThrows(
                CustomerNotFoundException.class,
                () -> customerService.getCustomer(id)
        );

        // then
        assertEquals(Exceptions.CUSTOMER.NOT_FOUND, exception.getMessage());
    }

    @Test
    @DisplayName("Should create customer")
    void shouldCreateCustomer() {
        // given
        final String passwordHash = "$5554ml;f;lsd";
        AccountRegistrationRequest request = new AccountRegistrationRequest(
                "david@gmail.com",
                "123456",
                "david",
                "david",
                "white",
                "123 123 123",
                LocalDate.of(1989, 1, 1),
                CustomerGender.MALE
        );

        // when
        when(bCryptPasswordEncoder.encode(request.password())).thenReturn(passwordHash);
        when(customerRepository.existsByEmail(request.email())).thenReturn(false);
        customerService.createCustomer(request);

        // then
        ArgumentCaptor<Customer> customerArgumentCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(customerArgumentCaptor.capture());

        Customer storedCustomer = customerArgumentCaptor.getValue();
        verify(customerRepository, times(1)).save(storedCustomer);

        assertThat(storedCustomer)
                .isNotNull()
                .extracting(
                        Customer::getId,
                        Customer::getEmail
                ).containsExactly(
                        storedCustomer.getId(),
                        storedCustomer.getEmail()
                );
    }

    @Test
    @DisplayName("Should not create any customer when email is taken")
    void shouldNotCreateCustomerWhenEmailIsTaken() {
        // given
        AccountRegistrationRequest request = new AccountRegistrationRequest(
                "david@gmail.com",
                "123456",
                "david",
                "david",
                "white",
                "123 123 123",
                LocalDate.of(1989, 1, 1),
                CustomerGender.MALE
        );

        // when
        when(customerRepository.existsByEmail(request.email())).thenReturn(true);
        CustomerEmailTakenException exception = assertThrows(
                CustomerEmailTakenException.class,
                () -> customerService.createCustomer(request)
        );

        // then
        verify(customerRepository, times(0)).save(any());
        assertEquals(Exceptions.CUSTOMER.EMAIL_TAKEN, exception.getMessage());
    }

    @Test
    @DisplayName("Should delete customer")
    void shouldDeleteCustomer() {
        // given
        Long id = 7L;
        when(customerRepository.existsById(id)).thenReturn(true);

        // when
        customerService.deleteCustomer(id);

        // then
        verify(customerRepository, times(1)).deleteById(id);
        verify(customerRepository).deleteById(id);
    }

    @Test
    @DisplayName("Should not delete customer when not exist")
    void shouldNotDeleteCustomerWhenNotExist() {
        // given
        Long id = -1L;

        // when
        when(customerRepository.existsById(id)).thenReturn(false);

        // then
        CustomerNotFoundException exception = assertThrows(
                CustomerNotFoundException.class,
                () -> customerService.deleteCustomer(id)
        );
        assertTrue(exception.getMessage().contains("Customer not found"));
        verify(customerRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should update customer email")
    void shouldUpdateCustomerEmail() {
        // given
        Customer customer = Customer.create()
                                    .setId(10L)
                                    .setEmail("customer@demo.com")
                                    .setPassword(passwordEncoder.encode(RAW_PASSWORD));

        // set the customer on the context
        setUpContext(customer);

        CustomerEmailUpdateRequest updateRequest = new CustomerEmailUpdateRequest(
                RAW_PASSWORD,
                "david@test.com"
        );

        // when
        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        customerService.updateEmail(updateRequest);

        // then
        assertThat(customer)
                .isNotNull()
                .extracting(
                        Customer::getId,
                        Customer::getEmail
                ).containsExactly(
                        customer.getId(),
                        updateRequest.newEmail()
                );

        verify(customerRepository, times(1)).save(customer);
    }

    @Test
    @DisplayName("Should not update customer email when is already taken")
    void shouldNotUpdateCustomerEmailWhenIsAlreadyTaken() {
        // given
        Customer customer = Customer.create()
                                    .setId(2L)
                                    .setEmail("customer@demo.com")
                                    .setPassword(passwordEncoder.encode(RAW_PASSWORD));

        // set the customer on the context
        setUpContext(customer);

        CustomerEmailUpdateRequest updateRequest = new CustomerEmailUpdateRequest(
                RAW_PASSWORD,
                "david@test.com"
        );

        // when
        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(customerRepository.existsByEmail(updateRequest.newEmail())).thenReturn(true);

        CustomerEmailTakenException exception = assertThrows(
                CustomerEmailTakenException.class,
                () -> customerService.updateEmail(updateRequest)
        );

        // then
        assertEquals(Exceptions.CUSTOMER.EMAIL_TAKEN, exception.getMessage());
    }

    @Test
    @DisplayName("Should not update customer email when password is wrong")
    void shouldNotUpdateCustomerEmailWhenPasswordIsWrong() {
        // given
        Customer customer = Customer.create()
                                    .setId(2L)
                                    .setEmail("customer@demo.com")
                                    .setPassword(passwordEncoder.encode(RAW_PASSWORD));

        // set the customer on the context
        setUpContext(customer);

        CustomerEmailUpdateRequest updateRequest = new CustomerEmailUpdateRequest(
                "wrong password",
                "david@test.com"
        );

        // when
        AccountInvalidPasswordConfirmationException exception = assertThrows(
                AccountInvalidPasswordConfirmationException.class,
                () -> customerService.updateEmail(updateRequest)
        );

        // then
        assertEquals(Exceptions.ACCOUNT.INVALID_PASSWORD, exception.getMessage());
    }
}
