package com.damian.photogram.service.auth;

import com.damian.photogram.core.AbstractIntegrationTest;
import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.core.util.JwtUtil;
import com.damian.photogram.domain.user.enums.AccountStatus;
import com.damian.photogram.domain.user.enums.CustomerGender;
import com.damian.photogram.domain.user.enums.UserRole;
import com.damian.photogram.domain.user.model.Customer;
import com.damian.photogram.web.rest.user.dto.request.ProfileUpdateRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AuthorizationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private JwtUtil jwtUtil;

    private Customer customer;
    private Customer admin;

    @BeforeAll
    void setUp() {
        customer = Customer.create()
                           .setEmail("customer@demo.com")
                           .setPassword(bCryptPasswordEncoder.encode(this.RAW_PASSWORD))
                           .setRole(UserRole.CUSTOMER)
                           .setProfile(profile -> profile
                                   .setFirstName("John")
                                   .setLastName("Wick")
                                   .setGender(CustomerGender.MALE)
                                   .setBirthdate(LocalDate.of(1989, 1, 1))
                                   .setImageFilename("images/avatar.jpg")
                           );
        customer.getAccount().setAccountStatus(AccountStatus.VERIFIED);
        customerRepository.save(customer);

        admin = Customer.create()
                        .setEmail("admin@test.com")
                        .setPassword(bCryptPasswordEncoder.encode(RAW_PASSWORD))
                        .setRole(UserRole.ADMIN);
        admin.getAccount().setAccountStatus(AccountStatus.VERIFIED);
        customerRepository.save(admin);
    }

    @Test
    @DisplayName("Should have access when token is valid")
    void shouldHaveAccessWhenTokenIsValid() throws Exception {
        // given
        final String givenToken = jwtUtil.generateToken(
                customer.getEmail(),
                new Date(System.currentTimeMillis() + 1000 * 60 * 60)
        );

        // when
        mockMvc.perform(MockMvcRequestBuilders
                       .get("/api/v1/customers/profile")
                       .header(HttpHeaders.AUTHORIZATION, "Bearer " + givenToken))
               .andDo(print())
               .andExpect(MockMvcResultMatchers.status().is(200))
               .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should not have access when not authenticated")
    void shouldNotHaveAccessWhenNotAuthenticated() throws Exception {
        // when
        mockMvc.perform(MockMvcRequestBuilders
                       .get("/api/v1/customers/profile"))
               .andDo(print())
               .andExpect(MockMvcResultMatchers.status().is(401))
               .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should not have access when token has expired")
    void shouldNotHaveAccessWhenTokenHasExpired() throws Exception {
        // given
        final String expiredToken = jwtUtil.generateToken(
                customer.getEmail(),
                new Date(System.currentTimeMillis() - 1000 * 60 * 60)
        );

        // given
        Map<String, Object> fields = new HashMap<>();
        fields.put("firstName", "alice");

        ProfileUpdateRequest request = new ProfileUpdateRequest(
                this.RAW_PASSWORD,
                fields
        );

        String jsonRequest = objectMapper.writeValueAsString(request);

        // when
        mockMvc.perform(MockMvcRequestBuilders
                       .put("/api/v1/profiles/" + customer.getProfile().getId())
                       .contentType(MediaType.APPLICATION_JSON)
                       .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken)
                       .content(jsonRequest))
               .andDo(print())
               .andExpect(MockMvcResultMatchers.status().is(401))
               .andExpect(jsonPath("$.message").value(Exceptions.JWT.TOKEN.EXPIRED))
               .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should not have access when token is invalid")
    void shouldNotHaveAccessWhenTokenIsInvalid() throws Exception {
        // given
        final String invalidToken = "bad-token";

        // given
        Map<String, Object> fields = new HashMap<>();
        fields.put("firstName", "alice");

        ProfileUpdateRequest request = new ProfileUpdateRequest(
                this.RAW_PASSWORD,
                fields
        );

        String jsonRequest = objectMapper.writeValueAsString(request);

        // when
        mockMvc.perform(MockMvcRequestBuilders
                       .put("/api/v1/profiles/" + customer.getProfile().getId())
                       .contentType(MediaType.APPLICATION_JSON)
                       .header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidToken)
                       .content(jsonRequest))
               .andDo(print())
               .andExpect(MockMvcResultMatchers.status().is(401))
               .andExpect(jsonPath("$.message").value(Exceptions.JWT.TOKEN.INVALID))
               .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should not have access when token email not exists")
    void shouldNotHaveAccessWhenTokenEmailNotExists() throws Exception {
        // given
        final String token = jwtUtil.generateToken(
                "fake-email@demo.com",
                new Date(System.currentTimeMillis() + 1000 * 60 * 60)
        );

        // when
        mockMvc.perform(MockMvcRequestBuilders
                       .get("/api/v1/customers/me/profile")
                       .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
               .andDo(print())
               .andExpect(MockMvcResultMatchers.status().is(401))
               .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }
}
