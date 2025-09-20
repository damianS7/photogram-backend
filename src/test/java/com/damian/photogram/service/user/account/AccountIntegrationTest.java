package com.damian.photogram.service.user.account;

import com.damian.photogram.core.AbstractIntegrationTest;
import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.core.util.ApiResponse;
import com.damian.photogram.core.util.JsonHelper;
import com.damian.photogram.domain.user.enums.AccountStatus;
import com.damian.photogram.domain.user.enums.CustomerGender;
import com.damian.photogram.domain.user.enums.UserRole;
import com.damian.photogram.domain.user.model.Customer;
import com.damian.photogram.web.rest.user.dto.request.AccountPasswordUpdateRequest;
import com.damian.photogram.web.rest.user.dto.request.AccountRegistrationRequest;
import com.damian.photogram.web.rest.user.dto.response.CustomerWithProfileDto;
import com.damian.photogram.web.rest.user.dto.response.ProfileDto;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AccountIntegrationTest extends AbstractIntegrationTest {
    private Customer customer;

    @BeforeAll
    void setUp() {
        customer = Customer.create()
                           .setEmail("customer@test.com")
                           .setPassword(passwordEncoder.encode(this.RAW_PASSWORD))
                           .setRole(UserRole.ADMIN)
                           .setProfile(profile -> profile
                                   .setFirstName("John")
                                   .setLastName("Wick")
                                   .setGender(CustomerGender.MALE)
                                   .setBirthdate(LocalDate.of(1989, 1, 1))
                                   .setImageFilename("images/avatar.jpg")
                           );
        customer.getAccount().setAccountStatus(AccountStatus.VERIFIED);
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

        // when
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                                          .post("/api/v1/accounts/register")
                                          .contentType(MediaType.APPLICATION_JSON)
                                          .content(JsonHelper.toJson(request)))
                                  .andDo(print())
                                  .andExpect(MockMvcResultMatchers.status().is(HttpStatus.CREATED.value()))
                                  .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                                  .andReturn();

        // then
        CustomerWithProfileDto customerDto = JsonHelper.fromJson(
                result.getResponse().getContentAsString(),
                CustomerWithProfileDto.class
        );

        // then
        assertThat(customerDto)
                .isNotNull()
                .extracting(
                        CustomerWithProfileDto::email
                ).isEqualTo(
                        request.email()
                );

        assertThat(customerDto.profile())
                .isNotNull()
                .extracting(
                        ProfileDto::firstName,
                        ProfileDto::lastName,
                        ProfileDto::username,
                        ProfileDto::phone,
                        ProfileDto::birthdate,
                        ProfileDto::gender
                ).containsExactly(
                        request.firstName(),
                        request.lastName(),
                        request.username(),
                        request.phone(),
                        request.birthdate(),
                        request.gender()
                );
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

        // then
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/accounts/register")
                                                                 .contentType(MediaType.APPLICATION_JSON)
                                                                 .content(JsonHelper.toJson(request)))
                                  .andDo(print())
                                  .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                                  .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                                  .andReturn();

        // then
        ApiResponse<?> response = JsonHelper.fromJson(
                result.getResponse().getContentAsString(),
                new TypeReference<ApiResponse<?>>() {
                }
        );

        // then
        assertThat(response)
                .isNotNull()
                .extracting(ApiResponse::getMessage)
                .asString()
                .isEqualTo(Exceptions.COMMON.VALIDATION_FAILED);
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

        // then
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/accounts/register")
                                                                 .contentType(MediaType.APPLICATION_JSON)
                                                                 .content(JsonHelper.toJson(request)))
                                  .andDo(print())
                                  .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                                  //                                  .andExpect(jsonPath("$.errors.email").value(containsString(
                                  //                                          "Email must be a well-formed email address")))
                                  .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                                  .andReturn();

        // then
        ApiResponse<?> response = JsonHelper.fromJson(
                result.getResponse().getContentAsString(),
                new TypeReference<ApiResponse<?>>() {
                }
        );

        // then
        assertThat(response)
                .isNotNull()
                .extracting(
                        ApiResponse::getMessage
                ).isEqualTo(
                        Exceptions.COMMON.VALIDATION_FAILED
                );

        assertThat(response.getErrors().get("email"))
                .contains("must be a well-formed email address");
    }

    @Test
    @DisplayName("Should not register customer when email is taken")
    void shouldNotRegisterCustomerWhenEmailIsTaken() throws Exception {
        AccountRegistrationRequest request = new AccountRegistrationRequest(
                customer.getEmail(),
                "12345678X$",
                "david",
                "david",
                "white",
                "123 123 123",
                LocalDate.of(1989, 1, 1),
                CustomerGender.MALE
        );

        // when
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                                          .post("/api/v1/accounts/register")
                                          .contentType(MediaType.APPLICATION_JSON)
                                          .content(JsonHelper.toJson(request)))
                                  .andDo(print())
                                  .andExpect(MockMvcResultMatchers.status().is(HttpStatus.CONFLICT.value()))
                                  .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                                  .andReturn();

        // then
        ApiResponse<?> response = JsonHelper.fromJson(
                result.getResponse().getContentAsString(),
                new TypeReference<ApiResponse<?>>() {
                }
        );

        // then
        assertThat(response)
                .isNotNull()
                .extracting(ApiResponse::getMessage)
                .asString()
                .isEqualTo(Exceptions.CUSTOMER.EMAIL_TAKEN);
    }

    @Test
    @DisplayName("Should not register customer when password policy not satisfied")
    void shouldNotRegisterCustomerWhenPasswordPolicyNotSatisfied() throws Exception {
        AccountRegistrationRequest request = new AccountRegistrationRequest(
                "customer@demo.com",
                "123456",
                "david",
                "david",
                "white",
                "123 123 123",
                LocalDate.of(1989, 1, 1),
                CustomerGender.MALE
        );

        // when
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                                          .post("/api/v1/accounts/register")
                                          .contentType(MediaType.APPLICATION_JSON)
                                          .content(JsonHelper.toJson(request)))
                                  .andDo(print())
                                  .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                                  .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                                  .andReturn();

        // then
        ApiResponse<?> response = JsonHelper.fromJson(
                result.getResponse().getContentAsString(),
                new TypeReference<ApiResponse<?>>() {
                }
        );

        // then
        assertThat(response)
                .isNotNull()
                .extracting(
                        ApiResponse::getMessage
                ).isEqualTo(
                        Exceptions.COMMON.VALIDATION_FAILED
                );

        assertThat(response.getErrors().get("password"))
                .containsIgnoringCase("password must be at least");
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
                                              .content(JsonHelper.toJson(updatePasswordRequest)))
               .andDo(print())
               .andExpect(MockMvcResultMatchers.status().is(HttpStatus.OK.value()));
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
                                              .content(JsonHelper.toJson(updatePasswordRequest)))
               .andDo(print())
               .andExpect(MockMvcResultMatchers.status().is(HttpStatus.FORBIDDEN.value()));
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
        MvcResult result = mockMvc
                .perform(MockMvcRequestBuilders.patch("/api/v1/accounts/password")
                                               .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                               .contentType(MediaType.APPLICATION_JSON)
                                               .content(JsonHelper.toJson(updatePasswordRequest)))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        ApiResponse<?> response = JsonHelper.fromJson(
                result.getResponse().getContentAsString(),
                new TypeReference<ApiResponse<?>>() {
                }
        );

        // then
        assertThat(response)
                .isNotNull()
                .extracting(
                        ApiResponse::getMessage
                ).isEqualTo(
                        Exceptions.COMMON.VALIDATION_FAILED
                );

        assertThat(response.getErrors().get("newPassword"))
                .containsIgnoringCase("password must be at least");


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
        MvcResult result = mockMvc
                .perform(MockMvcRequestBuilders.patch("/api/v1/accounts/password")
                                               .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                               .contentType(MediaType.APPLICATION_JSON)
                                               .content(JsonHelper.toJson(updatePasswordRequest)))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();


        ApiResponse<?> response = JsonHelper.fromJson(
                result.getResponse().getContentAsString(),
                new TypeReference<ApiResponse<?>>() {
                }
        );

        // then
        assertThat(response)
                .isNotNull()
                .extracting(
                        ApiResponse::getMessage
                ).isEqualTo(
                        Exceptions.COMMON.VALIDATION_FAILED
                );

        assertThat(response.getErrors().get("newPassword"))
                .containsIgnoringCase("must not be blank");
    }
}
