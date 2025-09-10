package com.damian.photogram.app.auth;

import com.damian.photogram.AbstractServiceTest;
import com.damian.photogram.app.auth.dto.AuthenticationRequest;
import com.damian.photogram.app.auth.dto.AuthenticationResponse;
import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.core.security.user.User;
import com.damian.photogram.core.utils.JwtUtil;
import com.damian.photogram.domain.account.enums.AccountStatus;
import com.damian.photogram.domain.account.exception.AccountNotVerifiedException;
import com.damian.photogram.domain.account.exception.AccountSuspendedException;
import com.damian.photogram.domain.customer.model.Customer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AuthenticationServiceTest extends AbstractServiceTest {

    @InjectMocks
    private AuthenticationService authenticationService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

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
        User user = new User(customer);

        customer.getAccount().setAccountStatus(AccountStatus.VERIFIED);

        AuthenticationRequest request = new AuthenticationRequest(customer.getEmail(), customer.getPassword());

        // when
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtUtil.generateToken(anyMap(), anyString())).thenReturn(token);
        when(authentication.getPrincipal()).thenReturn(user);

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
                "1234"
        );

        AuthenticationRequest request = new AuthenticationRequest(customer.getEmail(), customer.getPassword());

        // when
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException(Exceptions.AUTH.BAD_CREDENTIALS));

        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> authenticationService.login(request)
        );

        // Then
        assertEquals(Exceptions.AUTH.BAD_CREDENTIALS, exception.getMessage());
    }

    @Test
    @DisplayName("should not login when account is suspended")
    void shouldNotLoginWhenAccountIsSuspended() {
        // given
        Authentication authentication = mock(Authentication.class);
        String token = "jwt-token";

        Customer customer = new Customer(
                1L,
                "alice@gmail.com",
                "123456"
        );
        User user = new User(customer);
        customer.getAccount().setAccountStatus(AccountStatus.SUSPENDED);

        AuthenticationRequest request = new AuthenticationRequest(customer.getEmail(), customer.getPassword());

        // when
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtUtil.generateToken(anyMap(), anyString())).thenReturn(token);
        when(authentication.getPrincipal()).thenReturn(user);

        AccountSuspendedException exception = assertThrows(
                AccountSuspendedException.class,
                () -> authenticationService.login(request)
        );

        // Then
        assertEquals(Exceptions.AUTH.ACCOUNT_SUSPENDED, exception.getMessage());
    }

    @Test
    @DisplayName("should not login when account is not verified")
    void shouldNotLoginWhenAccountIsNotVerified() {
        // given
        Authentication authentication = mock(Authentication.class);
        String token = "jwt-token";

        Customer customer = new Customer(
                1L,
                "alice@gmail.com",
                "123456"
        );
        User user = new User(customer);
        customer.getAccount().setAccountStatus(AccountStatus.PENDING_VERIFICATION);

        AuthenticationRequest request = new AuthenticationRequest(customer.getEmail(), customer.getPassword());

        // when
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtUtil.generateToken(anyMap(), anyString())).thenReturn(token);
        when(authentication.getPrincipal()).thenReturn(user);

        AccountNotVerifiedException exception = assertThrows(
                AccountNotVerifiedException.class,
                () -> authenticationService.login(request)
        );

        // Then
        assertEquals(Exceptions.AUTH.ACCOUNT_NOT_VERIFIED, exception.getMessage());
    }
}
