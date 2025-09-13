package com.damian.photogram.core.service;

import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.core.exception.ImageEmptyFileException;
import com.damian.photogram.core.exception.ImageFileSizeExceededException;
import com.damian.photogram.core.exception.ImageTypeNotAllowedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;

/**
 * Service class for validating images.
 */
@Service
public class ImageValidationService {
    private final long MAX_FILE_SIZE = 5L * 1024 * 1024; // 5 MB
    private final String[] ALLOWED_IMAGE_TYPES = {"image/jpg", "image/jpeg", "image/png"};

    public ImageValidationService(
    ) {
    }

    public boolean isResizeNeeded(MultipartFile file, int width, int height) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(file.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (image.getWidth() > width || image.getHeight() > height) {
            return true;
        }

        return false;
    }

    public boolean isCompressionNeeded(MultipartFile file) {
        return isCompressionNeeded(file, MAX_FILE_SIZE);
    }

    public boolean isCompressionNeeded(MultipartFile file, long maxFileSize) {
        if (file.getSize() >= maxFileSize) {
            return true;
        }
        return false;
    }

    public void validateImage(MultipartFile file) {
        this.validateImage(file, MAX_FILE_SIZE, ALLOWED_IMAGE_TYPES);
    }

    // validations for file uploaded photos
    public void validateImage(MultipartFile file, long maxFileSize, String[] allowedImageTypes) {
        if (file.isEmpty()) {
            throw new ImageEmptyFileException(Exceptions.IMAGE.EMPTY_FILE);
        }

        String contentType = file.getContentType();
        boolean imageTypeAllowed = Arrays
                .stream(allowedImageTypes)
                .anyMatch(ct -> ct.equalsIgnoreCase(contentType));

        if (!imageTypeAllowed) {
            throw new ImageTypeNotAllowedException(Exceptions.IMAGE.TYPE_NOT_SUPPORTED);
        }

        if (file.getSize() > maxFileSize) {
            throw new ImageFileSizeExceededException(Exceptions.IMAGE.TOO_LARGE);
        }
    }
}
