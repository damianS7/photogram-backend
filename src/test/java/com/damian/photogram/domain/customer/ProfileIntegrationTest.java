package com.damian.photogram.domain.customer;

import com.damian.photogram.AbstractIntegrationTest;
import com.damian.photogram.ImageTestHelper;
import com.damian.photogram.domain.user.account.enums.AccountStatus;
import com.damian.photogram.domain.user.customer.dto.request.ProfileUpdateRequest;
import com.damian.photogram.domain.user.customer.dto.response.ProfileDto;
import com.damian.photogram.domain.user.customer.enums.CustomerGender;
import com.damian.photogram.domain.user.customer.enums.UserRole;
import com.damian.photogram.domain.user.customer.model.Customer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
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

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProfileIntegrationTest extends AbstractIntegrationTest {

    private Customer customerA;
    private Customer customerB;
    private Customer customerAdmin;

    @BeforeAll
    void setUp() throws Exception {
        customerA = Customer.create()
                            .setEmail("customerA@test.com")
                            .setPassword(bCryptPasswordEncoder.encode(this.RAW_PASSWORD))
                            .setRole(UserRole.CUSTOMER)
                            .setProfile(profile -> profile
                                    .setFirstName("John")
                                    .setLastName("Wick")
                                    .setGender(CustomerGender.MALE)
                                    .setBirthdate(LocalDate.of(1989, 1, 1))
                                    .setImageFilename("images/avatar.jpg")
                            );
        customerA.getAccount().setAccountStatus(AccountStatus.VERIFIED);
        customerRepository.save(customerA);

        customerB = Customer.create()
                            .setEmail("customerB@test.com")
                            .setPassword(bCryptPasswordEncoder.encode(this.RAW_PASSWORD)
                            );
        customerB.getAccount().setAccountStatus(AccountStatus.VERIFIED);
        customerRepository.save(customerB);

        customerAdmin = Customer.create()
                                .setEmail("customerAdmin@test.com")
                                .setRole(UserRole.ADMIN)
                                .setPassword(bCryptPasswordEncoder.encode(this.RAW_PASSWORD)
                                );
        customerAdmin.getAccount().setAccountStatus(AccountStatus.VERIFIED);
        customerRepository.save(customerAdmin);
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
                this.RAW_PASSWORD,
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
                this.RAW_PASSWORD,
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
    void shouldUploadProfileImage() throws Exception {
        // given
        loginWithCustomer(customerA);

        MockMultipartFile file = ImageTestHelper.createDefaultJpg();

        // when
        MvcResult result = mockMvc
                .perform(
                        multipart("/api/v1/customers/profile/photo")
                                .file(file)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .param("currentPassword", this.RAW_PASSWORD)
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

    @Test
    @DisplayName("Should not upload profile image when file is empty")
    void shouldNotUploadProfileImageWhenFileIsEmpty() throws Exception {
        // given
        loginWithCustomer(customerA);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                customerA.getProfile().getImageFilename(),
                "image/jpeg",
                new byte[0]
        );

        // when
        mockMvc
                .perform(
                        multipart("/api/v1/customers/profile/photo")
                                .file(file)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .param("currentPassword", this.RAW_PASSWORD)
                                .with(request -> {
                                    request.setMethod("POST");
                                    return request;
                                }))

                .andDo(print())
                .andExpect(status().is(400))
                .andReturn();
    }

    @Test
    @DisplayName("Should upload image when size exceeds limit")
    void shouldNotUploadImageWhenSizeExceedsLimit() throws Exception {
        // given
        loginWithCustomer(customerA);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                customerA.getProfile().getImageFilename(),
                "image/jpeg",
                new byte[5 * 1024 * 1024 + 1]
        );

        // when
        mockMvc
                .perform(
                        multipart("/api/v1/customers/profile/photo")
                                .file(file)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .param("currentPassword", this.RAW_PASSWORD)
                                .with(request -> {
                                    request.setMethod("POST");
                                    return request;
                                }))

                .andDo(print())
                .andExpect(status().is(413))
                .andReturn();
    }

    @Test
    @DisplayName("Should not upload image when type is not supported")
    void shouldNotUploadImageWhenTypeIsNotSupported() throws Exception {
        // given
        loginWithCustomer(customerA);

        //        MockMultipartFile file = ImageTestHelper.createDefaultBmp();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                customerA.getProfile().getImageFilename(),
                "text/plain",
                new byte[5]
        );

        // when
        mockMvc
                .perform(
                        multipart("/api/v1/customers/profile/photo")
                                .file(file)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .param("currentPassword", this.RAW_PASSWORD)
                                .with(request -> {
                                    request.setMethod("POST");
                                    return request;
                                }))

                .andDo(print())
                .andExpect(status().is(415))
                .andReturn();
    }
}