package com.damian.photogram.domain.account;

import com.damian.photogram.core.service.EmailSenderService;
import com.damian.photogram.domain.account.enums.AccountStatus;
import com.damian.photogram.domain.account.enums.AccountTokenType;
import com.damian.photogram.domain.account.exception.AccountActivationException;
import com.damian.photogram.domain.account.exception.AccountNotFoundException;
import com.damian.photogram.domain.account.model.Account;
import com.damian.photogram.domain.account.model.AccountToken;
import com.damian.photogram.domain.account.repository.AccountRepository;
import com.damian.photogram.domain.account.repository.AccountTokenRepository;
import com.damian.photogram.domain.account.service.AccountActivationService;
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
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Habilita Mockito en JUnit 5
public class AccountActivationServiceTest {

    private final String RAW_PASSWORD = "123456";

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AccountTokenRepository accountTokenRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private EmailSenderService emailSenderService;

    @Mock
    private AccountTokenVerificationService accountTokenVerificationService;

    @InjectMocks
    private AccountActivationService accountActivationService;

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
    @DisplayName("Should activate account")
    void shouldActivateAccount() {
        // given
        Customer customer = new Customer(
                10L,
                "customer@test.com",
                passwordEncoder.encode("123456")
        );

        // set the customer on the context
        //        setUpContext(customer);

        AccountToken activationToken = new AccountToken(customer);
        activationToken.setToken("sdfsidjgfiosdjfi");
        activationToken.setType(AccountTokenType.ACCOUNT_VERIFICATION);

        // when
        when(accountTokenVerificationService.verify(activationToken.getToken())).thenReturn(activationToken);
        when(accountRepository.findByCustomer_Id(customer.getId())).thenReturn(Optional.of(customer.getAccount()));
        when(accountTokenRepository.save(any(AccountToken.class))).thenReturn(activationToken);
        when(accountRepository.save(any(Account.class))).thenReturn(customer.getAccount());
        doNothing().when(emailSenderService).send(anyString(), anyString(), anyString());
        accountActivationService.activate(activationToken.getToken());

        // then
        //        verify(accountRepository, times(1)).save(customer.getAccount());
        assertThat(activationToken.isUsed()).isEqualTo(true);
        assertThat(customer.getAccount().getAccountStatus()).isEqualTo(AccountStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should not activate account when account not exists")
    void shouldNotActivateAccountWhenAccountNotExists() {
        // given
        Customer customer = new Customer(
                10L,
                "customer@test.com",
                passwordEncoder.encode("123456")
        );

        // set the customer on the context
        //        setUpContext(customer);

        AccountToken activationToken = new AccountToken(customer);
        activationToken.setToken("sdfsidjgfiosdjfi");
        activationToken.setType(AccountTokenType.ACCOUNT_VERIFICATION);

        // when
        when(accountTokenVerificationService.verify(activationToken.getToken())).thenReturn(activationToken);
        when(accountRepository.findByCustomer_Id(customer.getId())).thenReturn(Optional.empty());
        assertThrows(
                AccountNotFoundException.class,
                () -> accountActivationService.activate(activationToken.getToken())
        );
    }

    @Test
    @DisplayName("Should not activate account when account is Suspended")
    void shouldNotActivateAccountWhenAccountIsSuspended() {
        // given
        Customer customer = new Customer(
                10L,
                "customer@test.com",
                passwordEncoder.encode("123456")
        );
        customer.getAccount().setAccountStatus(AccountStatus.SUSPENDED);

        AccountToken activationToken = new AccountToken(customer);
        activationToken.setToken("sdfsidjgfiosdjfi");
        activationToken.setType(AccountTokenType.ACCOUNT_VERIFICATION);

        // when
        when(accountTokenVerificationService.verify(activationToken.getToken())).thenReturn(activationToken);
        when(accountRepository.findByCustomer_Id(customer.getId())).thenReturn(Optional.of(customer.getAccount()));
        assertThrows(
                AccountActivationException.class,
                () -> accountActivationService.activate(activationToken.getToken())
        );
    }


    @Test
    @DisplayName("Should not activate account when account is active")
    void shouldNotActivateAccountWhenAccountIsActive() {
        // given
        Customer customer = new Customer(
                10L,
                "customer@test.com",
                passwordEncoder.encode("123456")
        );
        customer.getAccount().setAccountStatus(AccountStatus.ACTIVE);

        AccountToken activationToken = new AccountToken(customer);
        activationToken.setToken("sdfsidjgfiosdjfi");
        activationToken.setType(AccountTokenType.ACCOUNT_VERIFICATION);

        // when
        when(accountTokenVerificationService.verify(activationToken.getToken())).thenReturn(activationToken);
        when(accountRepository.findByCustomer_Id(customer.getId())).thenReturn(Optional.of(customer.getAccount()));
        assertThrows(
                AccountActivationException.class,
                () -> accountActivationService.activate(activationToken.getToken())
        );
    }

}
