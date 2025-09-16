package com.damian.photogram.service.user.account;

import com.damian.photogram.core.AbstractIntegrationTest;
import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.domain.user.enums.AccountStatus;
import com.damian.photogram.domain.user.enums.CustomerGender;
import com.damian.photogram.domain.user.enums.UserRole;
import com.damian.photogram.domain.user.model.Customer;
import com.damian.photogram.web.user.dto.request.AccountPasswordUpdateRequest;
import com.damian.photogram.web.user.dto.request.AccountRegistrationRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AccountIntegrationTest extends AbstractIntegrationTest {
    private final String email = "customer@test.com";

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
    @DisplayName("Should register customer when request is valid")
    void shouldRegisterCustomerWhenValidRequest() throws Exception {
        // given
        AccountRegistrationRequest request = new AccountRegistrationRequest(
                "david@gmail.com",
                "12345678X$",
                "david",
                "david",
                "white",
                "123 123 123",
                LocalDate.of(1989, 1, 1),
                CustomerGender.MALE
        );

        // request to json
        String json = objectMapper.writeValueAsString(request);

        // when
        mockMvc.perform(MockMvcRequestBuilders
                       .post("/api/v1/accounts/register")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(json))
               .andDo(print())
               .andExpect(MockMvcResultMatchers.status().is(201))
               .andExpect(jsonPath("$.email").value(request.email()))
               .andExpect(jsonPath("$.profile.firstName").value(request.firstName()))
               .andExpect(jsonPath("$.profile.lastName").value(request.lastName()))
               .andExpect(jsonPath("$.profile.phone").value(request.phone()))
               .andExpect(jsonPath("$.profile.birthdate").value(request.birthdate().toString()))
               .andExpect(jsonPath("$.profile.gender").value(request.gender().toString()))
               .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should not register customer when missing fields")
    void shouldNotRegisterCustomerWhenMissingFields() throws Exception {
        // given
        AccountRegistrationRequest request = new AccountRegistrationRequest(
                "david@test.com",
                "123456",
                "david",
                "david",
                "white",
                "123 123 123",
                null,
                CustomerGender.MALE
        );

        // request to json
        String json = objectMapper.writeValueAsString(request);

        // then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/accounts/register")
                                              .contentType(MediaType.APPLICATION_JSON)
                                              .content(json))
               .andDo(print())
               .andExpect(MockMvcResultMatchers.status().is(400))
               .andExpect(jsonPath("$.message").value(Exceptions.COMMON.VALIDATION_FAILED))
               .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should not register customer when email is not well-formed")
    void shouldNotRegisterCustomerWhenEmailIsNotWellFormed() throws Exception {
        // given
        AccountRegistrationRequest request = new AccountRegistrationRequest(
                "badEmail",
                "1234567899X$",
                "david",
                "david",
                "white",
                "123 123 123",
                LocalDate.of(1989, 1, 1),
                CustomerGender.MALE
        );

        // request to json
        String json = objectMapper.writeValueAsString(request);

        // then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/accounts/register")
                                              .contentType(MediaType.APPLICATION_JSON)
                                              .content(json))
               .andDo(print())
               .andExpect(MockMvcResultMatchers.status().is(400))
               .andExpect(jsonPath("$.message").value(Exceptions.COMMON.VALIDATION_FAILED))
               .andExpect(jsonPath("$.errors.email").value(containsString("Email must be a well-formed email address")))
               .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should not register customer when email is taken")
    void shouldNotRegisterCustomerWhenEmailIsTaken() throws Exception {
        AccountRegistrationRequest request = new AccountRegistrationRequest(
                this.email,
                "12345678X$",
                "david",
                "david",
                "white",
                "123 123 123",
                LocalDate.of(1989, 1, 1),
                CustomerGender.MALE
        );

        // request to json
        String json = objectMapper.writeValueAsString(request);

        // when
        mockMvc.perform(MockMvcRequestBuilders
                       .post("/api/v1/accounts/register")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(json))
               .andDo(print())
               .andExpect(MockMvcResultMatchers.status().is(409))
               .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should not register customer when password policy not satisfied")
    void shouldNotRegisterCustomerWhenPasswordPolicyNotSatisfied() throws Exception {
        AccountRegistrationRequest request = new AccountRegistrationRequest(
                this.email,
                "123456",
                "david",
                "david",
                "white",
                "123 123 123",
                LocalDate.of(1989, 1, 1),
                CustomerGender.MALE
        );

        // request to json
        String json = objectMapper.writeValueAsString(request);

        // when
        mockMvc.perform(MockMvcRequestBuilders
                       .post("/api/v1/accounts/register")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(json))
               .andDo(print())
               .andExpect(MockMvcResultMatchers.status().is(400))
               .andExpect(jsonPath("$.message").value(Exceptions.COMMON.VALIDATION_FAILED))
               .andExpect(jsonPath("$.errors.password").value(containsString("Password must be at least")))
               .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should update password")
    void shouldUpdatePassword() throws Exception {
        // given
        loginWithCustomer(customer);

        AccountPasswordUpdateRequest updatePasswordRequest = new AccountPasswordUpdateRequest(
                "123456",
                "12345678$Xa"
        );

        // when
        // then
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/accounts/password")
                                              .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                              .contentType(MediaType.APPLICATION_JSON)
                                              .content(objectMapper.writeValueAsString(updatePasswordRequest)))
               .andDo(print())
               .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("Should update password")
    void shouldNotUpdatePasswordWhenPasswordMismatch() throws Exception {
        // given
        loginWithCustomer(customer);
        AccountPasswordUpdateRequest updatePasswordRequest = new AccountPasswordUpdateRequest(
                "1234564",
                "12345678$Xa"
        );

        // when
        // then
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/accounts/password")
                                              .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                              .contentType(MediaType.APPLICATION_JSON)
                                              .content(objectMapper.writeValueAsString(updatePasswordRequest)))
               .andDo(print())
               .andExpect(MockMvcResultMatchers.status().is(403));
    }

    @Test
    @DisplayName("Should not update password when password policy not satisfied")
    void shouldNotUpdatePasswordWhenPasswordPolicyNotSatisfied() throws Exception {
        // given
        loginWithCustomer(customer);
        AccountPasswordUpdateRequest updatePasswordRequest = new AccountPasswordUpdateRequest(
                "1234564",
                "1234"
        );

        // when
        // then
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/accounts/password")
                                              .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                              .contentType(MediaType.APPLICATION_JSON)
                                              .content(objectMapper.writeValueAsString(updatePasswordRequest)))
               .andDo(print())
               .andExpect(MockMvcResultMatchers.status().is(400))
               .andExpect(jsonPath("$.message").value(Exceptions.COMMON.VALIDATION_FAILED))
               .andExpect(jsonPath("$.errors.newPassword").value(containsString("Password must be at least")))
               .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should not update password when password is null")
    void shouldNotUpdatePasswordWhenPasswordIsNull() throws Exception {
        // given
        loginWithCustomer(customer);
        AccountPasswordUpdateRequest updatePasswordRequest = new AccountPasswordUpdateRequest(
                "1234564",
                null
        );

        // when
        // then
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/accounts/password")
                                              .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                              .contentType(MediaType.APPLICATION_JSON)
                                              .content(objectMapper.writeValueAsString(updatePasswordRequest)))
               .andDo(print())
               .andExpect(MockMvcResultMatchers.status().is(400))
               .andExpect(jsonPath("$.message").value(Exceptions.COMMON.VALIDATION_FAILED))
               .andExpect(jsonPath("$.errors.newPassword").value(containsString("must not be blank")))
               .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }
}
