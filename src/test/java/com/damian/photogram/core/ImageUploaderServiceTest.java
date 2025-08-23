package com.damian.photogram.core;

import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.core.exception.ImageEmptyFileException;
import com.damian.photogram.core.exception.ImageFileSizeExceededException;
import com.damian.photogram.core.exception.ImageTypeNotAllowedException;
import com.damian.photogram.core.service.ImageUploaderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ImageUploaderServiceTest {

    @InjectMocks
    private ImageUploaderService imageUploaderService;

    @Test
    @DisplayName("Should upload image")
    void shouldUploadImage() throws IOException {
        // given
        MultipartFile givenFile = new MockMultipartFile(
                "file.jpg",
                "photo.jpg",
                "image/jpeg",
                new byte[5]
        );

        // when
        String filename = imageUploaderService.uploadImage(
                givenFile, "posts", givenFile.getName()
        );

        // then
        assertNotNull(filename);
        assertEquals(filename, givenFile.getName());
        Files.deleteIfExists(Path.of("./uploads/images/posts/" + filename));
    }

    @Test
    @DisplayName("Should not upload image when file is empty")
    void shouldNotUploadImageWhenFileIsEmpty() {
        // given
        MultipartFile givenFile = new MockMultipartFile(
                "file.jpg",
                "photo.jpg",
                "image/jpeg",
                new byte[0]
        );

        // when
        ImageEmptyFileException exception = assertThrows(
                ImageEmptyFileException.class,
                () -> imageUploaderService.uploadImage(givenFile, "posts")
        );

        // then
        assertEquals(Exceptions.IMAGE.EMPTY_FILE, exception.getMessage());
    }

    @Test
    @DisplayName("Should not upload image when file is not image")
    void shouldNotUploadImageWhenFileIsNotImage() {
        // given
        MultipartFile givenFile = new MockMultipartFile(
                "file.jpg",
                "photo.jpg",
                "video/mp4",
                new byte[5]
        );

        // when
        ImageTypeNotAllowedException exception = assertThrows(
                ImageTypeNotAllowedException.class,
                () -> imageUploaderService.uploadImage(givenFile, "posts")
        );

        // then
        assertEquals(Exceptions.IMAGE.TYPE_NOT_ALLOWED, exception.getMessage());
    }

    @Test
    @DisplayName("Should not upload image when size exceeds limit")
    void shouldNotUploadImageWhenSizeExceedsLimit() {
        // given
        MultipartFile givenFile = new MockMultipartFile(
                "file",
                "photo.jpg",
                "image/jpeg",
                new byte[5 * 1024 * 1024 + 1]
        );

        // when
        ImageFileSizeExceededException exception = assertThrows(
                ImageFileSizeExceededException.class,
                () -> imageUploaderService.uploadImage(givenFile, "posts")
        );

        // then
        assertEquals(Exceptions.IMAGE.FILE_SIZE_LIMIT, exception.getMessage());
    }
}
