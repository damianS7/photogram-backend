package com.damian.photogram.auth.admin;

import com.damian.photogram.accounts.auth.http.AuthenticationRequest;
import com.damian.photogram.accounts.auth.http.AuthenticationResponse;
import com.damian.photogram.common.utils.JWTUtil;
import com.damian.photogram.customers.Customer;
import com.damian.photogram.customers.CustomerRepository;
import com.damian.photogram.customers.CustomerRole;
import com.damian.photogram.customers.http.request.CustomerPasswordUpdateRequest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class AdminAuthenticationIntegrationTest {
    private static final String RAW_PASSWORD = "123456";
    private final String email = "customers@test.com";
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
    private JWTUtil jwtUtil;
    private Customer customerA;
    private Customer customerB;
    private Customer customerAdmin;
    private String token;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        customerA = new Customer("customerA@test.com", bCryptPasswordEncoder.encode(RAW_PASSWORD));
        customerB = new Customer("customerB@test.com", bCryptPasswordEncoder.encode(RAW_PASSWORD));
        customerAdmin = new Customer("admin@test.com", bCryptPasswordEncoder.encode(RAW_PASSWORD));
        customerAdmin.setRole(CustomerRole.ADMIN);

        customerRepository.save(customerA);
        customerRepository.save(customerB);
        customerRepository.save(customerAdmin);
    }

    void loginWithCustomer(Customer customer) throws Exception {
        // given
        AuthenticationRequest authenticationRequest = new AuthenticationRequest(
                customer.getEmail(), "123456"
        );

        String jsonRequest = objectMapper.writeValueAsString(authenticationRequest);

        // when
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                                          .contentType(MediaType.APPLICATION_JSON)
                                          .content(jsonRequest))
                                  .andReturn();

        AuthenticationResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                AuthenticationResponse.class
        );

        this.token = response.token();
    }

    @Test
    @DisplayName("Should update customers password")
    void shouldUpdateCustomerPassword() throws Exception {
        // given
        loginWithCustomer(customerAdmin);
        CustomerPasswordUpdateRequest updatePasswordRequest = new CustomerPasswordUpdateRequest(
                "123456",
                "12345678$Xa"
        );

        // when
        // then
        mockMvc.perform(MockMvcRequestBuilders
                       .patch("/api/v1/admin/auth/customers/{id}/password", customerA.getId())
                       .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(updatePasswordRequest)))
               .andDo(print())
               .andExpect(status().isOk());
    }
}
