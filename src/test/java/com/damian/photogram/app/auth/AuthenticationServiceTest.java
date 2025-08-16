package com.damian.photogram.app.auth;

import com.damian.photogram.app.auth.dto.AuthenticationRequest;
import com.damian.photogram.app.auth.dto.AuthenticationResponse;
import com.damian.photogram.app.auth.exception.AuthenticationBadCredentialsException;
import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.core.utils.JwtUtil;
import com.damian.photogram.domain.account.enums.AccountStatus;
import com.damian.photogram.domain.account.exception.AccountDisabledException;
import com.damian.photogram.domain.customer.model.Customer;
import com.damian.photogram.domain.customer.repository.CustomerRepository;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class) // Habilita Mockito en JUnit 5
public class AuthenticationServiceTest {

    private final String RAW_PASSWORD = "123456";

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

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
        customer.getAccount().setAccountStatus(AccountStatus.ACTIVE);

        AuthenticationRequest request = new AuthenticationRequest(customer.getEmail(), customer.getPassword());

        // when
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtUtil.generateToken(anyMap(), anyString())).thenReturn(token);
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
        assertEquals(Exceptions.ACCOUNT.BAD_CREDENTIALS, exception.getMessage());
    }

    @Test
    @DisplayName("should not login when account is disabled")
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
        when(jwtUtil.generateToken(anyMap(), anyString())).thenReturn(token);
        when(authentication.getPrincipal()).thenReturn(customer);

        AccountDisabledException exception = assertThrows(
                AccountDisabledException.class,
                () -> authenticationService.login(request)
        );

        // Then
        assertEquals(Exceptions.ACCOUNT.SUSPENDED, exception.getMessage());
    }

    // TODO shouldValidateToken
}
