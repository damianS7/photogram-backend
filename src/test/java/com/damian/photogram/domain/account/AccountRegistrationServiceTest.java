package com.damian.photogram.domain.account;

import com.damian.photogram.domain.account.repository.AccountRepository;
import com.damian.photogram.domain.account.repository.AccountTokenRepository;
import com.damian.photogram.domain.account.service.AccountActivationService;
import com.damian.photogram.domain.account.service.AccountRegistrationService;
import com.damian.photogram.domain.customer.dto.request.CustomerRegistrationRequest;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Habilita Mockito en JUnit 5
public class AccountRegistrationServiceTest {

    private final String RAW_PASSWORD = "123456";

    @Mock
    private AccountTokenRepository accountTokenRepository;

    @Mock
    private AccountRepository accountRepository;

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
        customerRepository.deleteAll();
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("should register a new customer")
    void shouldRegisterCustomer() {
        // given
        Customer givenCustomer = new Customer();
        givenCustomer.setEmail("customer@test.com");
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
                givenCustomer.getProfile().getUsername(),
                givenCustomer.getProfile().getFirstName(),
                givenCustomer.getProfile().getLastName(),
                givenCustomer.getProfile().getPhone(),
                givenCustomer.getProfile().getBirthdate(),
                givenCustomer.getProfile().getGender()
        );

        // when
        doNothing().when(accountActivationService).sendAccountActivationToken(anyString());

        when(customerService.createCustomer(any(CustomerRegistrationRequest.class))).thenReturn(givenCustomer);
        //        when(accountRepository.findByCustomer_Email(anyString())).thenReturn(Optional.of(givenCustomer.getAccount()));
        //        when(accountTokenRepository.findByCustomer_Id(anyLong())).thenReturn(Optional.of(accountToken));

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
