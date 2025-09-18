package com.damian.photogram.service.auth;

import com.damian.photogram.core.AbstractIntegrationTest;
import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.core.util.ApiResponse;
import com.damian.photogram.core.util.CommonHelper;
import com.damian.photogram.domain.user.enums.AccountStatus;
import com.damian.photogram.domain.user.enums.CustomerGender;
import com.damian.photogram.domain.user.enums.UserRole;
import com.damian.photogram.domain.user.model.Customer;
import com.damian.photogram.web.auth.dto.AuthenticationRequest;
import com.damian.photogram.web.auth.dto.AuthenticationResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AuthenticationIntegrationTest extends AbstractIntegrationTest {
    private Customer customer;

    @BeforeAll
    void setUp() {
        customer = Customer.create()
                           .setEmail("customer@test.com")
                           .setPassword(bCryptPasswordEncoder.encode(this.RAW_PASSWORD))
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
    @DisplayName("Should login when valid credentials")
    void shouldLoginWhenValidCredentials() throws Exception {
        // given
        AuthenticationRequest request = new AuthenticationRequest(
                customer.getEmail(),
                this.RAW_PASSWORD
        );

        // when
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                                          .post("/api/v1/auth/login")
                                          .contentType(MediaType.APPLICATION_JSON)
                                          .content(CommonHelper.toJson(request)))
                                  .andDo(print())
                                  .andExpect(MockMvcResultMatchers.status().is(200))
                                  .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                                  .andReturn();

        // json to AuthenticationResponse
        AuthenticationResponse response = CommonHelper.fromJson(
                result.getResponse().getContentAsString(),
                AuthenticationResponse.class
        );

        // then
        assertThat(jwtUtil.extractEmail(response.token())).isEqualTo(customer.getEmail());
        assertTrue(jwtUtil.isTokenValid(response.token()));
    }

    @Test
    @DisplayName("Should not login when invalid credentials")
    void shouldNotLoginWhenInvalidCredentials() throws Exception {
        // given
        AuthenticationRequest request = new AuthenticationRequest(
                customer.getEmail(),
                "badPassword"
        );

        // when
        mockMvc.perform(MockMvcRequestBuilders
                       .post("/api/v1/auth/login")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(CommonHelper.toJson(request)))
               .andDo(print())
               .andExpect(MockMvcResultMatchers.status().is(401))
               .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should not login when email not exist")
    void shouldNotLoginWhenEmailNotExist() throws Exception {
        // given
        AuthenticationRequest request = new AuthenticationRequest(
                "nonemail@demo.com",
                "123456"
        );

        // when
        MvcResult result = mockMvc
                .perform(MockMvcRequestBuilders
                        .post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CommonHelper.toJson(request)))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().is(401))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // json to ApiResponse
        ApiResponse<?> response = CommonHelper.fromJson(
                result.getResponse().getContentAsString(),
                new TypeReference<ApiResponse<?>>() {
                }
        );

        // then
        assertEquals(Exceptions.ACCOUNT.BAD_CREDENTIALS, response.getMessage());
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

        // when
        MvcResult result = mockMvc
                .perform(MockMvcRequestBuilders
                        .post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CommonHelper.toJson(request)))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().is(403))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // json to ApiResponse
        ApiResponse<?> response = CommonHelper.fromJson(
                result.getResponse().getContentAsString(),
                new TypeReference<ApiResponse<?>>() {
                }
        );

        // then
        assertEquals(Exceptions.ACCOUNT.SUSPENDED, response.getMessage());

        // undo changes
        givenCustomer.getAccount().setAccountStatus(AccountStatus.VERIFIED);
        customerRepository.save(givenCustomer);
    }

    @Test
    @DisplayName("Should not login when invalid email format")
    void shouldNotLoginWhenInvalidEmailFormat() throws Exception {
        // Given
        AuthenticationRequest request = new AuthenticationRequest(
                "thisIsNotAnEmail",
                "123456"
        );

        // when
        MvcResult result = mockMvc
                .perform(MockMvcRequestBuilders
                        .post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CommonHelper.toJson(request)))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().is(400))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // json to ApiResponse
        ApiResponse<?> response = CommonHelper.fromJson(
                result.getResponse().getContentAsString(),
                new TypeReference<ApiResponse<?>>() {
                }
        );

        // then
        assertThat(response.getErrors().get("email"))
                .asString()
                .contains("must be a well-formed email address");
        assertEquals(Exceptions.COMMON.VALIDATION_FAILED, response.getMessage());
    }

    @Test
    @DisplayName("Should not login when null fields")
    void shouldNotLoginWhenNullFields() throws Exception {
        // Given
        AuthenticationRequest request = new AuthenticationRequest(
                null,
                null
        );

        // when
        MvcResult result = mockMvc
                .perform(MockMvcRequestBuilders
                        .post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CommonHelper.toJson(request)))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().is(400))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // json to ApiResponse
        ApiResponse<?> response = CommonHelper.fromJson(
                result.getResponse().getContentAsString(),
                new TypeReference<ApiResponse<?>>() {
                }
        );

        // then
        assertThat(response.getErrors().get("password"))
                .asString()
                .contains("must not be blank");

        assertThat(response.getErrors().get("email"))
                .asString()
                .contains("must not be blank");

        assertEquals(Exceptions.COMMON.VALIDATION_FAILED, response.getMessage());
    }

    @Test
    @DisplayName("Should not login when account is not activated")
    void shouldNotLoginWhenAccountIsNotVerified() throws Exception {
        // given
        customer.getAccount().setAccountStatus(AccountStatus.PENDING_VERIFICATION);
        customerRepository.save(customer);

        AuthenticationRequest request = new AuthenticationRequest(
                customer.getEmail(),
                RAW_PASSWORD
        );

        // when
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                                          .post("/api/v1/auth/login")
                                          .contentType(MediaType.APPLICATION_JSON)
                                          .content(CommonHelper.toJson(request)))
                                  .andDo(print())
                                  .andExpect(MockMvcResultMatchers.status().is(403))
                                  .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                                  .andReturn();

        // json to ApiResponse
        ApiResponse<?> response = CommonHelper.fromJson(
                result.getResponse().getContentAsString(),
                new TypeReference<ApiResponse<?>>() {
                }
        );

        assertEquals(Exceptions.ACCOUNT.NOT_VERIFIED, response.getMessage());

        // undo changes to customer
        customer.getAccount().setAccountStatus(AccountStatus.VERIFIED);
        customerRepository.save(customer);
    }

}
