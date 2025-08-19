package com.damian.photogram.domain.customer;

import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.core.service.ImageCacheService;
import com.damian.photogram.domain.customer.enums.CustomerGender;
import com.damian.photogram.domain.customer.exception.ProfileAuthorizationException;
import com.damian.photogram.domain.customer.model.Customer;
import com.damian.photogram.domain.customer.repository.ProfileRepository;
import com.damian.photogram.domain.customer.service.ProfileImageService;
import com.damian.photogram.domain.customer.service.ProfileService;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProfileImageServiceTest {

    private final String RAW_PASSWORD = "123456";

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private ProfileService profileService;

    @Mock
    private ImageCacheService imageCacheService;

    @InjectMocks
    private ProfileImageService profileImageService;
    private Customer customer;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        profileRepository.deleteAll();

        customer = new Customer();
        customer.setId(2L);
        customer.setEmail("customer@test.com");
        customer.setPassword(passwordEncoder.encode(RAW_PASSWORD));
        customer.getProfile().setId(5L);
        customer.getProfile().setImageFilename("avatar.jpg");
        customer.getProfile().setUsername("John");
        customer.getProfile().setFirstName("John");
        customer.getProfile().setLastName("Wick");
        customer.getProfile().setGender(CustomerGender.MALE);
        customer.getProfile().setBirthdate(LocalDate.of(1989, 1, 1));
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
        when(imageCacheService.getImage(anyString(), anyString())).thenReturn(r);
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

        // when
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
        ProfileAuthorizationException exception = assertThrows(
                ProfileAuthorizationException.class,
                () -> profileImageService.uploadImage(RAW_PASSWORD, givenFile)
        );

        // then
        assertEquals(Exceptions.PROFILE.IMAGE.EMPTY_FILE, exception.getMessage());
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
        ProfileAuthorizationException exception = assertThrows(
                ProfileAuthorizationException.class,
                () -> profileImageService.uploadImage(RAW_PASSWORD, givenFile)
        );

        // then
        assertEquals(Exceptions.PROFILE.IMAGE.ONLY_IMAGES_ALLOWED, exception.getMessage());
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
        ProfileAuthorizationException exception = assertThrows(
                ProfileAuthorizationException.class,
                () -> profileImageService.uploadImage(RAW_PASSWORD, givenFile)
        );

        // then
        assertEquals(Exceptions.PROFILE.IMAGE.FILE_SIZE_LIMIT, exception.getMessage());
    }
}
