package com.damian.photogram.core.image;

import com.damian.photogram.AbstractServiceTest;
import com.damian.photogram.core.image.service.ImageStorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ImageStorageServiceTest extends AbstractServiceTest {

    @InjectMocks
    private ImageStorageService imageStorageService;

    @Test
    @DisplayName("Should store image")
    void shouldStoreImage() throws IOException {
        // given
        MultipartFile givenFile = new MockMultipartFile(
                "file.jpg",
                "photo.jpg",
                "image/jpeg",
                new byte[5]
        );

        // when
        File storedFile = imageStorageService.storeImage(
                givenFile, "", givenFile.getName()
        );

        // then
        assertNotNull(storedFile);
        assertThat(storedFile.exists()).isTrue();
        Files.deleteIfExists(Path.of(storedFile.getAbsolutePath()));
    }

    @Test
    @DisplayName("Should get image")
    void shouldGetImage() throws IOException {
        // given
        File givenFile = imageStorageService.storeImage(
                new MockMultipartFile(
                        "file.jpg",
                        "photo.jpg",
                        "image/jpeg",
                        new byte[5]
                ),
                "", "file.jpg"
        );

        // when
        Resource storedFile = imageStorageService.getImage(
                "", givenFile.getName()
        );

        // then
        assertNotNull(storedFile);
        assertThat(storedFile.exists()).isTrue();
        Files.deleteIfExists(Path.of(givenFile.getAbsolutePath()));
    }

    @Test
    @DisplayName("Should delete image")
    void shouldDeleteImage() throws IOException {
        // given
        File givenFile = imageStorageService.storeImage(
                new MockMultipartFile(
                        "file.jpg",
                        "photo.jpg",
                        "image/jpeg",
                        new byte[5]
                ),
                "", "file.jpg"
        );

        // when
        imageStorageService.deleteImage(
                "", givenFile.getName()
        );

        // then
        assertThat(givenFile.exists()).isFalse();
    }
}
