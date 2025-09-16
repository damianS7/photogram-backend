package com.damian.photogram.infrastructure.storage;

import com.damian.photogram.core.AbstractServiceTest;
import com.damian.photogram.core.util.ImageTestHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ImageProcessingServiceTest extends AbstractServiceTest {

    private static final Logger log = LoggerFactory.getLogger(ImageProcessingServiceTest.class);

    @InjectMocks
    private ImageProcessingService imageProcessingService;

    @Test
    @DisplayName("Should compress image multipart file")
    void shouldCompressImageMultipartFile() {
        // given
        MultipartFile file = new MultipartImageAdapter(
                new File(getClass().getResource("/images/avatar.png").getFile())
        );

        // when
        MultipartFile compressedFile = imageProcessingService.compressImage(
                file
        );

        // then
        assertTrue(compressedFile.getSize() < file.getSize());
    }

    @Test
    @DisplayName("Should compress image multipart file")
    void shouldOptimizeImageMultipartFile() throws IOException {
        // given
        MultipartFile file = new MultipartImageAdapter(
                new File(getClass().getResource("/images/4k-image.jpg").getFile())
        );

        // when
        MultipartFile compressedFile = imageProcessingService.optimizeImage(
                file, 1920, 1080
        );

        //        Path outputDir = Path.of(getClass().getResource("/images").getPath());
        //        Path pFile = Files.createFile(Path.of(outputDir.toAbsolutePath() + "/optimized-image.jpg"));
        //        imageProcessingService.multipartToFile(compressedFile, pFile.toFile());

        // then
        assertTrue(compressedFile.getSize() < file.getSize());
        //        Files.deleteIfExists(pFile.toAbsolutePath());
    }

    @Test
    @DisplayName("Should resize image file")
    void shouldResizeImageFile() throws IOException {
        // given
        MultipartFile givenMultipart = ImageTestHelper.createMockImage(
                "file",
                "file.jpg",
                "jpg",
                5000,
                5000,
                Color.BLUE
        );

        File givenFile = ImageTestHelper.multipartToFile(givenMultipart);

        // when
        File compressedFile = imageProcessingService.resizeImageFile(
                givenFile, 1920, 1080
        );

        // then
        assertTrue(givenMultipart.getBytes().length > compressedFile.length());
        Files.deleteIfExists(Path.of(givenFile.getAbsolutePath()));
    }

    @Test
    @DisplayName("Should not resize image file when is within limits")
    void shouldNotResizeImageFileWhenIsWithinLimits() throws IOException {
        // given
        MultipartFile givenMultipart = ImageTestHelper.createMockImage(
                "file",
                "file.jpg",
                "jpg",
                5000,
                5000,
                Color.BLUE
        );

        File givenFile = ImageTestHelper.multipartToFile(givenMultipart);

        // when
        File compressedFile = imageProcessingService.shrinkImage(
                givenFile, 9000, 9000
        );

        // then
        assertThat(givenMultipart.getBytes().length).isEqualTo(compressedFile.length());
        Files.deleteIfExists(Path.of(givenFile.getAbsolutePath()));
    }
}
