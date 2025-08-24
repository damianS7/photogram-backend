package com.damian.photogram.domain.customer;

import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.core.exception.ImageEmptyFileException;
import com.damian.photogram.core.exception.ImageFileSizeExceededException;
import com.damian.photogram.core.exception.ImageTypeNotAllowedException;
import com.damian.photogram.core.service.ImageStorageService;
import com.damian.photogram.core.service.ImageUploaderService;
import com.damian.photogram.domain.customer.enums.CustomerGender;
import com.damian.photogram.domain.customer.enums.CustomerRole;
import com.damian.photogram.domain.customer.model.Customer;
import com.damian.photogram.domain.customer.model.Profile;
import com.damian.photogram.domain.customer.repository.ProfileRepository;
import com.damian.photogram.domain.customer.service.ProfileImageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProfileImageServiceTest {

    private final String RAW_PASSWORD = "123456";

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private ImageUploaderService imageUploaderService;

    @Mock
    private ImageStorageService imageStorageService;

    @InjectMocks
    private ProfileImageService profileImageService;
    private Customer customer;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        profileRepository.deleteAll();

        customer = Customer.create()
                           .setId(2L)
                           .setMail("customer@test.com")
                           .setPassword(passwordEncoder.encode(RAW_PASSWORD))
                           .setRole(CustomerRole.CUSTOMER)
                           .setProfile(profile -> profile
                                   .setId(5L)
                                   .setUsername("John")
                                   .setFirstName("John")
                                   .setLastName("Wick")
                                   .setGender(CustomerGender.MALE)
                                   .setBirthdate(LocalDate.of(1989, 1, 1))
                                   .setImageFilename("avatar.jpg")
                           );
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    void setUpContext(Customer customer) {
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(customer);
    }

    @Test
    @DisplayName("Should get profile image")
    void shouldGetProfileImage() throws IOException {
        // given
        //        setUpContext(customer);

        String filename = "image.jpg";
        Path directoryPath = Paths.get("uploads/images/customers/" + customer.getId() + "/");
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
        Path directoryPath = Paths.get("uploads/images/customers/" + customer.getId() + "/");
        Files.createDirectories(directoryPath); // ensure path exists
        Path filePath = directoryPath.resolve(filename);
        Files.write(filePath, givenFile.getBytes()); // create dummy file
        Resource r = new UrlResource(filePath.toUri());

        // when
        when(imageUploaderService.uploadImage(any(MultipartFile.class), anyString(), anyString())).thenReturn(
                filename);
        when(profileRepository.save(any(Profile.class))).thenReturn(customer.getProfile());
        when(imageStorageService.getImage(anyString(), anyString())).thenReturn(r);
        Resource result = profileImageService.uploadImage(
                RAW_PASSWORD, givenFile
        );

        // then
        assertNotNull(result);
        assertEquals(result.getFile().length(), givenFile.getSize());
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
        when(imageUploaderService.uploadImage(any(MultipartFile.class), anyString(), anyString())).thenThrow(
                ImageEmptyFileException.class
        );
        assertThrows(
                ImageEmptyFileException.class,
                () -> profileImageService.uploadImage(RAW_PASSWORD, givenFile)
        );
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
        when(imageUploaderService.uploadImage(any(MultipartFile.class), anyString(), anyString())).thenThrow(
                ImageTypeNotAllowedException.class
        );

        ImageTypeNotAllowedException exception = assertThrows(
                ImageTypeNotAllowedException.class,
                () -> profileImageService.uploadImage(RAW_PASSWORD, givenFile)
        );

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
                new byte[5 * 1024 * 1024 + 1]
        );

        // when
        ImageFileSizeExceededException exception = assertThrows(
                ImageFileSizeExceededException.class,
                () -> profileImageService.uploadImage(RAW_PASSWORD, givenFile)
        );

        // then
        assertEquals(Exceptions.PROFILE.IMAGE.FILE_SIZE_LIMIT, exception.getMessage());
    }
}
