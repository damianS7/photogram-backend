package com.damian.photogram.customer.profile;

import com.damian.photogram.auth.http.AuthenticationRequest;
import com.damian.photogram.auth.http.AuthenticationResponse;
import com.damian.photogram.customer.Customer;
import com.damian.photogram.customer.CustomerGender;
import com.damian.photogram.customer.CustomerRepository;
import com.damian.photogram.customer.CustomerRole;
import com.damian.photogram.customer.profile.http.request.ProfileUpdateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
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

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
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
        customerA.getProfile().setAvatarFilename("image.jpg");
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
    @DisplayName("Should get customer profile")
    void shouldGetCustomerProfile() throws Exception {
        // given
        loginWithCustomer(customerA);

        // when
        MvcResult result = mockMvc
                .perform(
                        get("/api/v1/customers/me/profile")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andDo(print())
                .andExpect(status().is(200))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // then
        ProfileDTO profileDTO = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ProfileDTO.class
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
                        patch("/api/v1/customers/me/profile")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .content(jsonRequest))
                .andDo(print())
                .andExpect(status().is(200))
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
                customerA.getProfile().getAvatarFilename(),
                "image/jpeg",
                new byte[5]
        );

        // when
        MvcResult result = mockMvc
                .perform(
                        multipart("/api/v1/customers/me/profile/photo")
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