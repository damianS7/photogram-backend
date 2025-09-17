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

public class FileStorageServiceTest extends AbstractServiceTest {

    @InjectMocks
    private FileStorageService fileStorageService;

    @Test
    @DisplayName("Should get root storage path")
    void shouldGetStoragePath() throws IOException {
        System.out.println(
                fileStorageService.getStoragePath(
                        ImageUploaderService.getCustomerUploadFolder(
                                1L)
                )
        );
    }

    @Test
    @DisplayName("Should store file")
    void shouldStoreFile() throws IOException {
        // given
        MultipartFile givenFile = new MockMultipartFile(
                "file.jpg",
                "photo.jpg",
                "image/jpeg",
                new byte[5]
        );

        // when
        File storedFile = fileStorageService.storeFile(
                givenFile, "", givenFile.getName()
        );

        // then
        assertNotNull(storedFile);
        assertThat(storedFile.exists()).isTrue();
        Files.deleteIfExists(Path.of(storedFile.getAbsolutePath()));
    }

    @Test
    @DisplayName("Should get file")
    void shouldGetFile() throws IOException {
        // given
        File givenFile = fileStorageService.storeFile(
                new MockMultipartFile(
                        "file.jpg",
                        "photo.jpg",
                        "image/jpeg",
                        new byte[5]
                ),
                "", "file.jpg"
        );

        // when
        File file = fileStorageService.getFile(
                "", givenFile.getName()
        );

        // then
        assertNotNull(file);
        assertThat(file.exists()).isTrue();
        Files.deleteIfExists(Path.of(givenFile.getAbsolutePath()));
    }

    @Test
    @DisplayName("Should delete file")
    void shouldDeleteFile() throws IOException {
        // given
        File givenFile = fileStorageService.storeFile(
                new MockMultipartFile(
                        "file.jpg",
                        "photo.jpg",
                        "image/jpeg",
                        new byte[5]
                ),
                "", "file.jpg"
        );

        // when
        fileStorageService.deleteFile(
                "", givenFile.getName()
        );

        // then
        assertThat(givenFile.exists()).isFalse();
    }
}
