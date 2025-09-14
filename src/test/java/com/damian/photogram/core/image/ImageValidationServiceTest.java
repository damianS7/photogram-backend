package com.damian.photogram.core.image;

import com.damian.photogram.AbstractServiceTest;
import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.core.image.exception.ImageEmptyFileException;
import com.damian.photogram.core.image.exception.ImageFileSizeExceededException;
import com.damian.photogram.core.image.exception.ImageTypeNotSupportedException;
import com.damian.photogram.core.image.service.ImageValidationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ImageValidationServiceTest extends AbstractServiceTest {

    @InjectMocks
    private ImageValidationService imageValidationService;

    @Test
    @DisplayName("Should validate image")
    void shouldValidateImage() {
        // given
        MultipartFile givenImage = new MockMultipartFile(
                "file",
                "photo.jpg",
                "image/jpeg",
                new byte[5]
        );

        // when
        imageValidationService.validateImage(
                givenImage, 5L * 1024 * 1024, new String[]{"image/jpg", "image/jpeg", "image/png"}
        );
    }

    @Test
    @DisplayName("Should not validate image when file is empty")
    void shouldNotValidateImageWhenFileIsEmpty() {
        // given
        MultipartFile givenImage = new MockMultipartFile(
                "file",
                "photo.jpg",
                "image/jpeg",
                new byte[0]
        );

        // when
        ImageEmptyFileException ex = assertThrows(
                ImageEmptyFileException.class,
                () -> imageValidationService.validateImage(
                        givenImage, 5L * 1024 * 1024, new String[]{"image/jpg", "image/jpeg", "image/png"}
                )
        );

        // then
        assertEquals(Exceptions.IMAGE.EMPTY_FILE, ex.getMessage());
    }

    @Test
    @DisplayName("Should not validate image when file is not a valid type")
    void shouldNotValidateImageWhenFileIsNotAValidType() {
        // given
        MultipartFile givenImage = new MockMultipartFile(
                "file",
                "photo.jpg",
                "octet/stream",
                new byte[5]
        );

        // when
        ImageTypeNotSupportedException ex = assertThrows(
                ImageTypeNotSupportedException.class,
                () -> imageValidationService.validateImage(
                        givenImage, 5L * 1024 * 1024, new String[]{"image/jpg", "image/jpeg", "image/png"}
                )
        );

        // then
        assertEquals(Exceptions.IMAGE.TYPE_NOT_SUPPORTED, ex.getMessage());
    }

    @Test
    @DisplayName("Should not validate image when size exceeds limit")
    void shouldNotValidateImageWhenSizeExceedsLimit() {
        // given
        MultipartFile givenImage = new MockMultipartFile(
                "file",
                "photo.jpg",
                "image/jpeg",
                new byte[1024 * 1024 + 1]
        );

        // when
        ImageFileSizeExceededException ex = assertThrows(
                ImageFileSizeExceededException.class,
                () -> imageValidationService.validateImage(
                        givenImage, 1024 * 1024, new String[]{"image/jpg", "image/jpeg", "image/png"}
                )
        );

        // then
        assertEquals(Exceptions.IMAGE.TOO_LARGE, ex.getMessage());
    }
}
