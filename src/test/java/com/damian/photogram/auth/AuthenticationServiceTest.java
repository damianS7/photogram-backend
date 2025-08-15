package com.damian.photogram.auth;

import com.damian.photogram.accounts.account.AccountRepository;
import com.damian.photogram.accounts.account.AccountStatus;
import com.damian.photogram.accounts.account.exception.AccountDisabledException;
import com.damian.photogram.accounts.auth.AuthenticationService;
import com.damian.photogram.accounts.auth.exception.AuthenticationBadCredentialsException;
import com.damian.photogram.accounts.auth.http.AuthenticationRequest;
import com.damian.photogram.accounts.auth.http.AuthenticationResponse;
import com.damian.photogram.common.exception.Exceptions;
import com.damian.photogram.common.exception.PasswordMismatchException;
import com.damian.photogram.common.utils.JWTUtil;
import com.damian.photogram.customers.Customer;
import com.damian.photogram.customers.CustomerGender;
import com.damian.photogram.customers.CustomerRepository;
import com.damian.photogram.customers.CustomerService;
import com.damian.photogram.customers.exception.CustomerNotFoundException;
import com.damian.photogram.customers.http.request.CustomerPasswordUpdateRequest;
import com.damian.photogram.customers.http.request.CustomerRegistrationRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Habilita Mockito en JUnit 5
public class AuthenticationServiceTest {

    private final String RAW_PASSWORD = "123456";

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Mock
    private CustomerService customerService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private JWTUtil jwtUtil;

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
    @DisplayName("should register a new customers")
    void shouldRegisterCustomer() {
        // given
        Customer givenCustomer = new Customer();
        givenCustomer.setEmail("customers@test.com");
        givenCustomer.setPassword("123456");
        givenCustomer.getProfile().setFirstName("John");
        givenCustomer.getProfile().setLastName("Wick");
        givenCustomer.getProfile().setPhone("123 123 123");
        givenCustomer.getProfile().setGender(CustomerGender.MALE);
        givenCustomer.getProfile().setBirthdate(LocalDate.of(1989, 1, 1));
        givenCustomer.getProfile().setImageFilename("no photoPath");

        CustomerRegistrationRequest registrationRequest = new CustomerRegistrationRequest(
                givenCustomer.getEmail(),
                givenCustomer.getPassword(),
                givenCustomer.getProfile().getFirstName(),
                givenCustomer.getProfile().getLastName(),
                givenCustomer.getProfile().getPhone(),
                givenCustomer.getProfile().getBirthdate(),
                givenCustomer.getProfile().getGender()
        );

        // when
        when(customerService.createCustomer(any(CustomerRegistrationRequest.class))).thenReturn(givenCustomer);
        Customer registeredCustomer = authenticationService.register(registrationRequest);

        // then
        verify(customerService, times(1)).createCustomer(registrationRequest);
        assertThat(registeredCustomer).isNotNull();
        assertThat(registeredCustomer.getEmail()).isEqualTo(givenCustomer.getEmail());
        assertThat(registeredCustomer.getProfile().getFirstName()).isEqualTo(givenCustomer.getProfile().getFirstName());
        assertThat(registeredCustomer.getProfile().getLastName()).isEqualTo(givenCustomer.getProfile().getLastName());
        assertThat(registeredCustomer.getProfile().getPhone()).isEqualTo(givenCustomer.getProfile().getPhone());
        assertThat(registeredCustomer.getProfile().getGender()).isEqualTo(givenCustomer.getProfile().getGender());
        assertThat(registeredCustomer.getProfile().getBirthdate()).isEqualTo(givenCustomer.getProfile().getBirthdate());
    }

    @Test
    @DisplayName("should login when valid credentials")
    void shouldLoginWhenValidCredentials() {
        // given
        Authentication authentication = mock(Authentication.class);
        String token = "jwt-token";

        Customer customer = new Customer(
                1L,
                "alice@gmail.com",
                "123456"
        );

        AuthenticationRequest request = new AuthenticationRequest(customer.getEmail(), customer.getPassword());

        // when
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtUtil.generateToken(request.email())).thenReturn(token);
        when(authentication.getPrincipal()).thenReturn(customer);

        AuthenticationResponse response = authenticationService.login(request);

