package com.damian.photogram.domain.account;

import com.damian.photogram.app.auth.dto.AuthenticationRequest;
import com.damian.photogram.app.auth.dto.AuthenticationResponse;
import com.damian.photogram.core.utils.JwtUtil;
import com.damian.photogram.domain.customer.dto.request.CustomerPasswordUpdateRequest;
import com.damian.photogram.domain.customer.dto.request.CustomerRegistrationRequest;
import com.damian.photogram.domain.customer.enums.CustomerGender;
import com.damian.photogram.domain.customer.enums.CustomerRole;
import com.damian.photogram.domain.customer.model.Customer;
import com.damian.photogram.domain.customer.repository.CustomerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class AccountIntegrationTest {
    private final String email = "customer@test.com";
    private final String rawPassword = "123456";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();
        customer = new Customer();
        customer.setRole(CustomerRole.ADMIN);
        customer.setEmail(this.email);
        customer.setPassword(bCryptPasswordEncoder.encode(this.rawPassword));
        customer.getProfile().setFirstName("John");
        customer.getProfile().setLastName("Wick");
        customer.getProfile().setPhone("123 123 123");
        customer.getProfile().setGender(CustomerGender.MALE);
        customer.getProfile().setBirthdate(LocalDate.of(1989, 1, 1));
        customer.getProfile().setImageFilename("no photoPath");

        customerRepository.save(customer);
    }

    String loginWithCustomer(Customer customer) throws Exception {
        // given
        AuthenticationRequest authenticationRequest = new AuthenticationRequest(
                customer.getEmail(), "123456"
        );

        String jsonRequest = objectMapper.writeValueAsString(authenticationRequest);

        // when
        MvcResult result = mockMvc.perform(post("/api/v1/security/login")
                                          .contentType(MediaType.APPLICATION_JSON)
                                          .content(jsonRequest))
                                  .andReturn();

        AuthenticationResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                AuthenticationResponse.class
        );

        return response.token();
    }

    @Test
    @DisplayName("Should register customer when request is valid")
    void shouldRegisterCustomerWhenValidRequest() throws Exception {
        // given
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
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
                       .post("/api/v1/security/register")
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
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
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
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/security/register")
                                              .contentType(MediaType.APPLICATION_JSON)
                                              .content(json))
               .andDo(print())
               .andExpect(MockMvcResultMatchers.status().is(400))
               .andExpect(jsonPath("$.message").value(containsString("Validation error")))
               .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should not register customer when email is not well-formed")
    void shouldNotRegisterCustomerWhenEmailIsNotWellFormed() throws Exception {
        // given
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
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
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/security/register")
                                              .contentType(MediaType.APPLICATION_JSON)
                                              .content(json))
               .andDo(print())
               .andExpect(MockMvcResultMatchers.status().is(400))
               .andExpect(jsonPath("$.message").value("Validation error"))
               .andExpect(jsonPath("$.errors.email").value(containsString("Email must be a well-formed email address")))
               .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should not register customer when email is taken")
    void shouldNotRegisterCustomerWhenEmailIsTaken() throws Exception {
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
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
                       .post("/api/v1/security/register")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(json))
               .andDo(print())
               .andExpect(MockMvcResultMatchers.status().is(409))
               .andExpect(jsonPath("$.message").value(containsString("is already taken.")))
               .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should not register customer when password policy not satisfied")
    void shouldNotRegisterCustomerWhenPasswordPolicyNotSatisfied() throws Exception {
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
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
                       .post("/api/v1/security/register")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(json))
               .andDo(print())
               .andExpect(MockMvcResultMatchers.status().is(400))
               .andExpect(jsonPath("$.message").value("Validation error"))
               .andExpect(jsonPath("$.errors.password").value(containsString("Password must be at least")))
               .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should update password")
    void shouldUpdatePassword() throws Exception {
        // given
        String token = loginWithCustomer(customer);
        CustomerPasswordUpdateRequest updatePasswordRequest = new CustomerPasswordUpdateRequest(
                "123456",
                "12345678$Xa"
        );

        // when
        // then
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/security/customer/me/password")
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
        String token = loginWithCustomer(customer);
        CustomerPasswordUpdateRequest updatePasswordRequest = new CustomerPasswordUpdateRequest(
                "1234564",
                "12345678$Xa"
        );

        // when
        // then
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/security/customer/me/password")
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
        String token = loginWithCustomer(customer);
        CustomerPasswordUpdateRequest updatePasswordRequest = new CustomerPasswordUpdateRequest(
                "1234564",
                "1234"
        );

        // when
        // then
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/security/customer/me/password")
                                              .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                              .contentType(MediaType.APPLICATION_JSON)
                                              .content(objectMapper.writeValueAsString(updatePasswordRequest)))
               .andDo(print())
               .andExpect(MockMvcResultMatchers.status().is(400))
               .andExpect(jsonPath("$.message").value("Validation error"))
               .andExpect(jsonPath("$.errors.newPassword").value(containsString("Password must be at least")))
               .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should not update password when password is null")
    void shouldNotUpdatePasswordWhenPasswordIsNull() throws Exception {
        // given
        String token = loginWithCustomer(customer);
        CustomerPasswordUpdateRequest updatePasswordRequest = new CustomerPasswordUpdateRequest(
                "1234564",
                null
        );

        // when
        // then
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/security/customer/me/password")
                                              .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                              .contentType(MediaType.APPLICATION_JSON)
                                              .content(objectMapper.writeValueAsString(updatePasswordRequest)))
               .andDo(print())
               .andExpect(MockMvcResultMatchers.status().is(400))
               .andExpect(jsonPath("$.message").value("Validation error"))
               .andExpect(jsonPath("$.errors.newPassword").value(containsString("must not be blank")))
               .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }
}
