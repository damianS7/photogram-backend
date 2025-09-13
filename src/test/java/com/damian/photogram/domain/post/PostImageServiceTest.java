package com.damian.photogram.domain.post;

import com.damian.photogram.AbstractServiceTest;
import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.core.exception.ImageEmptyFileException;
import com.damian.photogram.core.exception.ImageTypeNotAllowedException;
import com.damian.photogram.core.image.adapter.ImageMultipartAdapter;
import com.damian.photogram.core.service.ImageStorageService;
import com.damian.photogram.core.service.ImageUploaderService;
import com.damian.photogram.core.service.ImageValidationService;
import com.damian.photogram.domain.customer.enums.CustomerGender;
import com.damian.photogram.domain.customer.enums.UserRole;
import com.damian.photogram.domain.customer.model.Customer;
import com.damian.photogram.domain.post.service.PostImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class PostImageServiceTest extends AbstractServiceTest {

    @Mock
    private ImageUploaderService imageUploaderService;

    @Mock
    private ImageStorageService imageStorageService;

    //    @Spy
    @Mock
    private ImageValidationService imageValidationService;

    @InjectMocks
    private PostImageService postImageService;

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
    @DisplayName("Should upload post image")
    void shouldUploadPostImage() {
        // given
        setUpContext(customer);
        ImageMultipartAdapter givenFile = new ImageMultipartAdapter(
                new File(getClass().getResource("/images/avatar.png").getFile())
        );

        String filename = "avatar.jpg";

        // when
        doNothing().when(imageValidationService).validateImage(any(), any(Long.class), any(String[].class));
        when(imageValidationService.isResizeNeeded(
                any(MultipartFile.class),
                any(Integer.class),
                any(Integer.class)
        )).thenReturn(false);
        when(imageValidationService.isCompressionNeeded(any(MultipartFile.class), any(Long.class))).thenReturn(false);
        when(imageUploaderService.uploadImage(any(MultipartFile.class), anyString())).thenReturn(filename);

        String filenameResult = postImageService.uploadImage(
                givenFile
        );

        // then
        assertNotNull(filenameResult);
        assertEquals(filename, filenameResult);
    }

    @Test
    @DisplayName("Should not upload post image when file is empty")
    void shouldNotUploadPostImageWhenFileIsEmpty() {
        // given
        setUpContext(customer);
        MultipartFile givenFile = new MockMultipartFile(
                "file",
                "photo.jpg",
                "image/jpeg",
                new byte[0]
        );

        // when
        doThrow(new ImageEmptyFileException(Exceptions.IMAGE.EMPTY_FILE))
                .when(imageValidationService)
                .validateImage(any(MultipartFile.class), any(Long.class), any(String[].class));

        ImageEmptyFileException exception = assertThrows(
                ImageEmptyFileException.class,
                () -> postImageService.uploadImage(givenFile)
        );

        assertEquals(Exceptions.IMAGE.EMPTY_FILE, exception.getMessage());
    }

    @Test
    @DisplayName("Should not upload post image when file is not a valid image type")
    void shouldNotUploadPostImageWhenFileIsNotValidImageType() {
        // given
        setUpContext(customer);
        MultipartFile givenFile = new MockMultipartFile(
                "file",
                "photo.jpg",
                "image/jpeg",
                new byte[5]
        );

        // when
        doThrow(new ImageTypeNotAllowedException(Exceptions.IMAGE.TYPE_NOT_SUPPORTED))
                .when(imageValidationService)
                .validateImage(any(MultipartFile.class), any(Long.class), any(String[].class));

        ImageTypeNotAllowedException exception = assertThrows(
                ImageTypeNotAllowedException.class,
                () -> postImageService.uploadImage(givenFile)
        );

        assertEquals(Exceptions.IMAGE.TYPE_NOT_SUPPORTED, exception.getMessage());
    }

    // TODO

    //    @Test
    //    @DisplayName("Should not upload profile image when size exceeds limit")
    //    void shouldNotUploadProfileImageWhenSizeExceedsLimit() {
    //        // given
    //        setUpContext(customer);
    //        MultipartFile givenFile = new MockMultipartFile(
    //                "file",
    //                "photo.jpg",
    //                "image/jpeg",
    //                new byte[((int) profileImageService.getMaxImageSize()) + 1]
    //        );
    //
    //        // when
    //        ImageFileSizeExceededException exception = assertThrows(
    //                ImageFileSizeExceededException.class,
    //                () -> profileImageService.uploadImage(RAW_PASSWORD, givenFile)
    //        );
    //
    //        // then
    //        assertEquals(Exceptions.IMAGE.TOO_LARGE, exception.getMessage());
    //        assertThat(givenFile.getSize()).isGreaterThan(profileImageService.getMaxImageSize());
    //    }

    // shouldNotUploadProfileImageWhenResolutionExceedsLimit
    // shouldUploadAndCompressProfileImage
    // shouldUploadAndResizeProfileImage
}
