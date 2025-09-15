package com.damian.photogram.app.auth;

import com.damian.photogram.AbstractIntegrationTest;
import com.damian.photogram.app.auth.dto.AuthenticationRequest;
import com.damian.photogram.app.auth.dto.AuthenticationResponse;
import com.damian.photogram.core.common.JwtUtil;
import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.domain.user.account.enums.AccountStatus;
import com.damian.photogram.domain.user.customer.enums.CustomerGender;
import com.damian.photogram.domain.user.customer.enums.UserRole;
import com.damian.photogram.domain.user.customer.model.Customer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AuthenticationIntegrationTest extends AbstractIntegrationTest {
    private final String email = "customer@test.com";

    @Autowired
    private JwtUtil jwtUtil;

    private Customer customer;

    @BeforeAll
    void setUp() {
        customer = new Customer();
        customer.setRole(UserRole.ADMIN);
        customer.setEmail(this.email);
        customer.setPassword(bCryptPasswordEncoder.encode(this.RAW_PASSWORD));
        customer.getAccount().setAccountStatus(AccountStatus.VERIFIED);
        customer.getProfile().setFirstName("John");
        customer.getProfile().setLastName("Wick");
        customer.getProfile().setPhone("123 123 123");
        customer.getProfile().setGender(CustomerGender.MALE);
        customer.getProfile().setBirthdate(LocalDate.of(1989, 1, 1));
        customer.getProfile().setImageFilename("no photoPath");

        customerRepository.save(customer);
    }

    @Test
    @DisplayName("Should login when valid credentials")
    void shouldLoginWhenValidCredentials() throws Exception {
        // given
        customer.getAccount().setAccountStatus(AccountStatus.VERIFIED);
        customerRepository.save(customer);

        AuthenticationRequest request = new AuthenticationRequest(
                this.email, this.RAW_PASSWORD
        );

        // request to json
        String jsonRequest = objectMapper.writeValueAsString(request);

        // when
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                                          .post("/api/v1/auth/login")
                                          .contentType(MediaType.APPLICATION_JSON)
                                          .content(jsonRequest))
                                  .andDo(print())
                                  .andExpect(MockMvcResultMatchers.status().is(200))
                                  .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                                  .andReturn();

        // json to AuthenticationResponse
        AuthenticationResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                AuthenticationResponse.class
        );

        // then
        final String emailFromToken = jwtUtil.extractEmail(response.token());
        assertThat(emailFromToken).isEqualTo(this.email);
    }

    @Test
    @DisplayName("Should not login when invalid credentials")
    void shouldNotLoginWhenInvalidCredentials() throws Exception {
        // given
        AuthenticationRequest request = new AuthenticationRequest(
                this.email, "badPassword"
        );

        // request to json
        String jsonRequest = objectMapper.writeValueAsString(request);

        // when
        mockMvc.perform(MockMvcRequestBuilders
                       .post("/api/v1/auth/login")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(jsonRequest))
               .andDo(print())
               .andExpect(MockMvcResultMatchers.status().is(401))
               .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should not login when email not exist")
    void shouldNotLoginWhenEmailNotExist() throws Exception {
        // given
        AuthenticationRequest request = new AuthenticationRequest(
                "nonemail@demo.com", "123456"
        );

        // request to json
        String jsonRequest = objectMapper.writeValueAsString(request);

        // when
        mockMvc.perform(MockMvcRequestBuilders
                       .post("/api/v1/auth/login")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(jsonRequest))
               .andDo(print())
               .andExpect(MockMvcResultMatchers.status().is(401))
               .andExpect(jsonPath("$.message").value(Exceptions.ACCOUNT.BAD_CREDENTIALS))
               .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should not login when account is disabled")
    void shouldNotLoginWhenAccountIsSuspended() throws Exception {
        // given
        Customer givenCustomer = new Customer();
        givenCustomer.setEmail("disabled-customer@test.com");
        givenCustomer.setPassword(bCryptPasswordEncoder.encode(this.RAW_PASSWORD));
        givenCustomer.getAccount().setAccountStatus(AccountStatus.SUSPENDED);

        customerRepository.save(givenCustomer);

        AuthenticationRequest request = new AuthenticationRequest(
                givenCustomer.getEmail(), "123456"
        );

        String jsonRequest = objectMapper.writeValueAsString(request);

        // when
        mockMvc.perform(MockMvcRequestBuilders
                       .post("/api/v1/auth/login")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(jsonRequest))
               .andDo(print())
               .andExpect(MockMvcResultMatchers.status().is(403))
               .andExpect(jsonPath("$.message").value(Exceptions.ACCOUNT.SUSPENDED))
               .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should not login when invalid email format")
    void shouldNotLoginWhenInvalidEmailFormat() throws Exception {
        // Given
        AuthenticationRequest request = new AuthenticationRequest(
                "thisIsNotAnEmail", "123456"
        );

        // request to json
        String jsonRequest = objectMapper.writeValueAsString(request);

        // when
        mockMvc.perform(MockMvcRequestBuilders
                       .post("/api/v1/auth/login")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(jsonRequest))
               .andDo(print())
               .andExpect(MockMvcResultMatchers.status().is(400))
               .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$.errors.email").value(containsString("must be a well-formed email address")))
               .andExpect(jsonPath("$.message").value(Exceptions.COMMON.VALIDATION_FAILED));
    }

    @Test
    @DisplayName("Should not login when null fields")
    void shouldNotLoginWhenNullFields() throws Exception {
        // Given
        AuthenticationRequest request = new AuthenticationRequest(
                null, null
        );

        // request to json
        String jsonRequest = objectMapper.writeValueAsString(request);

        // when
        mockMvc.perform(MockMvcRequestBuilders
                       .post("/api/v1/auth/login")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(jsonRequest))
               .andDo(print())
               .andExpect(MockMvcResultMatchers.status().is(400))
               .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$.message").value(Exceptions.COMMON.VALIDATION_FAILED));
    }

    @Test
    @DisplayName("Should not login when account is not activated")
    void shouldNotLoginWhenAccountIsNotVerified() throws Exception {
        // Given
        customer.getAccount().setAccountStatus(AccountStatus.PENDING_VERIFICATION);
        customerRepository.save(customer);

        AuthenticationRequest request = new AuthenticationRequest(
                email, RAW_PASSWORD
        );

        // request to json
        String jsonRequest = objectMapper.writeValueAsString(request);

        // when
        mockMvc.perform(MockMvcRequestBuilders
                       .post("/api/v1/auth/login")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(jsonRequest))
               .andDo(print())
               .andExpect(MockMvcResultMatchers.status().is(403))
               .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

}
