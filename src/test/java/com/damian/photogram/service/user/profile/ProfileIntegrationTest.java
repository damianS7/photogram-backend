package com.damian.photogram.service.user.profile;

import com.damian.photogram.core.AbstractIntegrationTest;
import com.damian.photogram.core.util.ImageTestHelper;
import com.damian.photogram.core.util.JsonHelper;
import com.damian.photogram.domain.user.enums.AccountStatus;
import com.damian.photogram.domain.user.enums.CustomerGender;
import com.damian.photogram.domain.user.enums.UserRole;
import com.damian.photogram.domain.user.model.Customer;
import com.damian.photogram.infrastructure.storage.FileStorageService;
import com.damian.photogram.infrastructure.storage.ImageUploaderService;
import com.damian.photogram.infrastructure.storage.exception.FileStorageNotFoundException;
import com.damian.photogram.web.rest.user.dto.request.ProfileUpdateRequest;
import com.damian.photogram.web.rest.user.dto.response.ProfileDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProfileIntegrationTest extends AbstractIntegrationTest {

    @MockitoBean
    private FileStorageService fileStorageService;

    @MockitoBean
    private ImageUploaderService imageUploaderService;

    private Customer customerA;
    private Customer customerB;
    private Customer customerAdmin;

    @BeforeAll
    void setUp() throws Exception {
        customerA = Customer.create()
                            .setEmail("customerA@test.com")
                            .setPassword(passwordEncoder.encode(this.RAW_PASSWORD))
                            .setRole(UserRole.CUSTOMER)
                            .setProfile(profile -> profile
                                    .setFirstName("John")
                                    .setUsername("John")
                                    .setLastName("Wick")
                                    .setGender(CustomerGender.MALE)
                                    .setBirthdate(LocalDate.of(1989, 1, 1))
                                    .setImageFilename("avatar.jpg")
                            );
        customerA.getAccount().setAccountStatus(AccountStatus.VERIFIED);
        customerRepository.save(customerA);

        customerB = Customer.create()
                            .setEmail("customerB@test.com")
                            .setPassword(passwordEncoder.encode(this.RAW_PASSWORD)
                            );
        customerB.getAccount().setAccountStatus(AccountStatus.VERIFIED);
        customerRepository.save(customerB);

        customerAdmin = Customer.create()
                                .setEmail("customerAdmin@test.com")
                                .setRole(UserRole.ADMIN)
                                .setPassword(passwordEncoder.encode(this.RAW_PASSWORD)
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
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // then
        ProfileDto profileDTO = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ProfileDto.class
        );

        assertThat(profileDTO)
                .isNotNull()
                .extracting(
                        ProfileDto::firstName,
                        ProfileDto::lastName
                ).containsExactly(
                        customerA.getProfile().getFirstName(),
                        customerA.getProfile().getLastName()
                );
    }

    @Test
    @DisplayName("Should check if username profile exists")
    void shouldCheckIfUsernameProfileExists() throws Exception {
        // given
        loginWithCustomer(customerA);

        // when
        mockMvc
                .perform(
                        get(
                                "/api/v1/customers/profile/username/{username}/exists",
                                customerA.getProfile().getUsername()
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andDo(print())
                .andExpect(status().is(HttpStatus.OK.value()));
    }

    @Test
    @DisplayName("Should check if username profile not exists")
    void shouldCheckIfUsernameProfileNotExists() throws Exception {
        // given
        loginWithCustomer(customerA);

        // when
        mockMvc
                .perform(
                        get(
                                "/api/v1/customers/profile/username/{username}/exists",
                                "non-existing-username"
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andDo(print())
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    @DisplayName("Should update profile")
    void shouldUpdateProfile() throws Exception {
        // given
        loginWithCustomer(customerA);

        Map<String, Object> fields = new HashMap<>();
        fields.put("firstName", "alice");
        fields.put("lastName", "white");
        fields.put("userName", "alice77");
        fields.put("phone", "999 999 999");
        fields.put("birthdate", LocalDate.of(1989, 1, 1));
        fields.put("gender", CustomerGender.FEMALE);

        ProfileUpdateRequest givenRequest = new ProfileUpdateRequest(
                this.RAW_PASSWORD,
                fields
        );

        // when
        MvcResult result = mockMvc
                .perform(
                        patch("/api/v1/customers/profile")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .content(JsonHelper.toJson(givenRequest)))
                .andDo(print())
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // then
        ProfileDto profileDTO = JsonHelper.fromJson(
                result.getResponse().getContentAsString(),
                ProfileDto.class
        );

        assertThat(profileDTO)
                .isNotNull()
                .extracting(
                        ProfileDto::firstName,
                        ProfileDto::lastName,
                        ProfileDto::username,
                        ProfileDto::phone,
                        ProfileDto::birthdate,
                        ProfileDto::gender
                ).containsExactly(
                        givenRequest.fieldsToUpdate().get("firstName"),
                        givenRequest.fieldsToUpdate().get("lastName"),
                        givenRequest.fieldsToUpdate().get("userName"),
                        givenRequest.fieldsToUpdate().get("phone"),
                        givenRequest.fieldsToUpdate().get("birthdate"),
                        givenRequest.fieldsToUpdate().get("gender")
                );
    }

    @Test
    @DisplayName("Should get profile image")
    void shouldGetProfileImage() throws Exception {
        // given
        loginWithCustomer(customerA);

        MultipartFile imageMultipart = ImageTestHelper.createDefaultJpg();
        File imageFile = ImageTestHelper.multipartToFile(imageMultipart);
        Resource imageResource = new UrlResource(imageFile.toURI());

        when(fileStorageService.getFile(anyString(), anyString())).thenReturn(imageFile);
        when(fileStorageService.createResource(any(File.class))).thenReturn(imageResource);

        mockMvc
                .perform(
                        get("/api/v1/customers/{id}/profile/image", customerA.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andDo(print())
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.IMAGE_JPEG))
                .andReturn();
    }

    @Test
    @DisplayName("Should not get profile image when not exist")
    void shouldNotGetProfileImageWhenNotExist() throws Exception {
        // given
        loginWithCustomer(customerA);

        when(fileStorageService.getFile(anyString(), anyString())).thenThrow(
                FileStorageNotFoundException.class
        );

        mockMvc
                .perform(
                        get("/api/v1/customers/{id}/profile/image", customerA.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andDo(print())
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should upload customer profile image")
    void shouldUploadProfileImage() throws Exception {
        // given
        loginWithCustomer(customerA);

        MockMultipartFile imageMultipart = ImageTestHelper.createDefaultJpg();
        File imageFile = ImageTestHelper.multipartToFile(imageMultipart);
        Resource imageResource = new UrlResource(imageFile.toURI());

        when(fileStorageService.getFile(anyString(), anyString())).thenReturn(imageFile);
        when(fileStorageService.createResource(any(File.class))).thenReturn(imageResource);

        when(imageUploaderService.uploadImage(
                any(MultipartFile.class),
                anyString(),
                anyString()
        )).thenReturn(imageFile);

        // when
        MvcResult result = mockMvc
                .perform(
                        multipart("/api/v1/customers/profile/image")
                                .file(imageMultipart)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .param("currentPassword", this.RAW_PASSWORD)
                                .with(request -> {
                                    request.setMethod("POST");
                                    return request;
                                }))

                .andDo(print())
                .andExpect(status().is(HttpStatus.CREATED.value()))
                .andReturn();

        byte[] content = result.getResponse().getContentAsByteArray();
        Resource resource = new ByteArrayResource(content);

        // then
        assertThat(resource).isNotNull();
        assertEquals(resource.contentLength(), imageMultipart.getBytes().length);
        assertEquals(result.getResponse().getContentType(), imageMultipart.getContentType());
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

        // when
        mockMvc
                .perform(
                        patch("/api/v1/admin/profiles/{id}", customerA.getProfile().getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .content(JsonHelper.toJson(givenRequest)))
                .andDo(print())
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
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
                        multipart("/api/v1/customers/profile/image")
                                .file(file)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .param("currentPassword", this.RAW_PASSWORD)
                                .with(request -> {
                                    request.setMethod("POST");
                                    return request;
                                }))

                .andDo(print())
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    @DisplayName("Should not upload image when size exceeds limit")
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
                        multipart("/api/v1/customers/profile/image")
                                .file(file)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .param("currentPassword", this.RAW_PASSWORD)
                                .with(request -> {
                                    request.setMethod("POST");
                                    return request;
                                }))

                .andDo(print())
                .andExpect(status().is(HttpStatus.PAYLOAD_TOO_LARGE.value()));
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
                        multipart("/api/v1/customers/profile/image")
                                .file(file)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .param("currentPassword", this.RAW_PASSWORD)
                                .with(request -> {
                                    request.setMethod("POST");
                                    return request;
                                }))

                .andDo(print())
                .andExpect(status().is(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value()));
    }
}