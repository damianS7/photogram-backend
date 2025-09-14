package com.damian.photogram.domain.post;

import com.damian.photogram.AbstractServiceTest;
import com.damian.photogram.ImageTestHelper;
import com.damian.photogram.core.image.service.ImageProcessingService;
import com.damian.photogram.core.image.service.ImageUploaderService;
import com.damian.photogram.core.image.service.ImageValidationService;
import com.damian.photogram.domain.user.customer.enums.CustomerGender;
import com.damian.photogram.domain.user.customer.enums.UserRole;
import com.damian.photogram.domain.user.customer.model.Customer;
import com.damian.photogram.domain.post.service.PostImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

public class PostImageServiceTest extends AbstractServiceTest {

    @Mock
    private ImageUploaderService imageUploaderService;

    @Mock
    private ImageProcessingService imageProcessingService;

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
        MockMultipartFile givenFile = ImageTestHelper.createDefaultJpg();

        String filename = "avatar.jpg";

        // when
        doNothing().when(imageValidationService).validateImage(any(), any(Long.class), any(String[].class));
        when(imageProcessingService.optimizeImage(any(), any(Integer.class), any(Integer.class))).thenReturn(givenFile);
        when(imageUploaderService.uploadImage(any(MultipartFile.class), anyString())).thenReturn(filename);

        String filenameResult = postImageService.uploadImage(
                givenFile
        );

        // then
        assertNotNull(filenameResult);
        assertEquals(filename, filenameResult);
    }

    // TODO shouldGetImage
}
