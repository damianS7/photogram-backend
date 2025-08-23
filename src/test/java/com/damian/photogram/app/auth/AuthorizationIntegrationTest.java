package com.damian.photogram.app.auth;

import com.damian.photogram.app.auth.dto.AuthenticationRequest;
import com.damian.photogram.app.auth.dto.AuthenticationResponse;
import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.core.utils.JwtUtil;
import com.damian.photogram.domain.customer.dto.request.ProfileUpdateRequest;
import com.damian.photogram.domain.customer.enums.CustomerGender;
import com.damian.photogram.domain.customer.enums.CustomerRole;
import com.damian.photogram.domain.customer.model.Customer;
import com.damian.photogram.domain.customer.repository.CustomerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AuthorizationIntegrationTest {
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

    private String rawPassword = "123456";
    private Customer customer;
    private Customer admin;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();
        customer = new Customer();
        customer.setEmail("customer@test.com");
        customer.setPassword(bCryptPasswordEncoder.encode(rawPassword));
        customer.getProfile().setFirstName("John");
        customer.getProfile().setLastName("Wick");
        customer.getProfile().setPhone("123 123 123");
        customer.getProfile().setGender(CustomerGender.MALE);
        customer.getProfile().setBirthdate(LocalDate.of(1989, 1, 1));
        customer.getProfile().setImageFilename("no photoPath");

        customerRepository.save(customer);

        admin = new Customer();
        admin.setEmail("admin@test.com");
        admin.setPassword(bCryptPasswordEncoder.encode(rawPassword));
        admin.setRole(CustomerRole.ADMIN);

        customerRepository.save(admin);
    }

    @AfterAll
    void tearDown() {
        customerRepository.deleteAll();
    }

    String loginWithCustomer(Customer customer) throws Exception {
        // given
        AuthenticationRequest authenticationRequest = new AuthenticationRequest(
                customer.getEmail(), this.rawPassword
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
                this.rawPassword,
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
}
