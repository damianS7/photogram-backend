package com.damian.photogram.domain.customer;

import com.damian.photogram.app.auth.dto.AuthenticationRequest;
import com.damian.photogram.app.auth.dto.AuthenticationResponse;
import com.damian.photogram.domain.account.enums.AccountStatus;
import com.damian.photogram.domain.customer.enums.CustomerGender;
import com.damian.photogram.domain.customer.enums.CustomerRole;
import com.damian.photogram.domain.customer.model.Customer;
import com.damian.photogram.domain.customer.repository.CustomerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

// TODO review this
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProfileImageIntegrationTest {
    private final String rawPassword = "123456";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    private Customer customerA;
    private Customer customerB;
    private Customer customerAdmin;
    private String token;

    @BeforeAll
    void setUp() throws Exception {
        customerA = Customer.create()
                            .setMail("customerA@test.com")
                            .setPassword(bCryptPasswordEncoder.encode(this.rawPassword))
                            .setRole(CustomerRole.CUSTOMER)
                            .setProfile(profile -> profile
                                    .setFirstName("John")
                                    .setLastName("Wick")
                                    .setGender(CustomerGender.MALE)
                                    .setBirthdate(LocalDate.of(1989, 1, 1))
                                    .setImageFilename("avatar.jpg")
                            );
        customerA.getAccount().setAccountStatus(AccountStatus.VERIFIED);
        customerRepository.save(customerA);

        customerB = Customer.create()
                            .setMail("customerB@test.com")
                            .setPassword(bCryptPasswordEncoder.encode(this.rawPassword)
                            );
        customerB.getAccount().setAccountStatus(AccountStatus.VERIFIED);
        customerRepository.save(customerB);

        customerAdmin = Customer.create()
                                .setMail("admin@test.com")
                                .setRole(CustomerRole.ADMIN)
                                .setPassword(bCryptPasswordEncoder.encode(this.rawPassword)
                                );
        customerAdmin.getAccount().setAccountStatus(AccountStatus.VERIFIED);
        customerRepository.save(customerAdmin);
    }

    @AfterAll
    void tearDown() {
        customerRepository.deleteAll();
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


}