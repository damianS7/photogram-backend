package com.damian.photogram.app.auth;

import com.damian.photogram.AbstractIntegrationTest;
import com.damian.photogram.app.user.UserRole;
import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.core.utils.JwtUtil;
import com.damian.photogram.domain.customer.dto.request.ProfileUpdateRequest;
import com.damian.photogram.domain.customer.enums.CustomerGender;
import com.damian.photogram.domain.customer.model.Customer;
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
        customer = new Customer();
        customer.setEmail("customer@test.com");
        customer.setPassword(bCryptPasswordEncoder.encode(RAW_PASSWORD));
        customer.getProfile().setFirstName("John");
        customer.getProfile().setLastName("Wick");
        customer.getProfile().setPhone("123 123 123");
        customer.getProfile().setGender(CustomerGender.MALE);
        customer.getProfile().setBirthdate(LocalDate.of(1989, 1, 1));
        customer.getProfile().setImageFilename("no photoPath");

        customerRepository.save(customer);

        admin = new Customer();
        admin.setEmail("admin@test.com");
        admin.setPassword(bCryptPasswordEncoder.encode(RAW_PASSWORD));
        admin.setRole(UserRole.ADMIN);

        customerRepository.save(admin);
    }

    @Test
    @DisplayName("Should have access when token is valid")
    void shouldHaveAccessWhenTokenIsValid() throws Exception {
        // given
        final String token = jwtUtil.generateToken(
                customer.getEmail(),
                new Date(System.currentTimeMillis() + 1000 * 60 * 60)
        );

        // when
        mockMvc.perform(MockMvcRequestBuilders
                       .get("/api/v1/customers/profile")
                       .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
               .andDo(print())
               .andExpect(MockMvcResultMatchers.status().is(200))
               .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should not have access when not authenticated")
    void shouldNotHaveAccessWhenNotAuthenticated() throws Exception {
        // given

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
        fields.put("lastName", "white");
        fields.put("phone", "999 999 999");
        fields.put("birthdate", LocalDate.of(1989, 1, 1));
        fields.put("gender", CustomerGender.FEMALE);

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
               .andExpect(jsonPath("$.message").value(Exceptions.JWT.TOKEN_EXPIRED))
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
        fields.put("lastName", "white");
        fields.put("phone", "999 999 999");
        fields.put("birthdate", LocalDate.of(1989, 1, 1));
        fields.put("gender", CustomerGender.FEMALE);

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
               .andExpect(jsonPath("$.message").value(Exceptions.JWT.INVALID_TOKEN))
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
