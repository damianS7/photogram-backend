package com.damian.photogram.domain.customer;

import com.damian.photogram.AbstractServiceTest;
import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.core.exception.ImageEmptyFileException;
import com.damian.photogram.core.exception.ImageFileSizeExceededException;
import com.damian.photogram.core.exception.ImageTypeNotAllowedException;
import com.damian.photogram.core.service.ImageStorageService;
import com.damian.photogram.core.service.ImageUploaderService;
import com.damian.photogram.core.service.ImageValidationService;
import com.damian.photogram.domain.customer.enums.CustomerGender;
import com.damian.photogram.domain.customer.enums.UserRole;
import com.damian.photogram.domain.customer.model.Customer;
import com.damian.photogram.domain.customer.model.Profile;
import com.damian.photogram.domain.customer.repository.ProfileRepository;
import com.damian.photogram.domain.customer.service.ProfileImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Spy
    private ImageValidationService imageValidationService;

    @InjectMocks
    private ProfileImageService profileImageService;
    private Customer customer;

    @BeforeEach
    void setUp() {
        // TODO remove this?
        passwordEncoder = new BCryptPasswordEncoder();
        profileRepository.deleteAll();

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
        String filename = "image.jpg";
        Path directoryPath = Paths.get(imageUploaderService.getCustomerUploadFolder(customer.getId()) + "/");
        Files.createDirectories(directoryPath); // ensure path exists
        Path filePath = directoryPath.resolve(filename);
        Files.write(filePath, "test".getBytes()); // create dummy file
        Resource r = new UrlResource(filePath.toUri());

        // when
        when(profileRepository.findByCustomer_Id(customer.getId())).thenReturn(Optional.of(customer.getProfile()));
        when(imageStorageService.getImage(anyString(), anyString())).thenReturn(r);
        Resource resource = profileImageService.getProfileImage(customer.getId());

        // then
        assertNotNull(resource);
        assertTrue(resource.exists());

        // cleanup
        Files.deleteIfExists(filePath);
    }

    @Test
    @DisplayName("Should upload profile image")
    void shouldUploadProfileImage() throws IOException {
        // given
        setUpContext(customer);
        MultipartFile givenFile = new MockMultipartFile(
                "file",
                "photo.jpg",
                "image/jpeg",
                new byte[5]
        );

        String filename = "avatar.jpg";

        ImageValidationService imgValidationService = Mockito.mock(ImageValidationService.class);

        // when
        when(profileRepository.save(any(Profile.class))).thenReturn(customer.getProfile());
        doNothing().when(imgValidationService).validateImage(any(), any(Long.class), any(String[].class));
        when(imgValidationService.isResizeNeeded(
                any(MultipartFile.class),
                any(Integer.class),
                any(Integer.class)
        )).thenReturn(false);
        when(imgValidationService.isCompressionNeeded(any(MultipartFile.class), any(Long.class))).thenReturn(false);
        when(imageUploaderService.uploadImage(any(MultipartFile.class), anyString(), anyString())).thenReturn(filename);

        String filenameResult = profileImageService.uploadImage(
                RAW_PASSWORD, givenFile
        );

        // then
        assertNotNull(filenameResult);
        assertEquals(filename, filenameResult);
        verify(profileRepository, times(1)).save(any(Profile.class));
    }

    @Test
    @DisplayName("Should not upload profile image when file is empty")
    void shouldNotUploadProfileImageWhenFileIsEmpty() {
        // given
        setUpContext(customer);
        MultipartFile givenFile = new MockMultipartFile(
                "file",
                "photo.jpg",
                "image/jpeg",
                new byte[0]
        );

        // when
        ImageEmptyFileException exception = assertThrows(
                ImageEmptyFileException.class,
                () -> profileImageService.uploadImage(RAW_PASSWORD, givenFile)
        );

        assertEquals(Exceptions.IMAGE.EMPTY_FILE, exception.getMessage());
    }

    @Test
    @DisplayName("Should not upload profile image when file is not image")
    void shouldNotUploadProfileImageWhenFileIsNotImage() {
        // given
        setUpContext(customer);
        MultipartFile givenFile = new MockMultipartFile(
                "file",
                "photo.jpg",
                "not-image/jpeg",
                new byte[5]
        );

        // when
        ImageTypeNotAllowedException exception = assertThrows(
                ImageTypeNotAllowedException.class,
                () -> profileImageService.uploadImage(RAW_PASSWORD, givenFile)
        );

        assertEquals(Exceptions.IMAGE.TYPE_NOT_SUPPORTED, exception.getMessage());
    }

    @Test
    @DisplayName("Should not upload profile image when size exceeds limit")
    void shouldNotUploadProfileImageWhenSizeExceedsLimit() {
        // given
        setUpContext(customer);
        MultipartFile givenFile = new MockMultipartFile(
                "file",
                "photo.jpg",
                "image/jpeg",
                new byte[((int) profileImageService.getMaxImageSize()) + 1]
        );

        // when
        ImageFileSizeExceededException exception = assertThrows(
                ImageFileSizeExceededException.class,
                () -> profileImageService.uploadImage(RAW_PASSWORD, givenFile)
        );

        // then
        assertEquals(Exceptions.IMAGE.TOO_LARGE, exception.getMessage());
        assertThat(givenFile.getSize()).isGreaterThan(profileImageService.getMaxImageSize());
    }

    // shouldNotUploadProfileImageWhenResolutionExceedsLimit
    // shouldUploadAndCompressProfileImage
    // shouldUploadAndResizeProfileImage
}
