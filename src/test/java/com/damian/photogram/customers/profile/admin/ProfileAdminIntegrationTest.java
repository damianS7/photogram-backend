package com.damian.photogram.customers.profile.admin;

import com.damian.photogram.auth.AuthenticationService;
import com.damian.photogram.auth.http.AuthenticationRequest;
import com.damian.photogram.auth.http.AuthenticationResponse;
import com.damian.photogram.customers.Customer;
import com.damian.photogram.customers.CustomerGender;
import com.damian.photogram.customers.CustomerRepository;
import com.damian.photogram.customers.CustomerRole;
import com.damian.photogram.customers.profile.ProfileDTO;
import com.damian.photogram.customers.profile.http.request.ProfileUpdateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.datafaker.Faker;
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class ProfileAdminIntegrationTest {
    private final String rawPassword = "123456";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Faker faker;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    private Customer customerA;
    private Customer customerB;
    private Customer customerAdmin;
    private String token;

    @BeforeEach
    void setUp() throws Exception {
        customerRepository.deleteAll();

        customerA = new Customer();
        customerA.setEmail("customerA@test.com");
        customerA.setRole(CustomerRole.CUSTOMER);
        customerA.setPassword(bCryptPasswordEncoder.encode(this.rawPassword));
        customerA.getProfile().setFirstName("John");
        customerA.getProfile().setLastName("Wick");
        customerA.getProfile().setGender(CustomerGender.MALE);
        customerA.getProfile().setBirthdate(LocalDate.of(1989, 1, 1));
        customerRepository.save(customerA);

        customerB = new Customer();
        customerB.setPassword(bCryptPasswordEncoder.encode("123456"));
        customerB.setEmail("customerB@test.com");
        customerRepository.save(customerB);

        customerAdmin = new Customer();
        customerAdmin.setPassword(bCryptPasswordEncoder.encode("123456"));
        customerAdmin.setEmail("admin@test.com");
        customerAdmin.setRole(CustomerRole.ADMIN);
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

        token = response.token();
    }

    @Test
    @DisplayName("Should update any profile")
    void shouldUpdateAnyProfile() throws Exception {
        // given
        loginWithCustomer(customerAdmin);

        Map<String, Object> fields = new HashMap<>();
        fields.put("firstName", "alice");
        fields.put("lastName", "white");

        ProfileUpdateRequest givenRequest = new ProfileUpdateRequest(
                this.rawPassword,
                fields
        );

        String jsonRequest = objectMapper.writeValueAsString(givenRequest);

        // when
        MvcResult result = mockMvc
                .perform(
                        patch("/api/v1/admin/profiles/{id}", customerA.getProfile().getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .content(jsonRequest))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // then
        ProfileDTO profileDTO = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ProfileDTO.class
        );

        assertThat(profileDTO).isNotNull();
        assertThat(profileDTO.firstName()).isEqualTo(givenRequest.fieldsToUpdate().get("firstName"));
        assertThat(profileDTO.lastName()).isEqualTo(givenRequest.fieldsToUpdate().get("lastName"));
    }
}