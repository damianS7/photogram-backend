package com.damian.photogram.domain.customer;

import com.damian.photogram.app.auth.dto.AuthenticationRequest;
import com.damian.photogram.app.auth.dto.AuthenticationResponse;
import com.damian.photogram.domain.account.enums.AccountStatus;
import com.damian.photogram.domain.customer.dto.request.ProfileUpdateRequest;
import com.damian.photogram.domain.customer.dto.response.ProfileDto;
import com.damian.photogram.domain.customer.enums.CustomerGender;
import com.damian.photogram.domain.customer.enums.CustomerRole;
import com.damian.photogram.domain.customer.model.Customer;
import com.damian.photogram.domain.customer.repository.CustomerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProfileIntegrationTest {
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
        customerA.getAccount().setAccountStatus(AccountStatus.ACTIVE);
        customerRepository.save(customerA);

        customerB = Customer.create()
                            .setMail("customerB@test.com")
                            .setPassword(bCryptPasswordEncoder.encode(this.rawPassword)
                            );
        customerB.getAccount().setAccountStatus(AccountStatus.ACTIVE);
        customerRepository.save(customerB);

        customerAdmin = Customer.create()
                                .setMail("customerAdmin@test.com")
                                .setRole(CustomerRole.ADMIN)
                                .setPassword(bCryptPasswordEncoder.encode(this.rawPassword)
                                );
        customerAdmin.getAccount().setAccountStatus(AccountStatus.ACTIVE);
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

    @Test
    @DisplayName("Should get customer profile")
    void shouldGetCustomerProfile() throws Exception {
        // given
        loginWithCustomer(customerA);

        // when
        MvcResult result = mockMvc
                .perform(
                        get("/api/v1/customers/profile")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andDo(print())
                .andExpect(status().is(200))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // then
        ProfileDto profileDTO = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ProfileDto.class
        );

        assertThat(profileDTO).isNotNull();
        assertEquals(profileDTO.firstName(), customerA.getProfile().getFirstName());
        assertEquals(profileDTO.lastName(), customerA.getProfile().getLastName());
    }

    @Test
    @DisplayName("Should update profile")
    void shouldUpdateProfile() throws Exception {
        // given
        loginWithCustomer(customerA);

        Map<String, Object> fields = new HashMap<>();
        fields.put("firstName", "alice");
        fields.put("lastName", "white");
        fields.put("phone", "999 999 999");
        fields.put("birthdate", LocalDate.of(1989, 1, 1));
        fields.put("gender", CustomerGender.FEMALE);

        ProfileUpdateRequest givenRequest = new ProfileUpdateRequest(
                this.rawPassword,
                fields
        );

        String jsonRequest = objectMapper.writeValueAsString(givenRequest);

        // when
        MvcResult result = mockMvc
                .perform(
                        patch("/api/v1/customers/profile")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .content(jsonRequest))
                .andDo(print())
                .andExpect(status().is(200))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // then
        ProfileDto profileDTO = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ProfileDto.class
        );

        assertThat(profileDTO).isNotNull();
        assertThat(profileDTO.firstName()).isEqualTo(givenRequest.fieldsToUpdate().get("firstName"));
        assertThat(profileDTO.lastName()).isEqualTo(givenRequest.fieldsToUpdate().get("lastName"));
        assertThat(profileDTO.phone()).isEqualTo(givenRequest.fieldsToUpdate().get("phone"));
        assertThat(profileDTO.birthdate()).isEqualTo(givenRequest.fieldsToUpdate().get("birthdate"));
        assertThat(profileDTO.gender()).isEqualTo(givenRequest.fieldsToUpdate().get("gender"));
    }

    @Test
    @DisplayName("Should not update profile when is not yours")
    void shouldNotUpdateProfileWhenIsNotYours() throws Exception {
        // given
        loginWithCustomer(customerB);

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
                .andExpect(status().is(403))
                .andReturn();

        // then


    }

    @Test
    @DisplayName("Should upload customer profile image")
    void shouldUploadCustomerProfileImage() throws Exception {
        // given
        loginWithCustomer(customerA);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                customerA.getProfile().getImageFilename(),
                "image/jpeg",
                new byte[5]
        );

        // when
        MvcResult result = mockMvc
                .perform(
                        multipart("/api/v1/customers/profile/photo")
                                .file(file)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .param("currentPassword", this.rawPassword)
                                .with(request -> {
                                    request.setMethod("POST");
                                    return request;
                                }))

                .andDo(print())
                .andExpect(status().is(201))
                .andReturn();

        byte[] content = result.getResponse().getContentAsByteArray();
        Resource resource = new ByteArrayResource(content);

        // then
        assertThat(resource).isNotNull();
        assertEquals(resource.contentLength(), file.getBytes().length);
        assertEquals(result.getResponse().getContentType(), file.getContentType());
    }
}