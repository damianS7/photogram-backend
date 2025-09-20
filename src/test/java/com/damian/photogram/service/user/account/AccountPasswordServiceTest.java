package com.damian.photogram.service.user.account;

import com.damian.photogram.core.AbstractServiceTest;
import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.domain.user.enums.AccountTokenType;
import com.damian.photogram.domain.user.exception.AccountInvalidPasswordConfirmationException;
import com.damian.photogram.domain.user.exception.AccountNotFoundException;
import com.damian.photogram.domain.user.exception.CustomerNotFoundException;
import com.damian.photogram.domain.user.model.Account;
import com.damian.photogram.domain.user.model.AccountToken;
import com.damian.photogram.domain.user.model.Customer;
import com.damian.photogram.domain.user.repository.AccountRepository;
import com.damian.photogram.domain.user.repository.AccountTokenRepository;
import com.damian.photogram.infrastructure.mail.EmailSenderService;
import com.damian.photogram.service.user.AccountPasswordService;
import com.damian.photogram.service.user.AccountVerificationService;
import com.damian.photogram.web.rest.user.dto.request.AccountPasswordResetRequest;
import com.damian.photogram.web.rest.user.dto.request.AccountPasswordResetSetRequest;
import com.damian.photogram.web.rest.user.dto.request.AccountPasswordUpdateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class AccountPasswordServiceTest extends AbstractServiceTest {

    @Mock
    private EmailSenderService emailSenderService;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountPasswordService accountPasswordService;

    @Mock
    private AccountTokenRepository accountTokenRepository;

    @Mock
    private AccountVerificationService accountVerificationService;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Test
    @DisplayName("Should update account password")
    void shouldUpdateAccountPassword() {
        // given
        final String rawNewPassword = "1234";
        final String encodedNewPassword = passwordEncoder.encode(rawNewPassword);

        Customer customer = Customer
                .create()
                .setId(10L)
                .setEmail("customer@demo.com")
                .setPassword(passwordEncoder.encode(RAW_PASSWORD));

        AccountPasswordUpdateRequest updateRequest = new AccountPasswordUpdateRequest(
                RAW_PASSWORD,
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
        Customer customer = Customer
                .create()
                .setId(10L)
                .setEmail("customer@demo.com")
                .setPassword(passwordEncoder.encode(RAW_PASSWORD));

        // set the customer on the context
        setUpContext(customer);

        AccountPasswordUpdateRequest updateRequest = new AccountPasswordUpdateRequest(
                "wrongPassword",
                "1234"
        );

        // when
        AccountInvalidPasswordConfirmationException exception = assertThrows(
                AccountInvalidPasswordConfirmationException.class,
                () -> accountPasswordService.updatePassword(
                        updateRequest
                )
        );
        // then
        assertEquals(Exceptions.ACCOUNT.INVALID_PASSWORD, exception.getMessage());
    }

    @Test
    @DisplayName("Should not update password when account not found")
    void shouldNotUpdatePasswordWhenAccountNotFound() {
        // given
        Customer customer = Customer
                .create()
                .setId(10L)
                .setEmail("customer@demo.com")
                .setPassword(passwordEncoder.encode(RAW_PASSWORD));

        // set the customer on the context
        setUpContext(customer);

        AccountPasswordUpdateRequest updateRequest = new AccountPasswordUpdateRequest(
                RAW_PASSWORD,
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
    void shouldGeneratePasswordResetToken() {
        // given
        final String currentEncodedPassword = passwordEncoder.encode(RAW_PASSWORD);

        Customer customer = Customer
                .create()
                .setId(10L)
                .setEmail("customer@demo.com")
                .setPassword(passwordEncoder.encode(RAW_PASSWORD));

        AccountPasswordResetRequest passwordResetRequest = new AccountPasswordResetRequest(
                customer.getEmail()
        );

        // when
        when(accountRepository.findByCustomer_Email(customer.getEmail())).thenReturn(Optional.of(customer.getAccount()));
        when(accountTokenRepository.save(any(AccountToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AccountToken generatedToken = accountPasswordService.generatePasswordResetToken(passwordResetRequest);

        // then
        assertThat(generatedToken)
                .isNotNull();
        assertThat(generatedToken.getToken().length()).isGreaterThanOrEqualTo(5);
        verify(accountTokenRepository, times(1)).save(any(AccountToken.class));
    }

    @Test
    @DisplayName("Should not create password reset token when account not found")
    void shouldNotGeneratePasswordResetTokenWhenAccountNotFound() {
        // given
        Customer customer = Customer
                .create()
                .setId(10L)
                .setEmail("customer@demo.com")
                .setPassword(passwordEncoder.encode(RAW_PASSWORD));

        AccountPasswordResetRequest passwordResetRequest = new AccountPasswordResetRequest(
                customer.getEmail()
        );

        AccountToken token = new AccountToken(customer);
        token.setToken(token.generateToken());
        token.setType(AccountTokenType.RESET_PASSWORD);

        // when
        when(accountRepository.findByCustomer_Email(customer.getEmail())).thenReturn(Optional.empty());
        assertThrows(
                AccountNotFoundException.class,
                () -> accountPasswordService.generatePasswordResetToken(passwordResetRequest)
        );

        // then
        verify(accountRepository, times(1)).findByCustomer_Email(anyString());
    }

    @Test
    @DisplayName("Should set a new password after reset password")
    void shouldSetPasswordAfterGeneratePasswordResetToken() {
        // given
        final String rawNewPassword = "1111000";
        final String encodedNewPassword = passwordEncoder.encode(rawNewPassword);

        Customer customer = Customer
                .create()
                .setId(10L)
                .setEmail("customer@demo.com")
                .setPassword(passwordEncoder.encode(RAW_PASSWORD));

        AccountPasswordResetSetRequest passwordResetRequest = new AccountPasswordResetSetRequest(
                rawNewPassword
        );

        AccountToken token = new AccountToken(customer);
        token.setToken(token.generateToken());
        token.setType(AccountTokenType.RESET_PASSWORD);

        // when
        //        when(customerRepository.findByEmail(customer.getEmail())).thenReturn(Optional.of(customer));
        when(accountRepository.findByCustomer_Id(customer.getId())).thenReturn(Optional.of(customer.getAccount()));
        when(accountVerificationService.validateToken(token.getToken())).thenReturn(token);
        when(bCryptPasswordEncoder.encode(rawNewPassword)).thenReturn(encodedNewPassword);
        when(accountRepository.save(any(Account.class))).thenReturn(customer.getAccount());
        doNothing().when(emailSenderService).send(anyString(), anyString(), anyString());
        accountPasswordService.passwordResetWithToken(token.getToken(), passwordResetRequest);

        // then
        assertEquals(customer.getPassword(), encodedNewPassword);
    }
}
