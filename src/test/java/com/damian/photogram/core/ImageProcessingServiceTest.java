package com.damian.photogram.core;

import com.damian.photogram.AbstractServiceTest;
import com.damian.photogram.core.image.adapter.ImageMultipartAdapter;
import com.damian.photogram.core.service.ImageProcessingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ImageProcessingServiceTest extends AbstractServiceTest {

    private static final Logger log = LoggerFactory.getLogger(ImageProcessingServiceTest.class);

    @InjectMocks
    private ImageProcessingService imageProcessingService;

    @Test
    @DisplayName("Should compress image multipart file")
    void shouldCompressImageMultipartFile() {
        // given
        MultipartFile file = new ImageMultipartAdapter(
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
        MultipartFile file = new ImageMultipartAdapter(
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
        File file = new File(getClass().getResource("/images/4k-image.jpg").getFile());
        File copy = Files.copy(
                file.toPath(),
                new File(file.getParent() + "/image-copy.jpg").toPath()
        ).toFile();

        // when
        File compressedFile = imageProcessingService.resizeImageFile(
                copy, 1920, 1080
        );

        // then
        assertTrue(copy.length() > compressedFile.length());
        Files.deleteIfExists(Path.of(copy.getAbsolutePath()));
    }

    @Test
    @DisplayName("Should not resize image file when is within limits")
    void shouldNotResizeImageFileWhenIsWithinLimits() throws IOException {
        // given
        File file = new File(getClass().getResource("/images/4k-image.jpg").getFile());
        File copy = Files.copy(
                file.toPath(),
                new File(file.getParent() + "/image-copy.jpg").toPath()
        ).toFile();

        // when
        File compressedFile = imageProcessingService.resizeImageFile(
                copy, 9000, 9000
        );

        // then
        assertEquals(copy.length(), compressedFile.length());
        Files.deleteIfExists(Path.of(copy.getAbsolutePath()));
    }
}
