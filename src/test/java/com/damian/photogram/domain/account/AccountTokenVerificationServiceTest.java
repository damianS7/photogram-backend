package com.damian.photogram.domain.account;

import com.damian.photogram.domain.account.exception.AccountActivationTokenExpiredException;
import com.damian.photogram.domain.account.exception.AccountActivationTokenNotFoundException;
import com.damian.photogram.domain.account.exception.AccountActivationTokenUsedException;
import com.damian.photogram.domain.account.model.AccountToken;
import com.damian.photogram.domain.account.repository.AccountTokenRepository;
import com.damian.photogram.domain.account.service.AccountTokenVerificationService;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountTokenVerificationServiceTest {

    private final String RAW_PASSWORD = "123456";

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private AccountTokenVerificationService accountTokenVerificationService;

    @Mock
    private AccountTokenRepository accountTokenRepository;

    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
        customerRepository.deleteAll();
    }

    @Test
    @DisplayName("Should verify token")
    void shouldVerifyToken() {
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
        AccountToken result = accountTokenVerificationService.verify(accountToken.getToken());

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
                AccountActivationTokenNotFoundException.class,
                () -> accountTokenVerificationService.verify(anyString())
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
                AccountActivationTokenExpiredException.class,
                () -> accountTokenVerificationService.verify(anyString())
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
                AccountActivationTokenUsedException.class,
                () -> accountTokenVerificationService.verify(anyString())
        );

        // then
        verify(accountTokenRepository, times(1)).findByToken(anyString());
    }
}
