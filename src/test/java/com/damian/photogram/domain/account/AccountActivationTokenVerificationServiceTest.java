package com.damian.photogram.domain.account;

import com.damian.photogram.domain.account.exception.AccountActivationTokenExpiredException;
import com.damian.photogram.domain.account.exception.AccountActivationTokenNotFoundException;
import com.damian.photogram.domain.account.model.AccountToken;
import com.damian.photogram.domain.account.repository.AccountRepository;
import com.damian.photogram.domain.account.repository.AccountTokenRepository;
import com.damian.photogram.domain.account.service.AccountActivationTokenVerificationService;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Habilita Mockito en JUnit 5
public class AccountActivationTokenVerificationServiceTest {

    private final String RAW_PASSWORD = "123456";

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountActivationTokenVerificationService accountActivationTokenVerificationService;

    @Mock
    private CustomerService customerService;

    @Mock
    private AccountTokenRepository accountTokenRepository;

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
    @DisplayName("Should verify token")
    void shouldVerifyToken() {
        // given
        Customer customer = new Customer(
                10L,
                "customer@test.com",
                "currentEncodedPassword"
        );

        AccountToken accountToken = new AccountToken();
        accountToken.setCustomer(customer);
        accountToken.setToken("token");
        accountToken.setCreatedAt(Instant.now());
        accountToken.setExpiresAt(Instant.now().plus(1, ChronoUnit.DAYS));
        accountToken.setToken("token");

        // when
        when(accountTokenRepository.findByToken(accountToken.getToken())).thenReturn(Optional.of(accountToken));
        AccountToken result = accountActivationTokenVerificationService.verify(accountToken.getToken());

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
                () -> accountActivationTokenVerificationService.verify(anyString())
        );

        // then
        verify(accountTokenRepository, times(1)).findByToken(anyString());
    }

    // shouldNotVerifyAccountWhenTokenIsExpired
    @Test
    @DisplayName("Should not verify token when is expired")
    void shouldNotVerifyAccountWhenTokenIsExpired() {
        // given
        Customer customer = new Customer(
                10L,
                "customer@test.com",
                "currentEncodedPassword"
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
                () -> accountActivationTokenVerificationService.verify(anyString())
        );

        // then
        verify(accountTokenRepository, times(1)).findByToken(anyString());
    }
}
