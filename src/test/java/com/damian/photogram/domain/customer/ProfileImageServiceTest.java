package com.damian.photogram.domain.customer;

import com.damian.photogram.AbstractServiceTest;
import com.damian.photogram.ImageTestHelper;
import com.damian.photogram.core.image.service.ImageProcessingService;
import com.damian.photogram.core.image.service.ImageStorageService;
import com.damian.photogram.core.image.service.ImageUploaderService;
import com.damian.photogram.core.image.service.ImageValidationService;
import com.damian.photogram.domain.user.customer.enums.CustomerGender;
import com.damian.photogram.domain.user.customer.enums.UserRole;
import com.damian.photogram.domain.user.customer.model.Customer;
import com.damian.photogram.domain.user.customer.model.Profile;
import com.damian.photogram.domain.user.customer.repository.ProfileRepository;
import com.damian.photogram.domain.user.customer.service.ProfileImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class ProfileImageServiceTest extends AbstractServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private ImageUploaderService imageUploaderService;

    @Mock
    private ImageStorageService imageStorageService;

    @Mock
    private ImageValidationService imageValidationService;

    @Mock
    private ImageProcessingService imageProcessingService;

    @InjectMocks
    private ProfileImageService profileImageService;
    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = Customer.create()
                           .setId(2L)
                           .setEmail("customer@test.com")
                           .setPassword(passwordEncoder.encode(RAW_PASSWORD))
                           .setRole(UserRole.CUSTOMER)
                           .setProfile(profile -> profile
                                   .setId(5L)
                                   .setUsername("John")
                                   .setFirstName("John")
                                   .setLastName("Wick")
                                   .setGender(CustomerGender.MALE)
                                   .setBirthdate(LocalDate.of(1989, 1, 1))
                                   .setImageFilename("images/avatar.jpg")
                           );
    }

    @Test
    @DisplayName("Should get profile image")
    void shouldGetProfileImage() throws IOException {
        // given
        File givenFile = ImageTestHelper.multipartToFile(
                ImageTestHelper.createDefaultJpg()
        );
        Resource givenResource = new UrlResource(givenFile.toURI());

        // when
        when(profileRepository.findByCustomer_Id(customer.getId())).thenReturn(Optional.of(customer.getProfile()));
        when(imageStorageService.getImage(anyString(), anyString())).thenReturn(givenResource);
        Resource resource = profileImageService.getProfileImage(customer.getId());

        // then
        assertNotNull(resource);
        assertTrue(resource.exists());

        // cleanup
        Files.deleteIfExists(Path.of(givenFile.getAbsolutePath()));
    }

    @Test
    @DisplayName("Should upload profile image")
    void shouldUploadProfileImage() {
        // given
        setUpContext(customer);
        MultipartFile givenFile = ImageTestHelper.createDefaultJpg();
        String filename = "avatar.jpg";

        // when
        when(profileRepository.save(any(Profile.class))).thenReturn(customer.getProfile());
        doNothing().when(imageValidationService).validateImage(any(), any(Long.class), any(String[].class));
        when(imageProcessingService.optimizeImage(any(), any(Integer.class), any(Integer.class))).thenReturn(givenFile);
        when(imageUploaderService.uploadImage(any(MultipartFile.class), anyString(), anyString())).thenReturn(filename);

        String filenameResult = profileImageService.uploadProfileImage(
                RAW_PASSWORD, givenFile
        );

        // then
        assertNotNull(filenameResult);
        assertEquals(filename, filenameResult);
        verify(profileRepository, times(1)).save(any(Profile.class));
    }
}
