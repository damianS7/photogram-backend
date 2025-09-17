package com.damian.photogram.infrastructure.storage;

import com.damian.photogram.core.AbstractServiceTest;
import com.damian.photogram.core.util.ImageTestHelper;
import com.damian.photogram.domain.user.model.Customer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class ImageUploaderServiceTest extends AbstractServiceTest {

    @InjectMocks
    private ImageUploaderService imageUploaderService;

    @Mock
    private FileStorageService fileStorageService;

    @Test
    @DisplayName("Should get upload path")
    void shouldGetUploadPath() {
        System.out.println(
                ImageUploaderService.getCustomerUploadFolder(1L)
        );
    }

    @Test
    @DisplayName("Should upload image")
    void shouldUploadImage() {
        // given
        setUpContext(Customer.create()
                             .setId(1L)
        );

        MultipartFile givenMultipart = ImageTestHelper.createDefaultJpg();
        File tempFile = ImageTestHelper.multipartToFile(givenMultipart);

        // when
        when(fileStorageService.storeFile(any(MultipartFile.class), anyString(), anyString()))
                .thenReturn(tempFile);

        File uploadedImage = imageUploaderService.uploadImage(
                givenMultipart, "posts", tempFile.getName()
        );

        // then
        assertNotNull(uploadedImage);
        assertThat(uploadedImage.exists()).isTrue();
        assertEquals(tempFile.length(), uploadedImage.length());
    }
}
