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
        assertEquals(2, result.getTotalElements());
        assertEquals("customer1@test.com", result.getContent().get(0).getEmail());
        assertEquals("customer2@test.com", result.getContent().get(1).getEmail());
        verify(customerRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Should find customer")
    void shouldFindCustomer() {
        // given
        Customer customer = new Customer(
                1L,
                "customer@test.com",
                "1234"
        );

        // when
        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        Customer storedCustomer = customerService.getCustomer(customer.getId());

        // then
        verify(customerRepository, times(1)).findById(customer.getId());
        assertEquals(customer.getId(), storedCustomer.getId());
        assertEquals(customer.getEmail(), storedCustomer.getEmail());
    }

    @Test
    @DisplayName("Should not find customer when not exist")
    void shouldNotFindCustomerWhenNotExist() {
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
        final String passwordHash = "¢5554ml;f;lsd";
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
        when(customerRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        customerService.createCustomer(request);

        // then
        ArgumentCaptor<Customer> customerArgumentCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(customerArgumentCaptor.capture());

        Customer customer = customerArgumentCaptor.getValue();
        verify(customerRepository, times(1)).save(customer);
        assertThat(customer.getId()).isNull();
        assertThat(customer.getEmail()).isEqualTo(request.email());
        assertThat(customer.getPassword()).isEqualTo(passwordHash);
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
        when(customerRepository.findByEmail(request.email())).thenReturn(Optional.of(new Customer()));
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
        boolean isDeleted = customerService.deleteCustomer(id);

        // then
        verify(customerRepository, times(1)).deleteById(id);
        verify(customerRepository).deleteById(id);
        assertThat(isDeleted).isTrue();
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
        String currentRawPassword = "123456";
        String currentEncodedPassword = passwordEncoder.encode(currentRawPassword);

        Customer customer = new Customer(
                10L,
                "customer@test.com",
                currentEncodedPassword
        );

        // set the customer on the context
        setUpContext(customer);

        CustomerEmailUpdateRequest updateRequest = new CustomerEmailUpdateRequest(
                currentRawPassword,
                "david@test.com"
        );

        // when
        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        customerService.updateEmail(updateRequest);

        // then
        verify(customerRepository, times(1)).save(customer);
        assertThat(customer.getEmail()).isEqualTo(updateRequest.newEmail());
        assertThat(customer.getPassword()).isEqualTo(currentEncodedPassword);
    }

    @Test
    @DisplayName("Should not update customer email when password is wrong")
    void shouldNotUpdateCustomerEmailWhenPasswordIsWrong() {
        // given
        String currentRawPassword = "123456";
        String currentEncodedPassword = passwordEncoder.encode(currentRawPassword);

        Customer customer = new Customer(
                10L,
                "customer@test.com",
                currentEncodedPassword
        );

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
