package com.damian.photogram.core.image.service;

import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.core.image.exception.ImageEmptyFileException;
import com.damian.photogram.core.image.exception.ImageFileSizeExceededException;
import com.damian.photogram.core.image.exception.ImageTypeNotSupportedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;

/**
 * Service class for validating images.
 */
@Service
public class ImageValidationService {
    private static final Logger log = LoggerFactory.getLogger(ImageValidationService.class);
    private final long MAX_FILE_SIZE = 5L * 1024 * 1024; // 5 MB
    private final String[] ALLOWED_IMAGE_TYPES = {"image/jpg", "image/jpeg", "image/png"};

    public void validateImage(MultipartFile file) {
        this.validateImage(file, MAX_FILE_SIZE, ALLOWED_IMAGE_TYPES);
    }

    // validations for file uploaded photos
    public void validateImage(MultipartFile file, long maxFileSize, String[] allowedImageTypes) {
        log.debug("Validating image file: {}", file.getOriginalFilename());
        if (file.isEmpty()) {
            throw new ImageEmptyFileException(Exceptions.IMAGE.EMPTY_FILE);
        }

        String contentType = file.getContentType();
        boolean imageTypeAllowed = Arrays
                .stream(allowedImageTypes)
                .anyMatch(ct -> ct.equalsIgnoreCase(contentType));

        if (!imageTypeAllowed) {
            throw new ImageTypeNotSupportedException(Exceptions.IMAGE.TYPE_NOT_SUPPORTED);
        }

        if (file.getSize() > maxFileSize) {
            throw new ImageFileSizeExceededException(Exceptions.IMAGE.TOO_LARGE);
        }
        log.info("Image validated.");
    }
}
