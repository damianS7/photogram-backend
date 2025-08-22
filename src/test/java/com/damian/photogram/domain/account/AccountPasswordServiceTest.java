package com.damian.photogram.domain.account;

import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.core.exception.PasswordMismatchException;
import com.damian.photogram.core.service.EmailSenderService;
import com.damian.photogram.domain.account.dto.request.AccountPasswordResetRequest;
import com.damian.photogram.domain.account.dto.request.AccountPasswordResetSetRequest;
import com.damian.photogram.domain.account.enums.AccountTokenType;
import com.damian.photogram.domain.account.model.Account;
import com.damian.photogram.domain.account.model.AccountToken;
import com.damian.photogram.domain.account.repository.AccountRepository;
import com.damian.photogram.domain.account.repository.AccountTokenRepository;
import com.damian.photogram.domain.account.service.AccountPasswordService;
import com.damian.photogram.domain.account.service.AccountTokenVerificationService;
import com.damian.photogram.domain.customer.dto.request.CustomerPasswordUpdateRequest;
import com.damian.photogram.domain.customer.exception.CustomerNotFoundException;
import com.damian.photogram.domain.customer.model.Customer;
import com.damian.photogram.domain.customer.repository.CustomerRepository;
import com.damian.photogram.domain.customer.service.CustomerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Habilita Mockito en JUnit 5
public class AccountPasswordServiceTest {

    private final String RAW_PASSWORD = "123456";

    @Mock
    private Environment env;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountPasswordService accountPasswordService;

    @Mock
    private CustomerService customerService;

    @Mock
    private EmailSenderService emailSenderService;

    @Mock
    private AccountTokenRepository accountTokenRepository;

    @Mock
    private AccountTokenVerificationService accountTokenVerificationService;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private BCryptPasswordEncoder passwordEncoder;

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
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(customer);
    }

    @Test
    @DisplayName("Should update customer password")
    void shouldUpdateCustomerPassword() {
        // given
        final String currentRawPassword = "123456";
        final String currentEncodedPassword = passwordEncoder.encode(currentRawPassword);
        final String rawNewPassword = "1234";
        final String encodedNewPassword = passwordEncoder.encode(rawNewPassword);

        Customer customer = new Customer(
                10L,
                "customer@test.com",
                currentEncodedPassword
        );

        CustomerPasswordUpdateRequest updateRequest = new CustomerPasswordUpdateRequest(
                currentRawPassword,
                rawNewPassword
        );

        // set the customer on the context
        setUpContext(customer);

        // when
        when(bCryptPasswordEncoder.encode(rawNewPassword)).thenReturn(encodedNewPassword);
        when(accountRepository.findByCustomer_Id(customer.getId())).thenReturn(Optional.of(customer.getAccount()));
        accountPasswordService.updatePassword(updateRequest);

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
                "customer@test.com",
                bCryptPasswordEncoder.encode("1234")
        );

        // set the customer on the context
        setUpContext(customer);

        CustomerPasswordUpdateRequest updateRequest = new CustomerPasswordUpdateRequest(
                "wrongPassword",
                "1234"
        );

        // when
        PasswordMismatchException exception = assertThrows(
                PasswordMismatchException.class,
                () -> accountPasswordService.updatePassword(
                        updateRequest
                )
        );
        // then
        assertEquals(PasswordMismatchException.PASSWORD_MISMATCH, exception.getMessage());
    }

    @Test
    @DisplayName("Should not update password when account not found")
    void shouldNotUpdatePasswordWhenAccountNotFound() {
        // given
        Customer customer = new Customer(
                10L,
                "customer@test.com",
                passwordEncoder.encode("1234")
        );

        // set the customer on the context
        setUpContext(customer);

        CustomerPasswordUpdateRequest updateRequest = new CustomerPasswordUpdateRequest(
                "1234",
                "1234678Ax$"
        );

        when(accountRepository.findByCustomer_Id(customer.getId()))
                .thenReturn(Optional.empty());

        CustomerNotFoundException exception = assertThrows(
                CustomerNotFoundException.class,
                () -> accountPasswordService.updatePassword(
                        updateRequest
                )
        );

        // then
        assertEquals(Exceptions.CUSTOMER.NOT_FOUND, exception.getMessage());
    }

    @Test
    @DisplayName("Should reset password")
    void shouldResetPassword() {
        // given
        final String currentRawPassword = "123456";
        final String currentEncodedPassword = passwordEncoder.encode(currentRawPassword);

        Customer customer = new Customer(
                10L,
                "customer@test.com",
                currentEncodedPassword
        );

        AccountPasswordResetRequest passwordResetRequest = new AccountPasswordResetRequest(
                customer.getEmail()
        );

        AccountToken token = new AccountToken(customer);
        token.setToken(token.generateToken());
        token.setType(AccountTokenType.RESET_PASSWORD);

        // set the customer on the context
        //        setUpContext(customer);

        // when
        when(accountRepository.findByCustomer_Email(customer.getEmail())).thenReturn(Optional.of(customer.getAccount()));
        when(accountTokenRepository.save(any(AccountToken.class))).thenReturn(token);
        when(env.getProperty("app.frontend.host")).thenReturn("photogram.local");
        when(env.getProperty("app.frontend.port")).thenReturn("8090");
        //        doNothing().when(emailSenderService).send(anyString(), anyString(), anyString());
        accountPasswordService.resetPassword(passwordResetRequest);

        // then
        verify(accountTokenRepository, times(1)).save(any(AccountToken.class));
    }

    @Test
    @DisplayName("Should set a new password after reset password")
    void shouldSetPasswordAfterResetPassword() {
        // given
        final String currentRawPassword = "123456";
        final String currentEncodedPassword = passwordEncoder.encode(currentRawPassword);
        final String rawNewPassword = "1111000";
        final String encodedNewPassword = passwordEncoder.encode(rawNewPassword);

        Customer customer = new Customer(
                10L,
                "customer@test.com",
                currentEncodedPassword
        );

        AccountPasswordResetSetRequest passwordResetRequest = new AccountPasswordResetSetRequest(
                customer.getEmail(),
                rawNewPassword
        );

        AccountToken token = new AccountToken(customer);
        token.setToken(token.generateToken());
        token.setType(AccountTokenType.RESET_PASSWORD);

        // set the customer on the context
        //        setUpContext(customer);

        // when
        when(customerRepository.findByEmail(customer.getEmail())).thenReturn(Optional.of(customer));
        when(accountRepository.findByCustomer_Id(customer.getId())).thenReturn(Optional.of(customer.getAccount()));
        when(accountTokenVerificationService.verify(token.getToken())).thenReturn(token);
        when(bCryptPasswordEncoder.encode(rawNewPassword)).thenReturn(encodedNewPassword);
        when(accountRepository.save(any(Account.class))).thenReturn(customer.getAccount());
        accountPasswordService.updatePassword(token.getToken(), passwordResetRequest);

        // then
        assertEquals(customer.getPassword(), encodedNewPassword);
    }
}
