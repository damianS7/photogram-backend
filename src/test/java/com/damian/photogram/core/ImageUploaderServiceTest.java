package com.damian.photogram.core;

import com.damian.photogram.AbstractServiceTest;
import com.damian.photogram.core.image.service.ImageStorageService;
import com.damian.photogram.core.image.service.ImageUploaderService;
import com.damian.photogram.domain.customer.model.Customer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;

public class ImageUploaderServiceTest extends AbstractServiceTest {

    @InjectMocks
    private ImageUploaderService imageUploaderService;

    @Mock
    private ImageStorageService imageStorageService;

    @Test
    @DisplayName("Should upload image")
    void shouldUploadImage() throws IOException {
        // given
        setUpContext(
                Customer.create()
                        .setId(1L)
        );
        MultipartFile givenFile = new MockMultipartFile(
                "file.jpg",
                "photo.jpg",
                "image/jpeg",
                new byte[5]
        );

        // when
        doNothing().when(imageStorageService).storeImage(any(), anyString(), anyString());

        String filename = imageUploaderService.uploadImage(
                givenFile, "posts", givenFile.getName()
        );

        // then
        assertNotNull(filename);
        assertEquals(filename, givenFile.getName());
        Files.deleteIfExists(Path.of(imageUploaderService.getCustomerUploadFolder(1L) + filename));
    }

    // TODO shouldNotUploadImage
}
