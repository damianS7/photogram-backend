package com.damian.photogram.infrastructure.storage;

import com.damian.photogram.core.AbstractServiceTest;
import com.damian.photogram.core.util.ImageTestHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ImageProcessingServiceTest extends AbstractServiceTest {

    @InjectMocks
    private ImageProcessingService imageProcessingService;

    @Test
    @DisplayName("Should compress image multipart file")
    void shouldCompressImageMultipartFile() {
        // given
        MultipartFile givenImage = new MultipartImageAdapter(
                new File(getClass().getResource("/images/avatar.png").getFile())
        );

        // when
        MultipartFile compressedFile = imageProcessingService.compressImage(
                givenImage
        );

        // then
        assertTrue(compressedFile.getSize() < givenImage.getSize());
    }

    @Test
    @DisplayName("Should compress and or resize image multipart file")
    void shouldOptimizeImageMultipartFile() {
        // given
        MultipartFile givenImage = new MultipartImageAdapter(
                new File(getClass().getResource("/images/4k-image.jpg").getFile())
        );

        // when
        MultipartFile compressedFile = imageProcessingService.optimizeImage(
                givenImage, 1920, 1080
        );

        // then
        assertTrue(compressedFile.getSize() < givenImage.getSize());
    }

    @Test
    @DisplayName("Should resize image file")
    void shouldResizeImageFile() throws IOException {
        // given
        MultipartFile givenImage = ImageTestHelper.createMockImage(
                "file",
                "file.jpg",
                "jpg",
                5000,
                5000,
                Color.BLUE
        );

        // when
        MultipartFile compressedFile = imageProcessingService.resizeImage(
                givenImage, 1920, 1080
        );

        // then
        assertTrue(givenImage.getBytes().length > compressedFile.getBytes().length);
    }

    @Test
    @DisplayName("Should not resize image file when is within limits")
    void shouldNotResizeImageFileWhenIsWithinLimits() throws IOException {
        // given
        MultipartFile givenImage = ImageTestHelper.createMockImage(
                "file",
                "file.jpg",
                "jpg",
                5000,
                5000,
                Color.BLUE
        );

        // when
        MultipartFile compressedFile = imageProcessingService.shrinkImage(
                givenImage, 9000, 9000
        );

        // then
        assertThat(givenImage.getBytes().length).isEqualTo(compressedFile.getBytes().length);
    }
}
