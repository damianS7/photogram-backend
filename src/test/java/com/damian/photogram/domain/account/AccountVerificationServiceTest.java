package com.damian.photogram.domain.account;

import com.damian.photogram.domain.account.enums.AccountStatus;
import com.damian.photogram.domain.account.enums.AccountTokenType;
import com.damian.photogram.domain.account.exception.*;
import com.damian.photogram.domain.account.model.Account;
import com.damian.photogram.domain.account.model.AccountToken;
import com.damian.photogram.domain.account.repository.AccountRepository;
import com.damian.photogram.domain.account.repository.AccountTokenRepository;
import com.damian.photogram.domain.account.service.AccountVerificationService;
import com.damian.photogram.domain.customer.model.Customer;
import com.damian.photogram.domain.customer.repository.CustomerRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountVerificationServiceTest {

    private final String RAW_PASSWORD = "123456";

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AccountTokenRepository accountTokenRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountVerificationService accountVerificationService;

    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
    }

    @AfterEach
    public void tearDown() {
        customerRepository.deleteAll();
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should activate account")
    void shouldVerifyAccount() {
        // given
        Customer customer = new Customer(
                10L,
                "customer@test.com",
                passwordEncoder.encode(passwordEncoder.encode(RAW_PASSWORD))
        );

        AccountToken activationToken = new AccountToken(customer);
        activationToken.setToken("sdfsidjgfiosdjfi");
        activationToken.setType(AccountTokenType.ACCOUNT_VERIFICATION);

        // when
        when(accountTokenRepository.findByToken(activationToken.getToken())).thenReturn(Optional.of(activationToken));
        when(accountRepository.findByCustomer_Id(customer.getId())).thenReturn(Optional.of(customer.getAccount()));
        when(accountTokenRepository.save(any(AccountToken.class))).thenReturn(activationToken);
        when(accountRepository.save(any(Account.class))).thenReturn(customer.getAccount());
        accountVerificationService.verifyAccount(activationToken.getToken());

        // then
        //        verify(accountRepository, times(1)).save(customer.getAccount());
        assertThat(activationToken.isUsed()).isEqualTo(true);
        assertThat(customer.getAccount().getAccountStatus()).isEqualTo(AccountStatus.VERIFIED);
    }

    @Test
    @DisplayName("Should not activate account when account not exists")
    void shouldNotVerifyAccountWhenAccountNotExists() {
        // given
        Customer customer = new Customer(
                10L,
                "customer@test.com",
                passwordEncoder.encode(passwordEncoder.encode(RAW_PASSWORD))
        );

        // set the customer on the context
        //        setUpContext(customer);

        AccountToken activationToken = new AccountToken(customer);
        activationToken.setToken("sdfsidjgfiosdjfi");
        activationToken.setType(AccountTokenType.ACCOUNT_VERIFICATION);

        // when
        when(accountTokenRepository.findByToken(activationToken.getToken())).thenReturn(Optional.of(activationToken));
        when(accountRepository.findByCustomer_Id(customer.getId())).thenReturn(Optional.empty());
        assertThrows(
                AccountNotFoundException.class,
                () -> accountVerificationService.verifyAccount(activationToken.getToken())
        );
    }

    @Test
    @DisplayName("Should not activate account when account is Suspended")
    void shouldNotVerifyAccountWhenAccountIsSuspended() {
        // given
        Customer customer = new Customer(
                10L,
                "customer@test.com",
                passwordEncoder.encode(passwordEncoder.encode(RAW_PASSWORD))
        );
        customer.getAccount().setAccountStatus(AccountStatus.SUSPENDED);

        AccountToken activationToken = new AccountToken(customer);
        activationToken.setToken("sdfsidjgfiosdjfi");
        activationToken.setType(AccountTokenType.ACCOUNT_VERIFICATION);

        // when
        when(accountTokenRepository.findByToken(activationToken.getToken())).thenReturn(Optional.of(activationToken));
        when(accountRepository.findByCustomer_Id(customer.getId())).thenReturn(Optional.of(customer.getAccount()));
        assertThrows(
                AccountVerificationNotPendingException.class,
                () -> accountVerificationService.verifyAccount(activationToken.getToken())
        );
    }


    @Test
    @DisplayName("Should not activate account when account is active")
    void shouldNotVerifyAccountWhenAccountIsActive() {
        // given
        Customer customer = new Customer(
                10L,
                "customer@test.com",
                passwordEncoder.encode(passwordEncoder.encode(RAW_PASSWORD))
        );
        customer.getAccount().setAccountStatus(AccountStatus.VERIFIED);

        AccountToken activationToken = new AccountToken(customer);
        activationToken.setToken("sdfsidjgfiosdjfi");
        activationToken.setType(AccountTokenType.ACCOUNT_VERIFICATION);

        // when
        when(accountTokenRepository.findByToken(activationToken.getToken())).thenReturn(Optional.of(activationToken));
        when(accountRepository.findByCustomer_Id(customer.getId())).thenReturn(Optional.of(customer.getAccount()));
        assertThrows(
                AccountVerificationNotPendingException.class,
                () -> accountVerificationService.verifyAccount(activationToken.getToken())
        );
    }

    @Test
    @DisplayName("Should verify token")
    void shouldValidateToken() {
        // given
        Customer customer = new Customer(
                10L,
                "customer@test.com",
                passwordEncoder.encode(RAW_PASSWORD)
        );

        AccountToken accountToken = new AccountToken();
        accountToken.setCustomer(customer);
        accountToken.setToken("token");
        accountToken.setCreatedAt(Instant.now());
        accountToken.setExpiresAt(Instant.now().plus(1, ChronoUnit.DAYS));
        accountToken.setToken("token");

        // when
        when(accountTokenRepository.findByToken(accountToken.getToken())).thenReturn(Optional.of(accountToken));
        AccountToken result = accountVerificationService.validateToken(accountToken.getToken());

        // then
        verify(accountTokenRepository, times(1)).findByToken(accountToken.getToken());
        assertEquals(result.getToken(), accountToken.getToken());
    }

    @Test
    @DisplayName("Should not verify token when is wrong")
    void shouldNotVerifyAccountWhenTokenIsWrong() {
        // given
        // when
        when(accountTokenRepository.findByToken(anyString())).thenReturn(Optional.empty());
        assertThrows(
                AccountVerificationTokenNotFoundException.class,
                () -> accountVerificationService.validateToken(anyString())
        );

        // then
        verify(accountTokenRepository, times(1)).findByToken(anyString());
    }

    @Test
    @DisplayName("Should not verify token when is expired")
    void shouldNotVerifyAccountWhenTokenIsExpired() {
        // given
        Customer customer = new Customer(
                10L,
                "customer@test.com",
                passwordEncoder.encode(RAW_PASSWORD)
        );

        AccountToken accountToken = new AccountToken();
        accountToken.setCustomer(customer);
        accountToken.setToken("token");
        accountToken.setCreatedAt(Instant.now());
        accountToken.setExpiresAt(Instant.now().minus(1, ChronoUnit.DAYS));
        accountToken.setToken("token");

        // when
        when(accountTokenRepository.findByToken(anyString())).thenReturn(Optional.of(accountToken));
        assertThrows(
                AccountVerificationTokenExpiredException.class,
                () -> accountVerificationService.validateToken(anyString())
        );

        // then
        verify(accountTokenRepository, times(1)).findByToken(anyString());
    }

    @Test
    @DisplayName("Should not verify token when is already used")
    void shouldNotVerifyAccountWhenTokenIsUsed() {
        // given
        Customer customer = new Customer(
                10L,
                "customer@test.com",
                passwordEncoder.encode(RAW_PASSWORD)
        );

        AccountToken accountToken = AccountToken.create()
                                                .setUsed(true)
                                                .setCustomer(customer)
                                                .setCreatedAt(Instant.now())
                                                .setExpiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
                                                .setToken("token");

        // when
        when(accountTokenRepository.findByToken(anyString())).thenReturn(Optional.of(accountToken));
        assertThrows(
                AccountVerificationTokenUsedException.class,
                () -> accountVerificationService.validateToken(anyString())
        );

        // then
        verify(accountTokenRepository, times(1)).findByToken(anyString());
    }
    // TODO shouldCreateAccountActivationToken
}
