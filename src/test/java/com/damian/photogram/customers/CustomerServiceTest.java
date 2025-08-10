package com.damian.photogram.customers;

import com.damian.photogram.common.exception.Exceptions;
import com.damian.photogram.common.exception.PasswordMismatchException;
import com.damian.photogram.customers.exception.CustomerEmailTakenException;
import com.damian.photogram.customers.exception.CustomerNotFoundException;
import com.damian.photogram.customers.http.request.CustomerEmailUpdateRequest;
import com.damian.photogram.customers.http.request.CustomerRegistrationRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        customerRepository.deleteAll();
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    void setUpContext(Customer customer) {
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(customer);
    }

    @Test
    @DisplayName("Should get all customers")
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
    @DisplayName("Should find customers")
    void shouldFindCustomer() {
        // given
        Customer customer = new Customer(
                1L,
                "customers@test.com",
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
    @DisplayName("Should not find customers when not exist")
    void shouldNotFindCustomerWhenNotExist() {
        // given
        Long id = -1L;

        // when
        CustomerNotFoundException exception = assertThrows(
                CustomerNotFoundException.class,
                () -> customerService.getCustomer(id)
        );

        // then
        assertTrue(exception.getMessage().contains("Customer not found"));
    }

    @Test
    @DisplayName("Should create customers")
    void shouldCreateCustomer() {
        // given
        final String passwordHash = "¢5554ml;f;lsd";
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
                "david@gmail.com",
                "123456",
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
    @DisplayName("Should not create any customers when email is taken")
    void shouldNotCreateCustomerWhenEmailIsTaken() {
        // given
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
                "david@gmail.com",
                "123456",
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
    @DisplayName("Should delete customers")
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
    @DisplayName("Should not delete customers when not exist")
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
    @DisplayName("Should update customers email")
    void shouldUpdateCustomerEmail() {
        // given
        String currentRawPassword = "123456";
        String currentEncodedPassword = passwordEncoder.encode(currentRawPassword);

        Customer customer = new Customer(
                10L,
                "customers@test.com",
                currentEncodedPassword
        );

        // set the customers on the context
        setUpContext(customer);

        CustomerEmailUpdateRequest updateRequest = new CustomerEmailUpdateRequest(
                currentRawPassword,
                "david@test.com"
        );

        // when
        //        when(bCryptPasswordEncoder.matches(currentRawPassword, currentEncodedPassword)).thenReturn(true);
        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));

        customerService.updateEmail(updateRequest);

        // then
        verify(customerRepository, times(1)).save(customer);
        assertThat(customer.getEmail()).isEqualTo(updateRequest.newEmail());
        assertThat(customer.getPassword()).isEqualTo(currentEncodedPassword);
    }

    @Test
    @DisplayName("Should not update customers email when password is wrong")
    void shouldNotUpdateCustomerEmailWhenPasswordIsWrong() {
        // given
        String currentRawPassword = "123456";
        String currentEncodedPassword = passwordEncoder.encode(currentRawPassword);

        Customer customer = new Customer(
                10L,
                "customers@test.com",
                currentEncodedPassword
        );

        // set the customers on the context
        setUpContext(customer);

        CustomerEmailUpdateRequest updateRequest = new CustomerEmailUpdateRequest(
                "wrong password",
                "david@test.com"
        );

        // when
        PasswordMismatchException exception = assertThrows(
                PasswordMismatchException.class,
                () -> customerService.updateEmail(updateRequest)
        );

        // then
        assertEquals(PasswordMismatchException.PASSWORD_MISMATCH, exception.getMessage());
    }
}
