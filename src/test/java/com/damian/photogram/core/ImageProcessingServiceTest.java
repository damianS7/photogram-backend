package com.damian.photogram.core;

import com.damian.photogram.core.service.ImageProcessingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class ImageProcessingServiceTest {

    @InjectMocks
    private ImageProcessingService imageProcessingService;

    @Test
    @DisplayName("Should compress image")
    void shouldCompressImage() {
        // given
        File file = new File(getClass().getResource("/images/avatar.png").getFile());

        // when
        MultipartFile compressedFile = imageProcessingService.compressImage(
                file
        );

        // then
        assertTrue(compressedFile.getSize() < file.length());
    }

    @Test
    @DisplayName("Should resize image")
    void shouldResizeImage() {
        // given
        File file = new File(getClass().getResource("/images/4k-image.jpg").getFile());

        // when
        MultipartFile compressedFile = imageProcessingService.resizeImageFile(
                file, 1920, 1080
        );

        // then
        assertTrue(file.length() > compressedFile.getSize());
    }
}
