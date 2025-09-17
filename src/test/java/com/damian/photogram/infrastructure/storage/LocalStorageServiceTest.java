package com.damian.photogram.infrastructure.storage;

import com.damian.photogram.core.AbstractServiceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class LocalStorageServiceTest extends AbstractServiceTest {

    @InjectMocks
    private LocalStorageService localStorageService;

    @Test
    @DisplayName("Should store image")
    void shouldStoreFile() throws IOException {
        // given
        MultipartFile givenFile = new MockMultipartFile(
                "file.jpg",
                "photo.jpg",
                "image/jpeg",
                new byte[5]
        );

        // when
        File storedFile = localStorageService.storeFile(
                givenFile, "", givenFile.getName()
        );

        // then
        assertNotNull(storedFile);
        assertThat(storedFile.exists()).isTrue();
        Files.deleteIfExists(Path.of(storedFile.getAbsolutePath()));
    }

    @Test
    @DisplayName("Should get image")
    void shouldGetFile() throws IOException {
        // given
        File givenFile = localStorageService.storeFile(
                new MockMultipartFile(
                        "file.jpg",
                        "photo.jpg",
                        "image/jpeg",
                        new byte[5]
                ),
                "", "file.jpg"
        );

        // when
        File file = localStorageService.getFile(
                "", givenFile.getName()
        );

        // then
        assertNotNull(file);
        assertThat(file.exists()).isTrue();
        Files.deleteIfExists(Path.of(givenFile.getAbsolutePath()));
    }

    @Test
    @DisplayName("Should delete image")
    void shouldDeleteFile() throws IOException {
        // given
        File givenFile = localStorageService.storeFile(
                new MockMultipartFile(
                        "file.jpg",
                        "photo.jpg",
                        "image/jpeg",
                        new byte[5]
                ),
                "", "file.jpg"
        );

        // when
        localStorageService.deleteFile(
                "", givenFile.getName()
        );

        // then
        assertThat(givenFile.exists()).isFalse();
    }
}
