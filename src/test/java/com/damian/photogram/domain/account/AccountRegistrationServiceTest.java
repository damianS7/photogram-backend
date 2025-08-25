package com.damian.photogram.domain.account;

import com.damian.photogram.domain.account.dto.request.AccountRegistrationRequest;
import com.damian.photogram.domain.account.model.AccountToken;
import com.damian.photogram.domain.account.service.AccountActivationService;
import com.damian.photogram.domain.account.service.AccountRegistrationService;
import com.damian.photogram.domain.customer.enums.CustomerGender;
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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class) // Habilita Mockito en JUnit 5
public class AccountRegistrationServiceTest {

    private final String RAW_PASSWORD = "123456";

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private AccountRegistrationService accountRegistrationService;

    @Mock
    private CustomerService customerService;

    @Mock
    private AccountActivationService accountActivationService;

    @Mock
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
    @DisplayName("should register a new customer")
    void shouldRegisterCustomer() {
        // given
        Customer givenCustomer = Customer.create()
                                         .setMail("customer@test.com")
                                         .setPassword(passwordEncoder.encode(RAW_PASSWORD))
                                         .setProfile(profile -> profile
                                                 .setFirstName("John")
                                                 .setLastName("Wick")
                                                 .setPhone("123 123 123")
                                                 .setGender(CustomerGender.MALE)
                                                 .setBirthdate(LocalDate.of(1989, 1, 1))
                                                 .setImageFilename("no photoPath")
                                         );

        AccountRegistrationRequest registrationRequest = new AccountRegistrationRequest(
                givenCustomer.getEmail(),
                givenCustomer.getPassword(),
                givenCustomer.getProfile().getUsername(),
                givenCustomer.getProfile().getFirstName(),
                givenCustomer.getProfile().getLastName(),
                givenCustomer.getProfile().getPhone(),
                givenCustomer.getProfile().getBirthdate(),
                givenCustomer.getProfile().getGender()
        );

        AccountToken accountToken = AccountToken.create()
                                                .setCustomer(givenCustomer);

        // when
        when(accountActivationService.createAccountActivationToken(anyString())).thenReturn(accountToken);
        when(customerService.createCustomer(any(AccountRegistrationRequest.class))).thenReturn(givenCustomer);

        Customer registeredCustomer = accountRegistrationService.register(registrationRequest);

        // then
        assertThat(registeredCustomer).isNotNull();
        assertThat(registeredCustomer.getEmail()).isEqualTo(givenCustomer.getEmail());
        assertThat(registeredCustomer.getProfile().getFirstName()).isEqualTo(givenCustomer.getProfile().getFirstName());
        assertThat(registeredCustomer.getProfile().getLastName()).isEqualTo(givenCustomer.getProfile().getLastName());
        assertThat(registeredCustomer.getProfile().getPhone()).isEqualTo(givenCustomer.getProfile().getPhone());
        assertThat(registeredCustomer.getProfile().getGender()).isEqualTo(givenCustomer.getProfile().getGender());
        assertThat(registeredCustomer.getProfile().getBirthdate()).isEqualTo(givenCustomer.getProfile().getBirthdate());
    }
}