        // then
        assertThat(response.token()).isEqualTo(token);
    }

    @Test
    @DisplayName("should not login when invalid credentials")
    void shouldNotLoginWhenInvalidCredentials() {
        // given
        Customer customer = new Customer(
                1L,
                "alice@gmail.com",
                "123456"
        );

        AuthenticationRequest request = new AuthenticationRequest(customer.getEmail(), customer.getPassword());

        // when
        when(authenticationManager.authenticate(any())).thenThrow(AuthenticationBadCredentialsException.class);

        AuthenticationBadCredentialsException exception = assertThrows(
                AuthenticationBadCredentialsException.class,
                () -> authenticationService.login(request)
        );

        // Then
        assertEquals(Exceptions.AUTH.BAD_CREDENTIALS, exception.getMessage());
    }

    @Test
    @DisplayName("should not login when accounts is disabled")
    void shouldNotLoginWhenAccountIsDisabled() {
        // given
        Authentication authentication = mock(Authentication.class);
        String token = "jwt-token";

        Customer customer = new Customer(
                1L,
                "alice@gmail.com",
                "123456"
        );
        customer.getAccount().setAccountStatus(AccountStatus.SUSPENDED);

        AuthenticationRequest request = new AuthenticationRequest(customer.getEmail(), customer.getPassword());

        // when
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtUtil.generateToken(request.email())).thenReturn(token);
        when(authentication.getPrincipal()).thenReturn(customer);

        AccountDisabledException exception = assertThrows(
                AccountDisabledException.class,
                () -> authenticationService.login(request)
        );

        // Then
        assertEquals(Exceptions.CUSTOMER.DISABLED, exception.getMessage());
    }

    @Test
    @DisplayName("Should update customers password")
    void shouldUpdateCustomerPassword() {
        // given
        final String currentRawPassword = "123456";
        final String currentEncodedPassword = passwordEncoder.encode(currentRawPassword);
        final String rawNewPassword = "1234";
        final String encodedNewPassword = passwordEncoder.encode(rawNewPassword);

        Customer customer = new Customer(
                10L,
                "customers@test.com",
                currentEncodedPassword
        );

        CustomerPasswordUpdateRequest updateRequest = new CustomerPasswordUpdateRequest(
                currentRawPassword,
                rawNewPassword
        );

        // set the customers on the context
        setUpContext(customer);

        // when
        when(bCryptPasswordEncoder.encode(rawNewPassword)).thenReturn(encodedNewPassword);
        when(accountRepository.findByCustomer_Id(customer.getId())).thenReturn(Optional.of(customer.getAccount()));
        authenticationService.updatePassword(updateRequest);

        // then
        verify(accountRepository, times(1)).save(customer.getAccount());
        assertThat(customer.getPassword()).isEqualTo(encodedNewPassword);
    }

    @Test
    @DisplayName("Should not update password when current password does not match")
    void shouldNotUpdatePasswordWhenCurrentPasswordDoesNotMatch() {
        // given
        Customer customer = new Customer(
                10L,
                "customers@test.com",
                bCryptPasswordEncoder.encode("1234")
        );

        // set the customers on the context
        setUpContext(customer);

        CustomerPasswordUpdateRequest updateRequest = new CustomerPasswordUpdateRequest(
                "wrongPassword",
                "1234"
        );

        // when
        PasswordMismatchException exception = assertThrows(
                PasswordMismatchException.class,
                () -> authenticationService.updatePassword(
                        updateRequest
                )
        );
        // then
        assertEquals(PasswordMismatchException.PASSWORD_MISMATCH, exception.getMessage());
    }

    @Test
    @DisplayName("Should not update password when auth entity not found")
    void shouldNotUpdatePasswordWhenAuthNotFound() {
        // given
        Customer customer = new Customer(
                10L,
                "customers@test.com",
                passwordEncoder.encode("1234")
        );

        // set the customers on the context
        setUpContext(customer);

        CustomerPasswordUpdateRequest updateRequest = new CustomerPasswordUpdateRequest(
                "1234",
                "1234678Ax$"
        );

        when(accountRepository.findByCustomer_Id(customer.getId()))
                .thenReturn(Optional.empty());

        CustomerNotFoundException exception = assertThrows(
                CustomerNotFoundException.class,
                () -> authenticationService.updatePassword(
                        updateRequest
                )
        );

        // then
        assertEquals(Exceptions.CUSTOMER.NOT_FOUND, exception.getMessage());
    }
}
